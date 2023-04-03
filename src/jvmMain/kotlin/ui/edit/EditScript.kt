package ui.edit

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import scripting.ScriptContext
import scripting.ScriptService
import services.EventService
import services.ServiceManager
import ui.utils.ClosableList
import java.io.File

@Composable
fun EditScript(sm: ServiceManager, editingScript: String) {

    var errorsOpen by remember(editingScript) { mutableStateOf(false) }
    val scriptErrors = remember(editingScript) { mutableStateListOf<String>() }

    LaunchedEffect(editingScript) {
        scriptErrors.clear()
        scriptErrors.addAll(sm.get<ScriptService>().loadedScripts
            .firstOrNull({ it.filePath == editingScript })?.errors ?: emptyList())

        sm.get<EventService>().observe<ScriptService.ScriptErrorsUpdated> {
            if(it.scriptPath == editingScript) {
                scriptErrors.clear()
                scriptErrors.addAll(it.errors)
            }
        }
    }

    var outputOpen by remember(editingScript) { mutableStateOf(false) }
    val scriptOutput = remember(editingScript) { mutableStateListOf<String>() }

    LaunchedEffect(editingScript) {
        scriptOutput.clear()
        scriptOutput.addAll(sm.get<ScriptService>().loadedScripts
            .firstOrNull({ it.filePath == editingScript })?.output ?: emptyList())

        sm.get<EventService>().observe<ScriptService.ScriptOutputUpdated> {
            if(it.scriptPath == editingScript) {
                scriptOutput.clear()
                scriptOutput.addAll(it.output)
            }
        }
    }

    Surface(
        elevation = 10.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            val stateVertical = rememberScrollState(0)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
            ) {
                Column {
                    Text(
                        File(editingScript).name,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    ClosableList(errorsOpen, { errorsOpen = it }, "Errors", scriptErrors)
                    Spacer(modifier = Modifier.height(10.dp))
                    ClosableList(outputOpen, { outputOpen = it }, "Output", scriptOutput)
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
        }
    }
}