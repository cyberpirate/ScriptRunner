import kotlinx.coroutines.CoroutineScope

interface ScriptEnv {
    val scope: CoroutineScope
    fun run(vararg args: String): RunChannels
    suspend fun addOutput(line: String)
    suspend fun exec(vararg args: String): Int
}

class ScriptInfo(
    val name: String,
    val onRun: suspend ScriptEnv.() -> Unit,
)