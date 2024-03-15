package org.schabi.newpipe.error

import android.os.Parcel
import kotlinx.parcelize.Parceler
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * The binder can not handle to much data and throws TransactionTooLargeException.
 *
 * This Parceler tries to skip this fact with temporary gzip the data. Seems to
 * work -- but sending over eMail still needs some truncating
 * see {@link BraveErrorActivityHelper}.
 */
object BraveErrorInfoTracesParceler : Parceler<Array<String>> {
    override fun create(parcel: Parcel): Array<String> {
        val byteArray = ByteArray(parcel.readInt())
        parcel.readByteArray(byteArray)
        val unzipped = ungzip(byteArray)

        return unzipped.split(";").toTypedArray()
    }

    override fun Array<String>.write(parcel: Parcel, flags: Int) {
        val result = this.reduce { result, nr -> "$result; $nr" }
        val zipped = gzip(result)
        parcel.writeInt(zipped.size)
        parcel.writeByteArray(zipped)
    }

    private fun gzip(content: String): ByteArray {
        val byteOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteOutputStream)
            .bufferedWriter(StandardCharsets.UTF_8).use { it.write(content) }

        return byteOutputStream.toByteArray()
    }

    private fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream())
            .bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}
