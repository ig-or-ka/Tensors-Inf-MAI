import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import org.khronos.webgl.Int8Array


fun setCookie(cname: String, cvalue: String, exdays: String):Unit = js("""
    document.cookie = cname + "=" + cvalue + ";expires=" + exdays + ";path=/"
""")

fun timeToUTC(days: Int): String = js("{const d = new Date(); d.setTime(d.getTime() + (days*24*60*60*1000)); return d.toUTCString();}")
fun eraseCookie(cname: String): Unit = js("document.cookie = cname + \"=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/\"")
fun getCookiesLine():String = js("decodeURIComponent(document.cookie)")

fun getCookie(cookieName: String): String?{
    val cookies = getCookiesLine().split("; ")

    for(cookie in cookies){
        val cookSegments = cookie.split("=")
        if(cookSegments[0] == cookieName){
            return cookSegments[1]
        }
    }

    return null
}

fun saveFile(urlFile: String, fileName: String): Unit = js("{ let a = document.createElement(\"a\");\n" +
        "    a.style = \"display: none\";\n" +
        "    document.body.appendChild(a);\n" +
        "    a.href = urlFile;\n" +
        "    a.download = fileName;\n" +
        "    a.click();\n" +
        "    a.remove(); }")

fun toJsArrayBytes(data: Int8Array): String = js("{ const blobData = new Blob([data], { type: 'image/png' }); return window.URL.createObjectURL(blobData); }")


class WasmPlatform: Platform {
    override val httpClient = HttpClient(Js){
        install(ContentNegotiation){
            json()
        }
    }
    override val name: String = "Web with Kotlin/Wasm"
    override fun openImageFile(onWrongFormat:() -> Unit, onFileOpen:(dataBase64: String) -> Unit) {
        val elem = document.createElement("input") as HTMLInputElement
        elem.type = "file"
        elem.onchange = {
            if(elem.files!!.length > 0){
                val file = elem.files!!.item(0)!!
                if(file.name.lowercase().endsWith(".png")
                    || file.name.lowercase().endsWith(".jpg")
                    || file.name.lowercase().endsWith(".jpeg")){

                    val typeImage = if(file.name.lowercase().endsWith(".png")) "png" else "jpg"

                    currentJsCoroutineScope.launch {
                        val fileData = readFileAsByteArray(file)
                        onFileOpen("data:image/$typeImage;base64,${fileData.toBase64()}")
                    }
                }
                else{
                    onWrongFormat()
                }
            }
        }
        elem.click()
    }
    override fun saveImageFile(fileData: ByteArray){
        val urlImage = toJsArrayBytes(fileData.toJsArray())
        saveFile(urlImage, "upscale.png")
    }
    override fun getAccessToken(): String?{
        return getCookie("access_token")
    }

    override fun setAccessToken(token: String){
        setCookie("access_token", token, timeToUTC(365))
    }

    override fun eraseToken(){
        eraseCookie("access_token")
    }
}

actual fun getPlatform(): Platform = WasmPlatform()