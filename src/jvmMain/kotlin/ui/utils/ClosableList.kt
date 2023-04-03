package ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.Cursor

@Composable
fun ClosableList(
    isOpen: Boolean,
    setOpen: (Boolean) -> Unit,
    name: String,
    lines: List<String>
) {
    Surface(
        elevation = 10.dp,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                .clickable { setOpen(!isOpen) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                Image(
                    painter =
                        if(isOpen) painterResource("imgs/expand_less.svg")
                        else painterResource("imgs/expand_more.svg"),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(
                        Color.White,
                        blendMode = BlendMode.SrcAtop
                    ),
                )
                Text(name)
                if(!isOpen) {
                    Spacer(modifier = Modifier.weight(1.0f))
                    Surface(
                        color = MaterialTheme.colors.secondary,
                        shape = CircleShape,
                        elevation = 10.dp
                    ) {
                        Text(lines.size.toString(), modifier = Modifier.size(25.dp), textAlign = TextAlign.Center)
                    }
                }
            }

            if(isOpen) {
                Divider(thickness = 2.dp)

                if(lines.isNotEmpty()) {
                    lines.forEachIndexed({ idx, it ->
                        Text(it, modifier = Modifier.padding(horizontal = 10.dp))
                        if (idx != lines.lastIndex)
                            Divider()
                    })
                } else {
                    Text("Empty", modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}