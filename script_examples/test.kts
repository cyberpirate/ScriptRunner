import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

ScriptInfo(
    name = "asdf",
    onRun = {
        exec("curl", "https://google.com")
    }
)