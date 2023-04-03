package scripting

import RunChannels
import ScriptEnv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import services.EventService
import services.ServiceManager

class ScriptEnvImpl(
    val sCtx: ScriptContext,
    override val scope: CoroutineScope,
    val sm: ServiceManager
): ScriptEnv {

    private val outputMutex = Mutex()

    init {
        sCtx.output.clear()
    }

    override fun run(vararg args: String): RunChannels {
        val process = ProcessBuilder(args.toList())
            .directory(sCtx.fileDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        return RunChannels(process, scope)
    }

    override suspend fun addOutput(line: String): Unit = outputMutex.withLock {
        sCtx.output.add(line)
        sm.get<EventService>().send(ScriptService.ScriptOutputUpdated(sCtx.filePath, sCtx.output))
    }

    override suspend fun exec(vararg args: String): Int {
        val rc = run(*args)

        scope.launch {
            for(line in rc.recvChannel) {
                addOutput(line)
            }
        }

        scope.launch {
            for(line in rc.errChannel) {
                addOutput(line)
            }
        }

        rc.waitForExited()

        return rc.process.exitValue()
    }
}