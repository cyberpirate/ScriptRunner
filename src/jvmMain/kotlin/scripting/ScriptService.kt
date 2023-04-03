package scripting

import ActionScript
import ScriptInfo
import kotlinx.coroutines.*
import services.BaseService
import services.Event
import services.EventService
import services.Service
import util.Constants
import util.FolderWatcher
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ScriptService: BaseService() {

    override val dependencies: List<KClass<out Service>>
        get() = listOf(EventService::class)

    private val _loadedScripts = ArrayList<ScriptContext>()
    val loadedScripts: List<ScriptContext> get() = _loadedScripts

    lateinit var compilationConfiguration: ScriptCompilationConfiguration
    lateinit var host: BasicJvmScriptingHost
    lateinit var scriptWatcher: FolderWatcher

    data class ScriptListUpdated(val scripts: List<ScriptContext>): Event
    data class ScriptErrorsUpdated(val scriptPath: String, val errors: List<String>): Event
    data class ScriptRunning(val scriptPath: String, val running: Boolean): Event
    data class ScriptOutputUpdated(val scriptPath: String, val output: List<String>): Event

    override suspend fun initialize() {
        compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ActionScript>()
        host = BasicJvmScriptingHost()

        val scriptFolder = Constants.configFolder
        if(!scriptFolder.exists())
            scriptFolder.mkdirs()

        scriptWatcher = FolderWatcher(
            scope.coroutineContext,
            scriptFolder,
            ::onFileUpdate
        )
    }

    private suspend fun onFileUpdate(file: File, change: FolderWatcher.ChangeType) {
        if(!file.absolutePath.endsWith(ScriptConst.EXT, ignoreCase = true))
            return

        when(change) {
            FolderWatcher.ChangeType.created -> loadScript(file)
            FolderWatcher.ChangeType.updated -> reloadScript(file)
            FolderWatcher.ChangeType.deleted -> unloadScript(file)
        }

//        if(change == FolderWatcher.ChangeType.created || change == FolderWatcher.ChangeType.deleted) {
            sm.get<EventService>().send(ScriptListUpdated(loadedScripts))
//        }
    }

    private fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
        return host.eval(scriptFile.toScriptSource(), compilationConfiguration, null)
    }

    private suspend fun loadScript(scriptFile: File) {

        val res = evalFile(scriptFile)

        val scriptKey = scriptFile.absolutePath
        val sCtx = ScriptContext(scriptKey)
        _loadedScripts.add(sCtx)

        res.reports.forEach({
            if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
                sCtx.errors.add("${it.severity.name} ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
                sm.get<EventService>().send(ScriptErrorsUpdated(scriptKey, sCtx.errors))
            }
        })

        res.reports.forEach({
            it.location
        })

        val returnValue = res.valueOrNull()?.returnValue
        if(returnValue == null) {
            sCtx.errors.add("returnValue is null")
            sm.get<EventService>().send(ScriptErrorsUpdated(scriptKey, sCtx.errors))
            return
        }

        if(returnValue !is ResultValue.Value) {
            sCtx.errors.add("returnValue is not ResultValue.Value")
            sm.get<EventService>().send(ScriptErrorsUpdated(scriptKey, sCtx.errors))
            return
        }
        val scriptInfo = returnValue.value
        if(scriptInfo !is ScriptInfo) {
            sCtx.errors.add("buttonAction is not ButtonAction")
            sm.get<EventService>().send(ScriptErrorsUpdated(scriptKey, sCtx.errors))
            return
        }
        sCtx.info = scriptInfo
    }

    private suspend fun unloadScript(scriptFile: File) {
        val loaded = _loadedScripts.find({ it.filePath == scriptFile.absolutePath })
        _loadedScripts.removeIf({ it.filePath == scriptFile.absolutePath })

        if(loaded?.running?.get() == true)
            loaded.currentJob?.cancelAndJoin()
    }

    private suspend fun reloadScript(scriptFile: File) {
        unloadScript(scriptFile)
        loadScript(scriptFile)
    }

    fun runScript(sCtx: ScriptContext) {
        scope.launch {
            val runScript = sCtx.info?.onRun ?: return@launch

            sCtx.running.set(true)
            sm.get<EventService>().send(ScriptRunning(sCtx.filePath, true))

            val supervisorJob = SupervisorJob()
            sCtx.currentJob = supervisorJob
            val scriptScope = CoroutineScope(scope.coroutineContext + supervisorJob + Dispatchers.Default)

            scriptScope.launch {
                try {
                    ScriptEnvImpl(sCtx, scriptScope, sm).runScript()
                } catch(_: Exception) { }
            }

            while(supervisorJob.children.any()) {
                supervisorJob.children.forEach({ it.join() })
            }

            supervisorJob.cancelAndJoin()
            scriptScope.cancel()

            sCtx.running.set(false)
            sm.get<EventService>().send(ScriptRunning(sCtx.filePath, false))
        }
    }
}