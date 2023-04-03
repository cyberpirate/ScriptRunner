import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import scripting.ScriptService
import services.EventService
import services.ServiceManager
import ui.App

fun main() = application {

    val sm = remember { ServiceManager(Dispatchers.Main) }
    LaunchedEffect(Unit) {

        sm.add(EventService())
        sm.add(ScriptService())

        sm.initialize()

        while(isActive) {
            try {
                delay(100_000)
            } catch(_: CancellationException) { }
        }

        sm.teardown()
    }

    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme(colors = darkColors()) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colors.background,
                modifier = Modifier.fillMaxSize()
            ) {
                Box {
                    App(sm)
                }
            }
        }
    }
}
