package de.hsb.vibeify.ui.components.songCard

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class MenuOption(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null
)

@Composable
fun SongCardMenu(
    modifier: Modifier = Modifier,
    menuIcon: ImageVector = Icons.Default.MoreVert,
    menuIconContentDescription: String = "Weitere Optionen",
    menuOptions: List<MenuOption> = emptyList(),
    onMenuIconClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = {
            onMenuIconClick?.invoke() ?: run { expanded = !expanded }
        }) {
            Icon(menuIcon, contentDescription = menuIconContentDescription)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.text) },
                    leadingIcon =  {
                        option.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        option.onClick()
                        expanded = false
                    }
                )
            }
        }
    }
}