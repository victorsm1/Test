package com.tallerpro.app.ui.screens.inventory

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
import com.tallerpro.app.data.dao.InventoryDao
import com.tallerpro.app.data.model.InventoryItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    inventoryDao: InventoryDao,
    onNavigateToForm: (Long?) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val items by (if (searchQuery.isBlank()) inventoryDao.getAll() else inventoryDao.search(searchQuery))
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Eliminar repuesto") },
            text = { Text("¿Seguro que desea eliminar ${itemToDelete!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        itemToDelete?.let { inventoryDao.delete(it) }
                        itemToDelete = null
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
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
                Icon(Icons.Filled.Add, "Agregar repuesto")
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
                placeholder = { Text("Buscar por nombre o número de parte...") },
                leadingIcon = { Icon(Icons.Filled.Search, "Buscar") },
                singleLine = true
            )

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay repuestos en inventario", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn {
                    items(items, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            currencyFormat = currencyFormat,
                            onEdit = { onNavigateToForm(item.id) },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = item.quantity <= item.minStock

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
                if (isLowStock) Icons.Filled.Warning else Icons.Filled.Inventory,
                contentDescription = null,
                tint = if (isLowStock) Color(0xFFF44336) else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (item.partNumber.isNotBlank()) {
                    Text(
                        text = "N° Parte: ${item.partNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stock: ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLowStock) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = currencyFormat.format(item.unitPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (item.category.isNotBlank()) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
