import io.ktor.client.*

interface Platform {
    val name: String
    val httpClient: HttpClient
    fun openImageFile(onWrongFormat:() -> Unit, onFileOpen:(dataBase64: String) -> Unit)
    fun saveImageFile(fileData: ByteArray)
    fun getAccessToken(): String?
    fun setAccessToken(token: String)
    fun eraseToken()
}

expect fun getPlatform(): Platform