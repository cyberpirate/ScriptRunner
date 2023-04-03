package scripting

import ScriptInfo
import kotlinx.coroutines.Job
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class ScriptContext(val filePath: String) {
    val file: File get() = File(filePath)
    val fileDir get() = file.parentFile
    val running = AtomicBoolean(false)
    var currentJob: Job? = null
    var info: ScriptInfo? = null
    val errors = ArrayList<String>()
    val output = ArrayList<String>()
}