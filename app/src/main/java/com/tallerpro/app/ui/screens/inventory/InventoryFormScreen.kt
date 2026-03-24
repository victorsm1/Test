package com.tallerpro.app.ui.screens.inventory

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
import com.tallerpro.app.data.dao.InventoryDao
import com.tallerpro.app.data.model.InventoryItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormScreen(
    inventoryDao: InventoryDao,
    itemId: Long?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId != null && itemId > 0) {
            inventoryDao.getById(itemId)?.let { item ->
                name = item.name
                description = item.description
                partNumber = item.partNumber
                quantity = if (item.quantity > 0) item.quantity.toString() else ""
                minStock = if (item.minStock > 0) item.minStock.toString() else ""
                unitPrice = if (item.unitPrice > 0) item.unitPrice.toString() else ""
                supplier = item.supplier
                category = item.category
                isEditing = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Repuesto" else "Nuevo Repuesto") },
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
                    if (name.isNotBlank()) {
                        scope.launch {
                            val item = InventoryItem(
                                id = if (isEditing) itemId!! else 0,
                                name = name.trim(),
                                description = description.trim(),
                                partNumber = partNumber.trim(),
                                quantity = quantity.toIntOrNull() ?: 0,
                                minStock = minStock.toIntOrNull() ?: 0,
                                unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                                supplier = supplier.trim(),
                                category = category.trim()
                            )
                            if (isEditing) inventoryDao.update(item) else inventoryDao.insert(item)
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = partNumber,
                onValueChange = { partNumber = it },
                label = { Text("Número de parte") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Stock mín.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Precio unitario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            OutlinedTextField(
                value = supplier,
                onValueChange = { supplier = it },
                label = { Text("Proveedor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
