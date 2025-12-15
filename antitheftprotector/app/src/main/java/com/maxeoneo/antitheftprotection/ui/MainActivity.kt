package com.maxeoneo.antitheftprotection.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.maxeoneo.antitheftprotection.R
import com.maxeoneo.antitheftprotection.data.CLDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AntiTheftApp()
        }
    }
}

@Composable
fun AntiTheftApp() {
    val context = LocalContext.current
    val dataSource = remember { CLDataSource(context) }
    var lockActive by remember { mutableStateOf(dataSource.isLockActive()) }
    var showSettings by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (lockActive) stringResource(id = R.string.tOnOffON) else stringResource(id = R.string.tOnOffOFF),
                    color = if (lockActive) Color.Red else Color.Gray,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Switch(
                    checked = lockActive,
                    onCheckedChange = {
                        if (lockActive) {
                            showPinDialog = true // Ask for PIN before switching off
                        } else {
                            if (dataSource.getPassword().isEmpty()) {
                                Toast.makeText(context, R.string.emSetPwd, Toast.LENGTH_SHORT).show()
                            } else {
                                dataSource.setLockActive(true)
                                lockActive = true
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Red,
                        uncheckedThumbColor = Color.Gray,
                        checkedTrackColor = Color(0xFFFFCDD2),
                        uncheckedTrackColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showSettings = true }) {
                    Text(stringResource(id = R.string.bSettings))
                }
            }
            if (showSettings) {
                SettingsDialog(
                    dataSource = dataSource,
                    onDismiss = { showSettings = false }
                )
            }
            if (showPinDialog) {
                PinDialog(
                    dataSource = dataSource,
                    onSuccess = {
                        dataSource.setLockActive(false)
                        lockActive = false
                        showPinDialog = false
                    },
                    onDismiss = { showPinDialog = false }
                )
            }
        }
    }
}

@Composable
fun SettingsDialog(dataSource: CLDataSource, onDismiss: () -> Unit) {
    val oldPinExists = dataSource.getPassword().isNotEmpty()
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var repeatNewPwd by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.bSettings)) },
        text = {
            Column {
                if (oldPinExists) {
                    OutlinedTextField(
                        value = oldPwd,
                        onValueChange = { oldPwd = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(id = R.string.oldPwd)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(id = R.string.newPwd)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = repeatNewPwd,
                    onValueChange = { repeatNewPwd = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(id = R.string.repeatNewPwd)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                error?.let { Text(it, color = Color.Red) }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    newPwd != repeatNewPwd -> error = context.getString(R.string.emNewAndRepeatedEquals)
                    oldPinExists && oldPwd != dataSource.getPassword() -> error = context.getString(R.string.emOldPinNotRight)
                    newPwd.length < 4 -> error = context.getString(R.string.emPinToShort)
                    else -> {
                        dataSource.savePassword(newPwd)
                        onDismiss()
                    }
                }
            }) {
                Text(stringResource(id = R.string.bSave))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    )
}

@Composable
fun PinDialog(dataSource: CLDataSource, onSuccess: () -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.enterPwd)) },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(id = R.string.enterPwd)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                error?.let { Text(it, color = Color.Red) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pin == dataSource.getPassword()) {
                    onSuccess()
                } else {
                    error = context.getString(R.string.emWrongPin)
                }
            }) {
                Text(stringResource(id = R.string.bSubmitPwd))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    )
}
