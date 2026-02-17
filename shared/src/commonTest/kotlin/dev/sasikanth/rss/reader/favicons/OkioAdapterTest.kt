package dev.sasikanth.rss.reader.favicons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import okio.Buffer as OkioBuffer
import okio.Source as OkioSource
import okio.Timeout

class OkioAdapterTest {

  @Test
  fun reading_from_raw_source_should_work() {
    val data = "Hello, World!".encodeToByteArray()
    val okioSource = object : OkioSource {
      private var read = false
      override fun read(sink: OkioBuffer, byteCount: Long): Long {
        if (read) return -1L
        sink.write(data)
        read = true
        return data.size.toLong()
      }
      override fun timeout(): Timeout = Timeout.NONE
      override fun close() {}
    }

    val rawSource = okioSource.asKotlinxIoRawSource()
    val sink = Buffer()
    val readBytes = rawSource.readAtMostTo(sink, 100L)

    assertEquals(data.size.toLong(), readBytes)
    assertEquals("Hello, World!", sink.readByteArray().decodeToString())
  }

  @Test
  fun reading_from_buffered_source_should_work() {
    val content = "Hello, Buffered World!"
    val okioBuffer = OkioBuffer().writeUtf8(content)

    // okio.Buffer implements BufferedSource
    val rawSource = okioBuffer.asKotlinxIoRawSource()
    val sink = Buffer()

    // Read partially
    val readBytes1 = rawSource.readAtMostTo(sink, 7L)
    assertEquals(7L, readBytes1)
    assertEquals("Hello, ", sink.readByteArray().decodeToString())

    // Read the rest
    val readBytes2 = rawSource.readAtMostTo(sink, 100L)
    assertEquals((content.length - 7).toLong(), readBytes2)
    assertEquals("Buffered World!", sink.readByteArray().decodeToString())
  }

  @Test
  fun reading_zero_bytes_should_return_zero() {
    val okioBuffer = OkioBuffer().writeUtf8("Some data")
    val rawSource = okioBuffer.asKotlinxIoRawSource()
    val sink = Buffer()

    val readBytes = rawSource.readAtMostTo(sink, 0L)
    assertEquals(0L, readBytes)
    assertEquals(0L, sink.size)
  }

  @Test
  fun reading_at_eof_should_return_minus_one() {
    val okioBuffer = OkioBuffer() // Empty
    val rawSource = okioBuffer.asKotlinxIoRawSource()
    val sink = Buffer()

    val readBytes = rawSource.readAtMostTo(sink, 10L)
    assertEquals(-1L, readBytes)
  }
}
