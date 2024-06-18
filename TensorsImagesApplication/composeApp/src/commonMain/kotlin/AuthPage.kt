import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun AuthPage(onLoggedIn: () -> Unit){
    var login by remember { mutableStateOf(true) }

    if(login){
        authorizationPage({
            login = false
        }, onLoggedIn)
    }
    else{
        signupPage({
            login = true
        }, onLoggedIn)
    }
}


fun loginClicked(login: String, password: String, logIn: Boolean, onError:(message: String) -> Unit, onLoggedIn:() -> Unit){
    currentCoroutineScope.launch {
        val res = loginUser(login, password, logIn)

        if(res == null){
            onLoggedIn()
        }
        else{
            onError(res)
        }
    }
}

@Composable
fun authorizationPage(
    onSignUpClicked: () -> Unit,
    onLoggedIn: () -> Unit
) {
    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarMessage by remember { mutableStateOf("") }
    var login by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Добро пожаловать!",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = login,
                onValueChange = { login = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Введите username") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )

            Spacer(modifier = Modifier.height(Dp(8F)))

            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Введите пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    loginClicked(login, password, true, {
                        snackBarMessage = it
                        showSnackBar = true
                    }, onLoggedIn)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Войти")
            }

            Spacer(modifier = Modifier.height(Dp(8F)))

            TextButton(
                onClick = { onSignUpClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Зарегистрироваться")
            }

            if (showSnackBar) {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = { showSnackBar = false }
                        ) {
                            Text("Закрыть")
                        }
                    }
                ) {
                    Text(snackBarMessage)
                }
            }
        }
    }
}


@Composable
fun signupPage(toLoginPage: () -> Unit, signupDone: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    var showInvalidFieldsError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(Dp(16F))) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Dp(16F))) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                    showInvalidFieldsError = false
                },
                label = {
                    Text("Username")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailError
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    showInvalidFieldsError = false
                },
                label = {
                    Text("Пароль")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = {
                    passwordConfirm = it
                    passwordError = false
                    showInvalidFieldsError = false
                },
                label = {
                    Text("Подтверждение пароля")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (showInvalidFieldsError) {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = { showInvalidFieldsError = false }
                        ) {
                            Text("Закрыть")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }

            Button(onClick = {
                emailError = email.isBlank()
                passwordError = password.isBlank()
                passwordError = passwordConfirm.isBlank() || passwordError
                if (emailError || passwordError) {
                    showInvalidFieldsError = true
                    errorMessage = "Заполнены не все обязательные поля!"
                }
                else if(password != passwordConfirm){
                    showInvalidFieldsError = true
                    passwordError = true
                    errorMessage = "Пароли не совпадают!"
                }
                else {
                    showInvalidFieldsError = false

                    loginClicked(email, password, false, {
                        errorMessage = it
                        showInvalidFieldsError = true
                    }, signupDone)
                }
            },
                modifier = Modifier.fillMaxWidth()) {
                Text("Зарегистрировать")
            }

            TextButton(onClick = toLoginPage, modifier = Modifier.fillMaxWidth()){
                Text("Уже есть аккаунт? Войдите")
            }
        }
    }
}