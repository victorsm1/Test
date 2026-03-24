# TallerPro - Gestión de Taller Mecánico

App Android para la gestión integral de un taller mecánico automotriz.

## Funcionalidades

### Clientes
- Alta, baja y modificación de clientes
- Búsqueda por nombre o teléfono
- Datos de contacto completos

### Vehículos
- Registro de vehículos vinculados a clientes
- Datos: marca, modelo, año, patente, color, kilometraje
- Búsqueda por patente, marca o modelo

### Órdenes de Trabajo
- Creación y seguimiento de órdenes
- Estados: Pendiente → En Progreso → Esperando Repuestos → Completada → Entregada
- Costos de mano de obra y repuestos
- Diagnóstico y notas
- Cambio rápido de estado desde el detalle

### Inventario / Repuestos
- Control de stock de repuestos
- Alertas de stock bajo
- Número de parte, categoría, proveedor
- Precios unitarios

### Dashboard
- Resumen general del taller
- Cantidad de clientes y vehículos
- Órdenes activas
- Alertas de stock bajo
- Total facturado

## Tecnologías

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room Database** (SQLite local)
- **Navigation Compose**
- **Coroutines + Flow** para datos reactivos
- Min SDK: 26 (Android 8.0)

## Cómo compilar

1. Abrir el proyecto en Android Studio
2. Sync Gradle
3. Run en emulador o dispositivo físico
