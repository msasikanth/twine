/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.premium

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable expect fun PaywallComponent(onDismiss: () -> Unit, modifier: Modifier = Modifier)

@Composable expect fun CustomerCenterComponent(onDismiss: () -> Unit, modifier: Modifier = Modifier)
