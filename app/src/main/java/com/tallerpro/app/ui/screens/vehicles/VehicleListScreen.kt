package com.tallerpro.app.ui.screens.vehicles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerpro.app.data.dao.ClientDao
import com.tallerpro.app.data.dao.VehicleDao
import com.tallerpro.app.data.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    vehicleDao: VehicleDao,
    clientDao: ClientDao,
    onNavigateToForm: (Long?) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val vehicles by (if (searchQuery.isBlank()) vehicleDao.getAll() else vehicleDao.search(searchQuery))
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    val clientNames = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(vehicles) {
        vehicles.forEach { v ->
            if (!clientNames.containsKey(v.clientId)) {
                clientDao.getById(v.clientId)?.let { clientNames[v.clientId] = it.name }
            }
        }
    }

    if (vehicleToDelete != null) {
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = { Text("Eliminar vehículo") },
            text = { Text("¿Seguro que desea eliminar este vehículo? Se eliminarán también sus órdenes de trabajo.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vehicleToDelete?.let { vehicleDao.delete(it) }
                        vehicleToDelete = null
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehículos") },
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
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Filled.Add, "Agregar vehículo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por patente, marca o modelo...") },
                leadingIcon = { Icon(Icons.Filled.Search, "Buscar") },
                singleLine = true
            )

            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay vehículos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            clientName = clientNames[vehicle.clientId] ?: "...",
                            onEdit = { onNavigateToForm(vehicle.id) },
                            onDelete = { vehicleToDelete = vehicle }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleItem(
    vehicle: Vehicle,
    clientName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Patente: ${vehicle.plate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Dueño: $clientName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
