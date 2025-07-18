/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.media3.demo.compose.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player

/**
 * Minimal playback controls for a [Player].
 *
 * Includes buttons for seeking to a previous/next items or playing/pausing the playback.
 */
@Composable
internal fun MinimalControls(player: Player, modifier: Modifier = Modifier) {
    val graySemiTransparentBackground = Color.Gray.copy(alpha = 0.1f)
    val modifierForIconButton =
        modifier
          .size(80.dp)
          .background(graySemiTransparentBackground, CircleShape)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviousButton(player, modifierForIconButton)
        PlayPauseButton(player, modifierForIconButton)
        NextButton(player, modifierForIconButton)
    }
}
