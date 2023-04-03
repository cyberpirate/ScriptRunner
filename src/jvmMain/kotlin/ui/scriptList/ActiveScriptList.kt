package ui.scriptList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import scripting.ScriptContext
import scripting.ScriptService
import services.EventService
import services.ServiceManager

@Composable
fun ActiveScriptList(sm: ServiceManager, onEditScript: (String) -> Unit) {

    val scriptList = remember { mutableStateListOf<ScriptContext>() }

    LaunchedEffect(Unit) {
        scriptList.addAll(sm.get<ScriptService>().loadedScripts)

        sm.get<EventService>().observe<ScriptService.ScriptListUpdated> {
            scriptList.clear()
            scriptList.addAll(it.scripts)
        }
    }

    if(scriptList.isNotEmpty()) {
        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = 10.dp),
            columns = GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = {
//                items(scriptList.size, { scriptList[it].filePath }) {
                items(scriptList.size) {
                    ScriptButton(sm, scriptList[it], { onEditScript(scriptList[it].filePath)})
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No scripts loaded",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

}