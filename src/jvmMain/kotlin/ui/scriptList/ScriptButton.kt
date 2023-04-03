package ui.scriptList

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.Cursor
import scripting.ScriptContext
import scripting.ScriptService
import services.EventService
import services.ServiceManager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScriptButton(sm: ServiceManager, scriptContext: ScriptContext, onEditClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }

    var isRunning by remember(scriptContext.filePath) { mutableStateOf(scriptContext.running.get()) }

    LaunchedEffect(scriptContext.filePath) {
        sm.get<EventService>().observe<ScriptService.ScriptRunning> {
            if(it.scriptPath == scriptContext.filePath)
                isRunning = it.running
        }
    }

    Box(
        modifier = Modifier.onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false },
    ) {
        val buttonEnabled = scriptContext.info != null && !isRunning
        Button(
            onClick = { sm.get<ScriptService>().runScript(scriptContext) },
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxSize().let({
                if(buttonEnabled) it.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                else it
            }),
            contentPadding = PaddingValues()
        ) {
            if(!isRunning) {
                Text(scriptContext.info?.name ?: scriptContext.file.name)
            } else {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if(isHovered) {
            Box(
                modifier = Modifier.width(30.dp).height(30.dp).align(Alignment.TopEnd)
                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            ) {
                Image(
                    painter = painterResource("imgs/circle.svg"),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(
                        Color.White,
                        blendMode = BlendMode.SrcAtop
                    ),
                    modifier = Modifier.matchParentSize().clickable(onClick = onEditClick)
                )
                Image(
                    painter = painterResource("imgs/edit.svg"),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(
                        Color.Black,
                        blendMode = BlendMode.SrcAtop
                    ),
                    modifier = Modifier.matchParentSize().padding(5.dp)
                )
            }
        }
    }
}