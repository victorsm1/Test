package com.tallerpro.app.ui.screens.vehicles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tallerpro.app.data.dao.ClientDao
import com.tallerpro.app.data.dao.VehicleDao
import com.tallerpro.app.data.model.Client
import com.tallerpro.app.data.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormScreen(
    vehicleDao: VehicleDao,
    clientDao: ClientDao,
    vehicleId: Long?,
    preselectedClientId: Long?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf(preselectedClientId ?: 0L) }
    var isEditing by remember { mutableStateOf(false) }

    val clients by clientDao.getAll().collectAsState(initial = emptyList())
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(vehicleId) {
        if (vehicleId != null && vehicleId > 0) {
            vehicleDao.getById(vehicleId)?.let { v ->
                brand = v.brand
                model = v.model
                year = v.year.toString()
                plate = v.plate
                color = v.color
                vin = v.vin
                mileage = if (v.mileage > 0) v.mileage.toString() else ""
                notes = v.notes
                selectedClientId = v.clientId
                isEditing = true
            }
        }
    }

    val selectedClientName = clients.find { it.id == selectedClientId }?.name ?: "Seleccionar cliente..."

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Vehículo" else "Nuevo Vehículo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (brand.isNotBlank() && plate.isNotBlank() && selectedClientId > 0) {
                        scope.launch {
                            val vehicle = Vehicle(
                                id = if (isEditing) vehicleId!! else 0,
                                clientId = selectedClientId,
                                brand = brand.trim(),
                                model = model.trim(),
                                year = year.toIntOrNull() ?: 0,
                                plate = plate.trim().uppercase(),
                                color = color.trim(),
                                vin = vin.trim(),
                                mileage = mileage.toIntOrNull() ?: 0,
                                notes = notes.trim()
                            )
                            if (isEditing) vehicleDao.update(vehicle) else vehicleDao.insert(vehicle)
                            onBack()
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Save, "Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Client selector
            ExposedDropdownMenuBox(
                expanded = clientDropdownExpanded,
                onExpandedChange = { clientDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedClientName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = clientDropdownExpanded,
                    onDismissRequest = { clientDropdownExpanded = false }
                ) {
                    clients.forEach { client ->
                        DropdownMenuItem(
                            text = { Text(client.name) },
                            onClick = {
                                selectedClientId = client.id
                                clientDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marca *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Año") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text("Patente *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("Kilometraje") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}
