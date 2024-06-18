import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64(): String = Base64.encode(this)

class JVMPlatform: Platform {
    override val httpClient = HttpClient(OkHttp){
        install(ContentNegotiation){
            json()
        }
    }
    override val name: String = "Java ${System.getProperty("java.version")}"
    override fun openImageFile(onWrongFormat:() -> Unit, onFileOpen:(dataBase64: String) -> Unit) {
        val filePath = chooseFile(System.getProperty("user.dir"), "png,jpg", "Hello")

        if(filePath != null){
            if(filePath.lowercase().endsWith(".png")
                || filePath.lowercase().endsWith(".jpg")
                || filePath.lowercase().endsWith(".jpeg")){

                val typeImage = if(filePath.lowercase().endsWith(".png")) "png" else "jpg"

                val file = File(filePath)
                val baseData = file.readBytes().toBase64()
                onFileOpen("data:image/$typeImage;base64,$baseData")
            }
            else{
                onWrongFormat()
            }
        }
    }
    override fun saveImageFile(fileData: ByteArray){
        val filePath = chooseFile(System.getProperty("user.dir"), "png", "Hello", true)

        if(filePath != null){
            val file = File(filePath)
            file.writeBytes(fileData)
        }
    }
    override fun getAccessToken(): String?{
        val file = File("access_token.txt")
        try {
            val scanner = Scanner(file)
            if(scanner.hasNextLine()){
                val line = scanner.nextLine()
                scanner.close()
                return line
            }
        }
        catch(_: FileNotFoundException){}
        return null
    }

    override fun setAccessToken(token: String){
        val file = File("access_token.txt")
        val writer = FileWriter(file)
        writer.write(token)
        writer.close()
    }

    override fun eraseToken(){
        val file = File("access_token.txt")
        file.delete()
    }
}

actual fun getPlatform(): Platform = JVMPlatform()