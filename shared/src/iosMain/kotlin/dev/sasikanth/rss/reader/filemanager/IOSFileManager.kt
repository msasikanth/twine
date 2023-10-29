/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.filemanager

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeXML
import platform.darwin.NSObject

@Inject
@AppScope
@OptIn(ExperimentalForeignApi::class)
class IOSFileManager(
  private val dispatchersProvider: DispatchersProvider,
  private val viewControllerProvider: () -> UIViewController
) : FileManager {

  private val mainScope = CoroutineScope(dispatchersProvider.main)

  @Suppress("CAST_NEVER_SUCCEEDS")
  override suspend fun save(name: String, content: String) {
    suspendCoroutine { continuation ->
      if (content.isNotBlank()) {
        val delegate =
          DocumentPickerDelegate(
            didPickDocument = { url ->
              (content as NSString).writeToFile(
                path = "${url.path}/$name",
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
              )

              continuation.resume(Unit)
            },
            cancelledDocumentPicker = { continuation.resume(Unit) }
          )

        presentDocumentPicker(type = UTTypeFolder, delegate = delegate)
      }
    }
  }

  override suspend fun read(): String? {
    return suspendCoroutine { continuation ->
      val delegate =
        DocumentPickerDelegate(
          didPickDocument = { url ->
            val content =
              NSString.stringWithContentsOfFile(
                path = url.path!!,
                encoding = NSUTF8StringEncoding,
                error = null
              )

            continuation.resume(content)
          },
          cancelledDocumentPicker = { continuation.resume(null) }
        )
      presentDocumentPicker(type = UTTypeXML, delegate = delegate)
    }
  }

  private fun presentDocumentPicker(type: UTType, delegate: UIDocumentPickerDelegateProtocol) {
    mainScope.launch {
      val documentPickerViewController =
        UIDocumentPickerViewController(forOpeningContentTypes = listOf(type))
      documentPickerViewController.delegate = delegate
      documentPickerViewController.allowsMultipleSelection = false
      documentPickerViewController.modalPresentationStyle = UIModalPresentationPageSheet

      viewControllerProvider().presentViewController(documentPickerViewController, true, null)
    }
  }
}

private class DocumentPickerDelegate(
  private val didPickDocument: (url: NSURL) -> Unit,
  private val cancelledDocumentPicker: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

  override fun documentPicker(
    controller: UIDocumentPickerViewController,
    didPickDocumentAtURL: NSURL
  ) {
    try {
      val scoped = didPickDocumentAtURL.startAccessingSecurityScopedResource()
      if (scoped) {
        didPickDocument(didPickDocumentAtURL)
      }
    } catch (e: Exception) {
      cancelledDocumentPicker()
      throw e
    } finally {
      didPickDocumentAtURL.stopAccessingSecurityScopedResource()
    }
  }

  override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
    cancelledDocumentPicker()
  }
}
