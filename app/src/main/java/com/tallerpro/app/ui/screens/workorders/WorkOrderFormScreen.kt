package com.tallerpro.app.ui.screens.workorders

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
import com.tallerpro.app.data.dao.WorkOrderDao
import com.tallerpro.app.data.model.Vehicle
import com.tallerpro.app.data.model.WorkOrder
import com.tallerpro.app.data.model.WorkOrderStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderFormScreen(
    workOrderDao: WorkOrderDao,
    vehicleDao: VehicleDao,
    clientDao: ClientDao,
    orderId: Long?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var description by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var partsCost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedVehicleId by remember { mutableStateOf(0L) }
    var selectedStatus by remember { mutableStateOf(WorkOrderStatus.PENDING) }
    var isEditing by remember { mutableStateOf(false) }
    var existingOrder by remember { mutableStateOf<WorkOrder?>(null) }

    val vehicles by vehicleDao.getAll().collectAsState(initial = emptyList())
    val vehicleLabels = remember { mutableStateMapOf<Long, String>() }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(vehicles) {
        vehicles.forEach { v ->
            if (!vehicleLabels.containsKey(v.id)) {
                val clientName = clientDao.getById(v.clientId)?.name ?: ""
                vehicleLabels[v.id] = "${v.brand} ${v.model} (${v.plate}) - $clientName"
            }
        }
    }

    LaunchedEffect(orderId) {
        if (orderId != null && orderId > 0) {
            workOrderDao.getById(orderId)?.let { order ->
                existingOrder = order
                description = order.description
                diagnosis = order.diagnosis
                laborCost = if (order.laborCost > 0) order.laborCost.toString() else ""
                partsCost = if (order.partsCost > 0) order.partsCost.toString() else ""
                notes = order.notes
                selectedVehicleId = order.vehicleId
                selectedStatus = order.status
                isEditing = true
            }
        }
    }

    val statusOptions = WorkOrderStatus.entries.toList()
    val statusLabels = mapOf(
        WorkOrderStatus.PENDING to "Pendiente",
        WorkOrderStatus.IN_PROGRESS to "En Progreso",
        WorkOrderStatus.WAITING_PARTS to "Esperando Repuestos",
        WorkOrderStatus.COMPLETED to "Completada",
        WorkOrderStatus.DELIVERED to "Entregada"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Orden" else "Nueva Orden") },
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
                    if (description.isNotBlank() && selectedVehicleId > 0) {
                        scope.launch {
                            val labor = laborCost.toDoubleOrNull() ?: 0.0
                            val parts = partsCost.toDoubleOrNull() ?: 0.0
                            val order = WorkOrder(
                                id = if (isEditing) orderId!! else 0,
                                vehicleId = selectedVehicleId,
                                description = description.trim(),
                                diagnosis = diagnosis.trim(),
                                status = selectedStatus,
                                laborCost = labor,
                                partsCost = parts,
                                totalCost = labor + parts,
                                notes = notes.trim(),
                                createdAt = existingOrder?.createdAt ?: System.currentTimeMillis(),
                                completedAt = if (selectedStatus == WorkOrderStatus.COMPLETED || selectedStatus == WorkOrderStatus.DELIVERED)
                                    System.currentTimeMillis() else null
                            )
                            if (isEditing) workOrderDao.update(order) else workOrderDao.insert(order)
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
            // Vehicle selector
            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = vehicleLabels[selectedVehicleId] ?: "Seleccionar vehículo...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vehículo *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false }
                ) {
                    vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text(vehicleLabels[vehicle.id] ?: "${vehicle.brand} ${vehicle.model}") },
                            onClick = {
                                selectedVehicleId = vehicle.id
                                vehicleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Status selector
            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = statusLabels[selectedStatus] ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(statusLabels[status] ?: status.name) },
                                onClick = {
                                    selectedStatus = status
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción del trabajo *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = { Text("Diagnóstico") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = laborCost,
                onValueChange = { laborCost = it },
                label = { Text("Costo mano de obra") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            OutlinedTextField(
                value = partsCost,
                onValueChange = { partsCost = it },
                label = { Text("Costo repuestos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )

            val labor = laborCost.toDoubleOrNull() ?: 0.0
            val parts = partsCost.toDoubleOrNull() ?: 0.0
            if (labor > 0 || parts > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Total: $${String.format("%.2f", labor + parts)}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

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
