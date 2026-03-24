package com.tallerpro.app.ui.screens.workorders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun WorkOrderListScreen(
    workOrderDao: WorkOrderDao,
    vehicleDao: VehicleDao,
    clientDao: ClientDao,
    onNavigateToForm: (Long?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit
) {
    val orders by workOrderDao.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var orderToDelete by remember { mutableStateOf<WorkOrder?>(null) }
    val vehicleInfo = remember { mutableStateMapOf<Long, String>() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "AR"))
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    LaunchedEffect(orders) {
        orders.forEach { order ->
            if (!vehicleInfo.containsKey(order.vehicleId)) {
                vehicleDao.getById(order.vehicleId)?.let { v ->
                    val clientName = clientDao.getById(v.clientId)?.name ?: ""
                    vehicleInfo[order.vehicleId] = "${v.brand} ${v.model} (${v.plate}) - $clientName"
                }
            }
        }
    }

    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Eliminar orden") },
            text = { Text("¿Seguro que desea eliminar esta orden de trabajo?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        orderToDelete?.let { workOrderDao.delete(it) }
                        orderToDelete = null
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Órdenes de Trabajo") },
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
                Icon(Icons.Filled.Add, "Nueva orden")
            }
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay órdenes de trabajo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    WorkOrderItem(
                        order = order,
                        vehicleInfo = vehicleInfo[order.vehicleId] ?: "...",
                        dateFormat = dateFormat,
                        currencyFormat = currencyFormat,
                        onClick = { onNavigateToDetail(order.id) },
                        onDelete = { orderToDelete = order }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkOrderItem(
    order: WorkOrder,
    vehicleInfo: String,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (order.status) {
        WorkOrderStatus.PENDING -> Color(0xFFFF9800)
        WorkOrderStatus.IN_PROGRESS -> Color(0xFF2196F3)
        WorkOrderStatus.WAITING_PARTS -> Color(0xFFF44336)
        WorkOrderStatus.COMPLETED -> Color(0xFF4CAF50)
        WorkOrderStatus.DELIVERED -> Color(0xFF9E9E9E)
    }
    val statusText = when (order.status) {
        WorkOrderStatus.PENDING -> "Pendiente"
        WorkOrderStatus.IN_PROGRESS -> "En Progreso"
        WorkOrderStatus.WAITING_PARTS -> "Esperando Repuestos"
        WorkOrderStatus.COMPLETED -> "Completada"
        WorkOrderStatus.DELIVERED -> "Entregada"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OT #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(statusText, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = statusColor.copy(alpha = 0.15f),
                            labelColor = statusColor
                        )
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Text(
                text = vehicleInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(order.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (order.totalCost > 0) {
                    Text(
                        text = currencyFormat.format(order.totalCost),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
