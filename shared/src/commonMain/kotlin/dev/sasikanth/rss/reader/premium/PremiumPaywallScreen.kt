/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sasikanth.rss.reader.billing.PackageType
import dev.sasikanth.rss.reader.billing.TwinePackage
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.FormatLineSpacing
import dev.sasikanth.rss.reader.resources.icons.LayoutSimple
import dev.sasikanth.rss.reader.resources.icons.Palette
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.Sync
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.restrictContentWidth
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.premiumPaywallBestValue
import twine.shared.generated.resources.premiumPaywallBilledAnnually
import twine.shared.generated.resources.premiumPaywallBilledMonthly
import twine.shared.generated.resources.premiumPaywallCancelAnytime
import twine.shared.generated.resources.premiumPaywallFeature1Desc
import twine.shared.generated.resources.premiumPaywallFeature1Title
import twine.shared.generated.resources.premiumPaywallFeature2Desc
import twine.shared.generated.resources.premiumPaywallFeature2Title
import twine.shared.generated.resources.premiumPaywallFeature3Desc
import twine.shared.generated.resources.premiumPaywallFeature3Title
import twine.shared.generated.resources.premiumPaywallFeature4Desc
import twine.shared.generated.resources.premiumPaywallFeature4Title
import twine.shared.generated.resources.premiumPaywallGetTwinePro
import twine.shared.generated.resources.premiumPaywallPayOnce
import twine.shared.generated.resources.premiumPaywallPrivacyPolicy
import twine.shared.generated.resources.premiumPaywallRestorePurchase
import twine.shared.generated.resources.premiumPaywallSubtitle
import twine.shared.generated.resources.premiumPaywallTermsOfService
import twine.shared.generated.resources.premiumPaywallTitle
import twine.shared.generated.resources.premiumPaywallYouArePro

