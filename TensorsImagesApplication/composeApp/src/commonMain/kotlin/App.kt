import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.ui.tooling.preview.Preview

var currentCoroutineScope = CoroutineScope(Dispatchers.Default)


@Composable
@Preview
fun App() {
    val accessToken = platform.getAccessToken()
    var isUserAuthed by remember { mutableStateOf(accessToken != null) }

    MaterialTheme {
        if(isUserAuthed){
            AuthedUser{
                platform.eraseToken()
                isUserAuthed = false
            }
        }
        else{
            AuthPage{
                isUserAuthed = true
            }
        }
    }
}

@Composable
fun AuthedUser(onExit:() -> Unit){
    var history by remember { mutableStateOf(false) }

    Column {
        if(history){
            Row {
                Spacer(modifier = Modifier.padding(10.dp))

                TextButton(onClick = {
                    history = false
                }) {
                    Text("Загрузка")
                }

                Spacer(modifier = Modifier.padding(10.dp))

                TextButton(onClick = onExit) {
                    Text("Выход")
                }
            }
            HistoryPage()
        }
        else{
            Row {
                Spacer(modifier = Modifier.padding(10.dp))

                TextButton(onClick = {
                    history = true
                }) {
                    Text("История")
                }

                Spacer(modifier = Modifier.padding(10.dp))

                TextButton(onClick = onExit) {
                    Text("Выход")
                }
            }
            UpscalePage()
        }
    }
}