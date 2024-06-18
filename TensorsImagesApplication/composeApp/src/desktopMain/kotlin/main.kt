import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.tinyfd.TinyFileDialogs

internal fun chooseFile(
    initialDirectory: String,
    fileExtension: String,
    title: String?,
    saveFile:Boolean = false
): String? = MemoryStack.stackPush().use { stack ->
    val filters = if (fileExtension.isNotEmpty()) fileExtension.split(",") else emptyList()
    val aFilterPatterns = stack.mallocPointer(filters.size)
    filters.forEach {
        aFilterPatterns.put(stack.UTF8("*.$it"))
    }
    aFilterPatterns.flip()

    if(saveFile){
        TinyFileDialogs.tinyfd_saveFileDialog(
            title,
            initialDirectory,
            aFilterPatterns,
            null
        )
    }
    else{
        TinyFileDialogs.tinyfd_openFileDialog(
            title,
            initialDirectory,
            aFilterPatterns,
            null,
            false
        )
    }

}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TensorsImages",
    ) {
        App()
    }
}