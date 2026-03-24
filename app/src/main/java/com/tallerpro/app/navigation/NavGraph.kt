package com.tallerpro.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tallerpro.app.data.database.AppDatabase
import com.tallerpro.app.ui.screens.clients.ClientFormScreen
import com.tallerpro.app.ui.screens.clients.ClientListScreen
import com.tallerpro.app.ui.screens.dashboard.DashboardScreen
import com.tallerpro.app.ui.screens.inventory.InventoryFormScreen
import com.tallerpro.app.ui.screens.inventory.InventoryListScreen
import com.tallerpro.app.ui.screens.vehicles.VehicleFormScreen
import com.tallerpro.app.ui.screens.vehicles.VehicleListScreen
import com.tallerpro.app.ui.screens.workorders.WorkOrderDetailScreen
import com.tallerpro.app.ui.screens.workorders.WorkOrderFormScreen
import com.tallerpro.app.ui.screens.workorders.WorkOrderListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                clientDao = database.clientDao(),
                vehicleDao = database.vehicleDao(),
                workOrderDao = database.workOrderDao(),
                inventoryDao = database.inventoryDao(),
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Clients
        composable(Screen.ClientList.route) {
            ClientListScreen(
                clientDao = database.clientDao(),
                onNavigateToForm = { clientId ->
                    navController.navigate(Screen.ClientForm.createRoute(clientId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "clients/form?clientId={clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getLong("clientId")?.takeIf { it > 0 }
            ClientFormScreen(
                clientDao = database.clientDao(),
                clientId = clientId,
                onBack = { navController.popBackStack() }
            )
        }

        // Vehicles
        composable(Screen.VehicleList.route) {
            VehicleListScreen(
                vehicleDao = database.vehicleDao(),
                clientDao = database.clientDao(),
                onNavigateToForm = { vehicleId ->
                    navController.navigate(Screen.VehicleForm.createRoute(vehicleId = vehicleId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "vehicles/form?vehicleId={vehicleId}&clientId={clientId}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("clientId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId")?.takeIf { it > 0 }
            val clientId = backStackEntry.arguments?.getLong("clientId")?.takeIf { it > 0 }
            VehicleFormScreen(
                vehicleDao = database.vehicleDao(),
                clientDao = database.clientDao(),
                vehicleId = vehicleId,
                preselectedClientId = clientId,
                onBack = { navController.popBackStack() }
            )
        }

        // Work Orders
        composable(Screen.WorkOrderList.route) {
            WorkOrderListScreen(
                workOrderDao = database.workOrderDao(),
                vehicleDao = database.vehicleDao(),
                clientDao = database.clientDao(),
                onNavigateToForm = { orderId ->
                    navController.navigate(Screen.WorkOrderForm.createRoute(orderId))
                },
                onNavigateToDetail = { orderId ->
                    navController.navigate(Screen.WorkOrderDetail.createRoute(orderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "workorders/form?orderId={orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId")?.takeIf { it > 0 }
            WorkOrderFormScreen(
                workOrderDao = database.workOrderDao(),
                vehicleDao = database.vehicleDao(),
                clientDao = database.clientDao(),
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "workorders/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            WorkOrderDetailScreen(
                workOrderDao = database.workOrderDao(),
                vehicleDao = database.vehicleDao(),
                clientDao = database.clientDao(),
                orderId = orderId,
                onEdit = { id ->
                    navController.navigate(Screen.WorkOrderForm.createRoute(id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Inventory
        composable(Screen.InventoryList.route) {
            InventoryListScreen(
                inventoryDao = database.inventoryDao(),
                onNavigateToForm = { itemId ->
                    navController.navigate(Screen.InventoryForm.createRoute(itemId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "inventory/form?itemId={itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId")?.takeIf { it > 0 }
            InventoryFormScreen(
                inventoryDao = database.inventoryDao(),
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
