import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


fun uploadImage(errorDialog:(message: String) -> Unit, taskDone: (taskId: Int) -> Unit){
    platform.openImageFile({
        errorDialog("Ошибка|Формат файла не поддерживается!")
    }){photoBase64 ->
        currentCoroutineScope.launch {
            val res = upscalePhotoRequest(photoBase64)

            if(res == null || res < 0){
                errorDialog("Ошибка|Произошла ошибка запроса!")
            }
            else{
                taskDone(res)
            }
        }
    }
}


fun taskChecker(taskId: Int?, taskError:()->Unit, taskDone:(base64Photo: String)->Unit){
    if(taskId != null){
        currentCoroutineScope.launch {
            while (true){
                val res = getUpscaleTaskInfo(taskId)

                if(res != null){
                    if(res["status"] == "1"){
                        taskDone(res["output_photo"]!!)
                        break
                    }
                    else if(res["status"] == "2"){
                        taskError()
                        break
                    }
                }

                delay(1000)
            }
        }
    }
}


@OptIn(ExperimentalEncodingApi::class)
@Composable
fun UpscalePage(){
    var taskId by remember { mutableStateOf<Int?>(null) }
    var taskResult by remember { mutableStateOf<String?>(null) }
    var openDialogInfo by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if(taskId == null){
            Button(onClick = {
                uploadImage({
                    openDialogInfo = it
                }){
                    taskResult = null
                    taskId = it
                    //taskId = 52
                }
            }) {
                Text("Загрузить картинку")
            }

            var currentTaskResult = taskResult
            if(currentTaskResult != null){
                currentTaskResult = currentTaskResult.replace("\n", "")
                val photoData = Base64.decode(currentTaskResult)
                val imageEnc = Image.makeFromEncoded(photoData)
                val image = imageEnc.toComposeImageBitmap()

                Button(onClick = {
                    platform.saveImageFile(photoData)
                }) {
                    Text("Сохранить")
                }

                Image(
                    bitmap = image,
                    contentDescription = "QR",
                )
            }
        }
        else{
            Text("Обработка...")

            taskChecker(taskId, {
                taskId = null
                openDialogInfo = "Ошибка|Произошла ошибка при обработке изображения!"
            }){
                taskId = null
                taskResult = it
            }
        }

        if (openDialogInfo != "") {
            val openDialogInfoSegments = openDialogInfo.split("|")
            AlertDialog(
                onDismissRequest = { openDialogInfo = ""},
                title = { Text(text = openDialogInfoSegments[0]) },
                text = { Text(openDialogInfoSegments[1]) },
                confirmButton = {
                    Button({ openDialogInfo = "" }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}