package com.tallerpro.app.ui.screens.workorders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerpro.app.data.dao.ClientDao
import com.tallerpro.app.data.dao.VehicleDao
import com.tallerpro.app.data.dao.WorkOrderDao
import com.tallerpro.app.data.model.WorkOrder
import com.tallerpro.app.data.model.WorkOrderStatus
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    workOrderDao: WorkOrderDao,
    vehicleDao: VehicleDao,
    clientDao: ClientDao,
    orderId: Long,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var order by remember { mutableStateOf<WorkOrder?>(null) }
    var vehicleInfo by remember { mutableStateOf("") }
    var clientInfo by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    LaunchedEffect(orderId) {
        workOrderDao.getById(orderId)?.let { o ->
            order = o
            vehicleDao.getById(o.vehicleId)?.let { v ->
                vehicleInfo = "${v.brand} ${v.model} ${v.year} - ${v.plate}"
                clientDao.getById(v.clientId)?.let { c ->
                    clientInfo = "${c.name} - ${c.phone}"
                }
            }
        }
    }

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
                title = { Text("Orden #${orderId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(orderId) }) {
                        Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        order?.let { o ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status card
                val statusColor = when (o.status) {
                    WorkOrderStatus.PENDING -> Color(0xFFFF9800)
                    WorkOrderStatus.IN_PROGRESS -> Color(0xFF2196F3)
                    WorkOrderStatus.WAITING_PARTS -> Color(0xFFF44336)
                    WorkOrderStatus.COMPLETED -> Color(0xFF4CAF50)
                    WorkOrderStatus.DELIVERED -> Color(0xFF9E9E9E)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estado", fontWeight = FontWeight.Bold)
                        Text(statusLabels[o.status] ?: o.status.name, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick status buttons
                if (o.status != WorkOrderStatus.DELIVERED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val nextStatuses = when (o.status) {
                            WorkOrderStatus.PENDING -> listOf(WorkOrderStatus.IN_PROGRESS)
                            WorkOrderStatus.IN_PROGRESS -> listOf(WorkOrderStatus.WAITING_PARTS, WorkOrderStatus.COMPLETED)
                            WorkOrderStatus.WAITING_PARTS -> listOf(WorkOrderStatus.IN_PROGRESS)
                            WorkOrderStatus.COMPLETED -> listOf(WorkOrderStatus.DELIVERED)
                            WorkOrderStatus.DELIVERED -> emptyList()
                        }
                        nextStatuses.forEach { status ->
                            FilledTonalButton(
                                onClick = {
                                    scope.launch {
                                        val updated = o.copy(
                                            status = status,
                                            completedAt = if (status == WorkOrderStatus.COMPLETED || status == WorkOrderStatus.DELIVERED)
                                                System.currentTimeMillis() else o.completedAt
                                        )
                                        workOrderDao.update(updated)
                                        order = updated
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(statusLabels[status] ?: status.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Vehicle info
                DetailSection(title = "Vehículo") {
                    Text(vehicleInfo)
                }

                // Client info
                DetailSection(title = "Cliente") {
                    Text(clientInfo)
                }

                // Description
                DetailSection(title = "Descripción") {
                    Text(o.description)
                }

                // Diagnosis
                if (o.diagnosis.isNotBlank()) {
                    DetailSection(title = "Diagnóstico") {
                        Text(o.diagnosis)
                    }
                }

                // Costs
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Costos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mano de obra:")
                            Text(currencyFormat.format(o.laborCost))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Repuestos:")
                            Text(currencyFormat.format(o.partsCost))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                currencyFormat.format(o.totalCost),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Dates
                DetailSection(title = "Fechas") {
                    Text("Creada: ${dateFormat.format(Date(o.createdAt))}")
                    o.completedAt?.let {
                        Text("Completada: ${dateFormat.format(Date(it))}")
                    }
                }

                // Notes
                if (o.notes.isNotBlank()) {
                    DetailSection(title = "Notas") {
                        Text(o.notes)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
