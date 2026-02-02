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

package dev.sasikanth.rss.reader.data.opml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("opml")
internal data class Opml(
  @XmlElement(value = false) val version: String?,
  @XmlElement(value = false) val head: Head?,
  val body: Body,
)

@Serializable @XmlSerialName("head") internal data class Head(@XmlElement val title: String)

@Serializable
@XmlSerialName("body")
internal data class Body(@XmlSerialName("outline") val outlines: List<Outline>)

@Serializable
@XmlSerialName("outline")
internal data class Outline(
  @XmlElement(value = false) val title: String?,
  @XmlElement(value = false) val text: String?,
  @XmlElement(value = false) val type: String?,
  @XmlElement(value = false) val xmlUrl: String?,
  @XmlSerialName("outline") val outlines: List<Outline>?,
)

sealed interface OpmlSource

data class OpmlFeed(val title: String?, val link: String) : OpmlSource

data class OpmlFeedGroup(val title: String, val feeds: List<OpmlFeed>) : OpmlSource
