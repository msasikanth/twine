/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.premium

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.util.DispatchersProvider
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class PremiumPaywallPresenter(
  dispatchersProvider: DispatchersProvider,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
      )
    }

  fun dispatch(event: PremiumPaywallEvent) {
    when (event) {
      PremiumPaywallEvent.GoBack -> goBack()
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(private val dispatchersProvider: DispatchersProvider) :
    InstanceKeeper.Instance {

    fun dispatch(event: PremiumPaywallEvent) {
      // no-op
    }
  }
}

internal typealias PremiumPaywallPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> PremiumPaywallPresenter
