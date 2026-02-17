package dev.sasikanth.rss.reader.favicons

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import okio.Buffer as OkioBuffer
import okio.Source as OkioSource
import okio.buffer

class FavIconFetcherTest {

  @Test
  fun asKotlinxIoRawSource_should_read_data_from_okio_source() {
    val data = "Hello, World!".encodeToByteArray()
    val okioSource: OkioSource = OkioBuffer().apply { write(data) }

    val kxRawSource = okioSource.asKotlinxIoRawSource()
    val sink = Buffer()

    val read = kxRawSource.readAtMostTo(sink, data.size.toLong())

    assertEquals(data.size.toLong(), read)
    assertContentEquals(data, sink.readByteArray())
  }

  @Test
  fun asKotlinxIoRawSource_should_work_with_buffered_source() {
    val data = "Hello, World!".encodeToByteArray()
    val okioSource = OkioBuffer().apply { write(data) }.buffer()

    val kxRawSource = okioSource.asKotlinxIoRawSource()
    val sink = Buffer()

    val read = kxRawSource.readAtMostTo(sink, data.size.toLong())

    assertEquals(data.size.toLong(), read)
    assertContentEquals(data, sink.readByteArray())
  }

  @Test
  fun asKotlinxIoRawSource_should_handle_multiple_reads() {
    val data = "Hello, World!".encodeToByteArray()
    val okioSource: OkioSource = OkioBuffer().apply { write(data) }

    val kxRawSource = okioSource.asKotlinxIoRawSource()
    val sink = Buffer()

    val read1 = kxRawSource.readAtMostTo(sink, 5)
    assertEquals(5L, read1)
    assertContentEquals("Hello".encodeToByteArray(), sink.readByteArray())

    val read2 = kxRawSource.readAtMostTo(sink, 10)
    assertEquals(8L, read2)
    assertContentEquals(", World!".encodeToByteArray(), sink.readByteArray())

    val read3 = kxRawSource.readAtMostTo(sink, 10)
    assertEquals(-1L, read3)
  }
}
