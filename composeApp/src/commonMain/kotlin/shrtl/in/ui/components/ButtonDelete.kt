package shrtl.`in`.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shrtl.`in`.ui.Theme

@Composable
fun ButtonDelete(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Theme.Icons.Trash2,
            contentDescription = "Delete",
            modifier = Modifier.size(16.dp),
        )
    }
}
