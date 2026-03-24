package com.tallerpro.app.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object ClientList : Screen("clients")
    data object ClientForm : Screen("clients/form?clientId={clientId}") {
        fun createRoute(clientId: Long? = null) =
            if (clientId != null) "clients/form?clientId=$clientId" else "clients/form"
    }
    data object VehicleList : Screen("vehicles")
    data object VehicleForm : Screen("vehicles/form?vehicleId={vehicleId}&clientId={clientId}") {
        fun createRoute(vehicleId: Long? = null, clientId: Long? = null): String {
            val params = mutableListOf<String>()
            vehicleId?.let { params.add("vehicleId=$it") }
            clientId?.let { params.add("clientId=$it") }
            return if (params.isEmpty()) "vehicles/form" else "vehicles/form?${params.joinToString("&")}"
        }
    }
    data object WorkOrderList : Screen("workorders")
    data object WorkOrderForm : Screen("workorders/form?orderId={orderId}") {
        fun createRoute(orderId: Long? = null) =
            if (orderId != null) "workorders/form?orderId=$orderId" else "workorders/form"
    }
    data object WorkOrderDetail : Screen("workorders/{orderId}") {
        fun createRoute(orderId: Long) = "workorders/$orderId"
    }
    data object InventoryList : Screen("inventory")
    data object InventoryForm : Screen("inventory/form?itemId={itemId}") {
        fun createRoute(itemId: Long? = null) =
            if (itemId != null) "inventory/form?itemId=$itemId" else "inventory/form"
    }
}
