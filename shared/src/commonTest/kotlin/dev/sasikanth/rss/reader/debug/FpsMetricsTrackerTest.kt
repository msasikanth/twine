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
package dev.sasikanth.rss.reader.debug

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FpsMetricsTrackerTest {

  private val frame60HzNanos = 16_666_667L
  private val frame90HzNanos = 11_111_111L
  private val frame120HzNanos = 8_333_333L

  @Test
  fun steady60FpsShouldReport60FpsGood() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    assertNull(tracker.onFrame(timeNanos))

    var latest: FpsMetrics? = null
    repeat(18) {
      timeNanos += frame60HzNanos
      latest = tracker.onFrame(timeNanos)
    }

    val metrics = assertNotNull(latest)
    assertEquals(60, metrics.fps)
    assertEquals(FpsStatus.Good, metrics.status)
  }

  @Test
  fun steady120FpsShouldReport120FpsGood() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    assertNull(tracker.onFrame(timeNanos))

    var latest: FpsMetrics? = null
    repeat(37) {
      timeNanos += frame120HzNanos
      latest = tracker.onFrame(timeNanos)
    }

    val metrics = assertNotNull(latest)
    assertEquals(120, metrics.fps)
    assertEquals(FpsStatus.Good, metrics.status)
  }

  @Test
  fun jankSpikeShouldReportPoorAndMildDropShouldReportWarning() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)

    timeNanos += 45_000_000L
    assertNull(tracker.onFrame(timeNanos))

    var latest: FpsMetrics? = null
    repeat(18) {
      timeNanos += frame60HzNanos
      latest = tracker.onFrame(timeNanos) ?: latest
    }

    val poorMetrics = assertNotNull(latest)
    assertEquals(FpsStatus.Poor, poorMetrics.status)

    var mildLatest: FpsMetrics? = null
    timeNanos += 30_000_000L
    tracker.onFrame(timeNanos)
    repeat(18) {
      timeNanos += frame60HzNanos
      mildLatest = tracker.onFrame(timeNanos) ?: mildLatest
    }

    val warningMetrics = assertNotNull(mildLatest)
    assertEquals(FpsStatus.Warning, warningMetrics.status)
  }

  @Test
  fun backgroundPauseGapShouldResetWindowWithoutReportingStaleDrop() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)

    repeat(5) {
      timeNanos += frame60HzNanos
      tracker.onFrame(timeNanos)
    }

    timeNanos += 2_000_000_000L
    assertNull(tracker.onFrame(timeNanos))

    var latest: FpsMetrics? = null
    repeat(18) {
      timeNanos += frame60HzNanos
      latest = tracker.onFrame(timeNanos)
    }

    val metrics = assertNotNull(latest)
    assertEquals(60, metrics.fps)
    assertEquals(FpsStatus.Good, metrics.status)
  }

  @Test
  fun zeroAndNegativeFrameDeltasAreIgnored() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    val start = 1_000_000_000L
    assertNull(tracker.onFrame(start))
    assertNull(tracker.onFrame(start))
    assertNull(tracker.onFrame(start - 1_000_000L))
  }

  @Test
  fun steady90FpsShouldReport90FpsGood() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    assertNull(tracker.onFrame(timeNanos))

    var latest: FpsMetrics? = null
    repeat(28) {
      timeNanos += frame90HzNanos
      latest = tracker.onFrame(timeNanos) ?: latest
    }

    val metrics = assertNotNull(latest)
    assertEquals(90, metrics.fps)
    assertEquals(FpsStatus.Good, metrics.status)
  }

  @Test
  fun jankSpikeAt120HzShouldReportWarningAndPoor() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)

    timeNanos += 17_000_000L
    tracker.onFrame(timeNanos)
    var warningLatest: FpsMetrics? = null
    repeat(36) {
      timeNanos += frame120HzNanos
      warningLatest = tracker.onFrame(timeNanos) ?: warningLatest
    }

    assertEquals(FpsStatus.Warning, assertNotNull(warningLatest).status)

    timeNanos += 26_000_000L
    tracker.onFrame(timeNanos)
    var poorLatest: FpsMetrics? = null
    repeat(36) {
      timeNanos += frame120HzNanos
      poorLatest = tracker.onFrame(timeNanos) ?: poorLatest
    }

    assertEquals(FpsStatus.Poor, assertNotNull(poorLatest).status)
  }

  @Test
  fun jankSpikeAt90HzShouldReportWarningAndPoor() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)

    timeNanos += 22_000_000L
    tracker.onFrame(timeNanos)
    var warningLatest: FpsMetrics? = null
    repeat(28) {
      timeNanos += frame90HzNanos
      warningLatest = tracker.onFrame(timeNanos) ?: warningLatest
    }

    assertEquals(FpsStatus.Warning, assertNotNull(warningLatest).status)

    timeNanos += 30_000_000L
    tracker.onFrame(timeNanos)
    var poorLatest: FpsMetrics? = null
    repeat(28) {
      timeNanos += frame90HzNanos
      poorLatest = tracker.onFrame(timeNanos) ?: poorLatest
    }

    assertEquals(FpsStatus.Poor, assertNotNull(poorLatest).status)
  }

  @Test
  fun ltpoStepDownFrom120HzToSteady60HzShouldRemainGood() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)

    var firstWindow: FpsMetrics? = null
    repeat(37) {
      timeNanos += frame120HzNanos
      firstWindow = tracker.onFrame(timeNanos) ?: firstWindow
    }
    assertEquals(FpsStatus.Good, assertNotNull(firstWindow).status)

    var secondWindow: FpsMetrics? = null
    repeat(19) {
      timeNanos += frame60HzNanos
      secondWindow = tracker.onFrame(timeNanos) ?: secondWindow
    }
    val steady60 = assertNotNull(secondWindow)
    assertEquals(60, steady60.fps)
    assertEquals(FpsStatus.Good, steady60.status)
  }

  @Test
  fun sparseInteractionIdleGapWithShortFrameDurationShouldResetWithoutFalseDrop() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L)

    timeNanos += 250_000_000L
    assertNull(tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L))

    var latest: FpsMetrics? = null
    repeat(37) {
      timeNanos += frame120HzNanos
      latest = tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L) ?: latest
    }

    val metrics = assertNotNull(latest)
    assertEquals(120, metrics.fps)
    assertEquals(FpsStatus.Good, metrics.status)
  }

  @Test
  fun realLongStallWithLargeFrameDurationShouldReportPoor() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L)

    timeNanos += 200_000_000L
    assertNull(tracker.onFrame(timeNanos, frameDurationNanos = 190_000_000L))

    var latest: FpsMetrics? = null
    repeat(20) {
      timeNanos += frame120HzNanos
      latest = tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L) ?: latest
    }

    assertEquals(FpsStatus.Poor, assertNotNull(latest).status)
  }

  @Test
  fun pipelinedSlowFrameDurationAt120HzShouldReportWarningAndPoor() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L)

    timeNanos += frame120HzNanos
    tracker.onFrame(timeNanos, frameDurationNanos = 16_000_000L)
    var warningLatest: FpsMetrics? = null
    repeat(36) {
      timeNanos += frame120HzNanos
      warningLatest = tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L) ?: warningLatest
    }

    assertEquals(FpsStatus.Warning, assertNotNull(warningLatest).status)

    timeNanos += frame120HzNanos
    tracker.onFrame(timeNanos, frameDurationNanos = 25_000_000L)
    var poorLatest: FpsMetrics? = null
    repeat(36) {
      timeNanos += frame120HzNanos
      poorLatest = tracker.onFrame(timeNanos, frameDurationNanos = 4_000_000L) ?: poorLatest
    }

    assertEquals(FpsStatus.Poor, assertNotNull(poorLatest).status)
  }

  @Test
  fun resetShouldClearAccumulatedWindowState() {
    val tracker = FpsMetricsTracker(sampleWindowNanos = 300_000_000L)
    var timeNanos = 1_000_000_000L
    tracker.onFrame(timeNanos)
    repeat(10) {
      timeNanos += frame60HzNanos
      tracker.onFrame(timeNanos)
    }

    tracker.reset()

    timeNanos += frame60HzNanos
    assertNull(tracker.onFrame(timeNanos))
  }

  @Test
  fun formatOneDecimalFormatsCleanly() {
    assertEquals("16.7", formatOneDecimal(16.66f))
    assertEquals("8.0", formatOneDecimal(8.0f))
    assertEquals("0.0", formatOneDecimal(0f))
    assertEquals("33.3", formatOneDecimal(33.34f))
  }
}
