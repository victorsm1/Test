package com.tallerpro.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerpro.app.data.dao.ClientDao
import com.tallerpro.app.data.dao.InventoryDao
import com.tallerpro.app.data.dao.VehicleDao
import com.tallerpro.app.data.dao.WorkOrderDao
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    clientDao: ClientDao,
    vehicleDao: VehicleDao,
    workOrderDao: WorkOrderDao,
    inventoryDao: InventoryDao,
    onNavigate: (String) -> Unit
) {
    val clientCount by clientDao.getCount().collectAsState(initial = 0)
    val vehicleCount by vehicleDao.getCount().collectAsState(initial = 0)
    val activeOrders by workOrderDao.getActiveCount().collectAsState(initial = 0)
    val totalRevenue by workOrderDao.getTotalRevenue().collectAsState(initial = 0.0)
    val lowStockCount by inventoryDao.getLowStockCount().collectAsState(initial = 0)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TallerPro", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Panel de Control",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Clientes",
                        value = "$clientCount",
                        icon = Icons.Filled.People,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigate("clients") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Vehiculos",
                        value = "$vehicleCount",
                        icon = Icons.Filled.DirectionsCar,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigate("vehicles") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Ordenes Activas",
                        value = "$activeOrders",
                        icon = Icons.Filled.Build,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { onNavigate("workorders") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Stock Bajo",
                        value = "$lowStockCount",
                        icon = Icons.Filled.Warning,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { onNavigate("inventory") }
                    )
                }
                item {
                    DashboardCard(
                        title = "Facturado",
                        value = currencyFormat.format(totalRevenue),
                        icon = Icons.Filled.AttachMoney,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { }
                    )
                }
                item {
                    DashboardCard(
                        title = "Inventario",
                        value = "Ver todo",
                        icon = Icons.Filled.Inventory,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigate("inventory") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
