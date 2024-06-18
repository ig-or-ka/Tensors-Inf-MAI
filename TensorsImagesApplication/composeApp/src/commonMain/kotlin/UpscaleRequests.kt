import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

val platform = getPlatform()
const val serverAddress = "http://localhost:8000/upscale"

suspend fun upscalePhotoRequest(photoBase64: String): Int?{
    @Serializable
    data class UpscaleData(val photo: String, val token: String?)

    try{
        with(platform.httpClient.post("$serverAddress/upscale_photo"){
            contentType(ContentType.Application.Json)
            setBody(UpscaleData(photoBase64, platform.getAccessToken()))
        }){
            val resData = this.body<Map<String, String>>()
            if(status == HttpStatusCode.OK){
                return resData["task_id"]!!.toInt()
            }
            else{
                return -status.value
            }
        }
    }
    catch (_:Exception){
        return null
    }
}


suspend fun getUpscaleTaskInfo(taskId: Int): Map<String, String>?{
    @Serializable
    data class UpscaleTaskData(val task_id: Int)

    try{
        with(platform.httpClient.post("$serverAddress/upscale_task_info"){
            contentType(ContentType.Application.Json)
            setBody(UpscaleTaskData(taskId))
        }){
            if(status == HttpStatusCode.OK){
                return body()
            }
            else{
                return null
            }
        }
    }
    catch (_:Exception){
        return null
    }
}

@Serializable
data class UpscaleTaskInfo(val task_id: Int, val status: Int, val time: String)

suspend fun getUpscaleTasksList(): List<UpscaleTaskInfo>?{
    @Serializable
    data class UpscaleTasksListData(val token: String?)

    try{
        with(platform.httpClient.post("$serverAddress/user_tasks_list"){
            contentType(ContentType.Application.Json)
            setBody(UpscaleTasksListData(platform.getAccessToken()))
        }){
            if(status == HttpStatusCode.OK){
                return body()
            }
            else{
                return null
            }
        }
    }
    catch (_:Exception){
        return null
    }
}


suspend fun loginUser(username: String, password: String, logIn:Boolean=true): String?{
    @Serializable
    data class LoginData(val username: String, val password: String)

    try{
        with(platform.httpClient.post("$serverAddress/${if(logIn) "login" else "signup"}"){
            contentType(ContentType.Application.Json)
            setBody(LoginData(username, password))
        }){
            val resData = this.body<Map<String, String>>()
            if(status == HttpStatusCode.OK){
                platform.setAccessToken(resData["access_token"]!!)
                return null
            }
            else{
                return if (resData["msg"] == null) "Error" else resData["msg"]
            }
        }
    }
    catch (_:Exception){
        return "Ошибка подключения к серверу!"
    }
}