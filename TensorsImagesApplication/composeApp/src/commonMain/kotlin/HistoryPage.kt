import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


fun getListImages(onError:() -> Unit, onDone:(list: List<UpscaleTaskInfo>) -> Unit){
    currentCoroutineScope.launch {
        val list = getUpscaleTasksList()

        if(list == null){
            onError()
        }
        else{
            onDone(list)
        }
    }
}


@Composable
fun HistoryItem(taskInfo: UpscaleTaskInfo, onClick:() -> Unit){
    Column(modifier = Modifier.clickable { if(taskInfo.status == 1) onClick() }.fillMaxWidth()){
        Text("ID: ${taskInfo.task_id}", fontSize = 20.sp)
        Text(taskInfo.time.split(".")[0])

        if(taskInfo.status == 0){
            Text("В процессе...")
        }
        else if(taskInfo.status == 1){
            Text("Успешно")
        }
        else{
            Text("Ошибка")
        }
    }
}


@OptIn(ExperimentalEncodingApi::class)
@Composable
fun HistoryPage(){
    var openDialogInfo by remember { mutableStateOf("") }
    var taskList by remember { mutableStateOf<List<UpscaleTaskInfo>?>(null) }
    val scrollState = rememberScrollState()

    var currentTaskId by remember { mutableStateOf(-1) }
    var currentTaskRes by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.padding(16.dp)){
        Box(modifier = Modifier.width(300.dp).verticalScroll(scrollState).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val currentList = taskList

                if(currentList != null){
                    for(item in currentList){
                        HistoryItem(item){
                            currentTaskId = item.task_id
                            taskChecker(item.task_id, {}){
                                if(currentTaskId == item.task_id){
                                    currentTaskRes = it
                                }
                            }
                        }
                    }
                }
            }
        }

        Box {
           Column {
               var currentTaskResult = currentTaskRes
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

    if(taskList == null){
        getListImages({
            openDialogInfo = "Ошибка|Произошла ошибка запроса!"
            taskList = listOf()
        }){
            taskList = it
        }
    }
}