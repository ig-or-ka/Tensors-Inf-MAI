import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.ComposeViewport
import io.ktor.util.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Image
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.File
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

var currentJsCoroutineScope = CoroutineScope(Dispatchers.Default)

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64(): String = Base64.encode(this)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

suspend fun readFileAsByteArray(file: File): ByteArray = suspendCoroutine {
    val reader = FileReader()
    reader.onload = {loadEvt ->
        try {
            val eventFileReader = loadEvt.target?.let { it as FileReader }!!
            val content = eventFileReader.result as ArrayBuffer
            val array = Uint8Array(content)

            val fileByteArray = ByteArray(array.length)
            for (i in 0 until array.length) {
                fileByteArray[i] = array[i]
            }
            it.resumeWith(Result.success(fileByteArray))
        } catch (e: Throwable) {
            it.resumeWithException(e)
        }
    }
    reader.readAsArrayBuffer(file)
}