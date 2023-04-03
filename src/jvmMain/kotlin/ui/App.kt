package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.ServiceManager
import ui.edit.EditScript
import ui.scriptList.ActiveScriptList

@Composable
fun App(sm: ServiceManager) {

    var editingScript by remember { mutableStateOf("") }

    Row {
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight()
        ) {
            ActiveScriptList(sm, { editingScript = if(editingScript == it) "" else it })
        }

        if(editingScript.isNotBlank()) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                EditScript(sm, editingScript)
            }
        }
    }

}