package dev.sasikanth.rss.reader.opml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("opml")
internal data class Opml(
  @XmlElement(value = false) val version: String?,
  val head: Head,
  val body: Body
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
  @XmlSerialName("outline") val outlines: List<Outline>?
)

data class OpmlFeed(val title: String, val link: String)
