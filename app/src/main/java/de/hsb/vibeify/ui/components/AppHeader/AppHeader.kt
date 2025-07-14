package de.hsb.vibeify.ui.components.AppHeader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.core.AuthViewModel

/**
 * AppHeader is a composable function that displays a header with a back button.
 *
 * @param scrollBehavior The scroll behavior for the top app bar.
 * @param icon The icon to be displayed in the header, default is back arrow.
 * @param onBackClick Callback function to be invoked when the back button is clicked.
 * @param authViewModel The AuthViewModel instance, provided by Hilt.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ArrowBackIosNew,
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { onBackClick() },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Back",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}