package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.ui.FormViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPreferences(applicationContext)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: FormViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(
                                modelClass: Class<T>,
                                extras: CreationExtras
                            ): T {
                                val savedStateHandle = extras.createSavedStateHandle()
                                return FormViewModel(savedStateHandle, userPrefs) as T
                            }
                        }
                    )
                    ResilientFormScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ResilientFormScreen(viewModel: FormViewModel, modifier: Modifier = Modifier) {
    val nameDisk by viewModel.nameFromDisk.observeAsState("")
    var nameInput by remember(nameDisk) { mutableStateOf(nameDisk) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Interceptación de Predictive Back (Navegación de Android 16 - API 36)
    BackHandler(enabled = nameInput.isNotEmpty() || viewModel.email.isNotEmpty()) {
        Log.d("NAV", "El usuario intentó retroceder con datos en el formulario")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Borrador de Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Formulario ultra-resiliente ante rotación, muerte del proceso y cierre.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // Campo persistido en DISCO (DataStore)
        OutlinedTextField(
            value = nameInput,
            onValueChange = {
                nameInput = it
                viewModel.saveName(it)
            },
            label = { Text("Nombre (Persiste al cerrar app)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Icono Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Indicador del Desafío: Feedback visual por 1 segundo tras guardar en disco
        AnimatedVisibility(
            visible = viewModel.isSavingIndicatorVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardado en disco",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Guardado en DataStore exitosamente...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Campo persistido en MEMORIA / PROCESO (SavedStateHandle)
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email (Persiste al rotar/muerte proceso)") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Icono Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.EmailAddress),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Switch de Notificaciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recibir notificaciones del sistema",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Monitoreo de Persistencia Multinivel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Capa Disco (DataStore): \"$nameDisk\"", style = MaterialTheme.typography.bodySmall)
                Text("• Capa Proceso (SavedStateHandle): \"${viewModel.email}\"", style = MaterialTheme.typography.bodySmall)
                Text("• Notificaciones: ${if (notificationsEnabled) "Activas" else "Inactivas"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}