@Composable
fun PremiumPaywallScreen(
  packages: List<TwinePackage>,
  inProgress: Boolean,
  hasPremium: Boolean,
  isFromOnboarding: Boolean = false,
  onPurchase: (String) -> Unit,
  onRestore: () -> Unit,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedPackageId by
    remember(packages) {
      mutableStateOf(
        packages.find { it.packageType == PackageType.LIFETIME }?.id ?: packages.firstOrNull()?.id
      )
    }

  val uriHandler = LocalUriHandler.current

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = AppTheme.colorScheme.surface,
    topBar = {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(AppTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val icon =
          if (isFromOnboarding) {
            TwineIcons.Close
          } else {
            TwineIcons.ArrowBack
          }

        CircularIconButton(
          modifier = Modifier.padding(start = 12.dp),
          icon = icon,
          label = stringResource(Res.string.buttonGoBack),
          onClick = goBack,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          TextButton(onClick = onRestore, enabled = !inProgress) {
            Text(
              text = stringResource(Res.string.premiumPaywallRestorePurchase),
              style = MaterialTheme.typography.labelLarge,
              color = AppTheme.colorScheme.primary,
            )
          }
        }
      }
    },
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      Column(
        modifier =
          Modifier.restrictContentWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 280.dp), // Space for tall bottom sticky section
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(Modifier.height(16.dp))

        // Hero Icon
        Box(
          modifier =
            Modifier.size(72.dp)
              .background(AppTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = TwineIcons.StarShine,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = AppTheme.colorScheme.primary,
          )
        }

        Spacer(Modifier.height(24.dp))

        Text(
          text = stringResource(Res.string.premiumPaywallTitle),
          style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
          color = AppTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
          text = stringResource(Res.string.premiumPaywallSubtitle),
          style = MaterialTheme.typography.bodyLarge,
          color = AppTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        // Features List
        Column(
          verticalArrangement = Arrangement.spacedBy(24.dp),
          modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        ) {
          FeatureItem(
            icon = TwineIcons.FormatLineSpacing,
            title = stringResource(Res.string.premiumPaywallFeature1Title),
            description =
              stringResource(Res.string.premiumPaywallFeature1Desc, Constants.MAX_FREE_FEEDS),
          )
          FeatureItem(
            icon = TwineIcons.Sync,
            title = stringResource(Res.string.premiumPaywallFeature2Title),
            description = stringResource(Res.string.premiumPaywallFeature2Desc),
          )
          FeatureItem(
            icon = TwineIcons.Palette,
            title = stringResource(Res.string.premiumPaywallFeature3Title),
            description = stringResource(Res.string.premiumPaywallFeature3Desc),
          )
          FeatureItem(
            icon = TwineIcons.LayoutSimple,
            title = stringResource(Res.string.premiumPaywallFeature4Title),
            description = stringResource(Res.string.premiumPaywallFeature4Desc),
          )
        }

        Spacer(Modifier.height(16.dp))
      }

      // Bottom Sticky Section
      Column(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(40.dp)
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, AppTheme.colorScheme.surface)
                )
              )
        )

        Column(
          modifier =
            Modifier.widthIn(max = Constants.MAX_CONTENT_WIDTH)
              .fillMaxWidth()
              .background(AppTheme.colorScheme.surface)
              .padding(horizontal = 24.dp)
              .padding(bottom = 24.dp)
        ) {
          // Packages
          if (packages.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = AppTheme.colorScheme.primary)
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              packages.forEach { twinePackage ->
                val isSelected = twinePackage.id == selectedPackageId
                PackageCard(
                  twinePackage = twinePackage,
                  isSelected = isSelected,
                  modifier = Modifier.weight(1f),
                  onClick = { selectedPackageId = twinePackage.id },
                )
              }
            }
          }

          Spacer(Modifier.height(24.dp))

          Button(
            onClick = { selectedPackageId?.let { onPurchase(it) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = AppTheme.colorScheme.primary,
                contentColor = AppTheme.colorScheme.onPrimary,
              ),
            enabled = !inProgress && selectedPackageId != null,
          ) {
            if (inProgress) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text =
                  if (hasPremium) stringResource(Res.string.premiumPaywallYouArePro)
                  else stringResource(Res.string.premiumPaywallGetTwinePro),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              )
            }
          }

          Spacer(Modifier.height(8.dp))

          Text(
            text = stringResource(Res.string.premiumPaywallCancelAnytime),
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
          )

          Spacer(Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(
              colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.primary),
              onClick = { uriHandler.openUri("https://www.sasikanth.dev/reader-privacy-policy/") },
            ) {
              Text(
                stringResource(Res.string.premiumPaywallPrivacyPolicy),
                style = MaterialTheme.typography.labelMedium,
              )
            }
            TextButton(
              colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.primary),
              onClick = {
                uriHandler.openUri(
                  "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/"
                )
              },
            ) {
              Text(
                stringResource(Res.string.premiumPaywallTermsOfService),
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String) {
  Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier =
        Modifier.size(40.dp)
          .background(AppTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp),
      )
    }
    Spacer(Modifier.width(16.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun PackageCard(
  twinePackage: TwinePackage,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val borderColor =
    if (isSelected) AppTheme.colorScheme.primary else AppTheme.colorScheme.surfaceContainerHigh
  val backgroundColor =
    if (isSelected) AppTheme.colorScheme.surfaceContainerHigh else Color.Transparent
  val isLifetime = twinePackage.packageType == PackageType.LIFETIME

  Box(modifier = modifier.padding(top = 12.dp)) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(backgroundColor)
          .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
          .clickable(onClick = onClick)
          .padding(vertical = 16.dp, horizontal = 4.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = twinePackage.period.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) AppTheme.colorScheme.primary else AppTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          maxLines = 1,
        )
        Spacer(Modifier.height(8.dp))
        Text(
          text = twinePackage.priceString,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = AppTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text =
            when (twinePackage.packageType) {
              PackageType.MONTHLY -> stringResource(Res.string.premiumPaywallBilledMonthly)
              PackageType.ANNUAL -> stringResource(Res.string.premiumPaywallBilledAnnually)
              PackageType.LIFETIME -> stringResource(Res.string.premiumPaywallPayOnce)
              else -> ""
            },
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = AppTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          maxLines = 1,
        )
      }
    }

    if (isLifetime) {
      Box(
        modifier =
          Modifier.align(Alignment.TopCenter)
            .offset(y = (-10).dp)
            .background(AppTheme.colorScheme.primary, RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = stringResource(Res.string.premiumPaywallBestValue),
          style =
            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
          color = AppTheme.colorScheme.onPrimary,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PremiumPaywallScreenPreview() {
  AppTheme {
    PremiumPaywallScreen(
      packages =
        listOf(
          TwinePackage("1", PackageType.MONTHLY, "$0.99", "1 Month"),
          TwinePackage("2", PackageType.ANNUAL, "$9.99", "12 Months"),
          TwinePackage("3", PackageType.LIFETIME, "$24.99", "Lifetime"),
        ),
      inProgress = false,
      hasPremium = false,
      onPurchase = {},
      onRestore = {},
      goBack = {},
    )
  }
}
