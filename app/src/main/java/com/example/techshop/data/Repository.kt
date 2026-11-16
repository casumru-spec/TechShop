package com.example.techshop.data

import androidx.room.withTransaction

class AppRepository(private val db: AppDatabase) {
    private val positions = db.positionDao()
    private val employees = db.employeeDao()
    private val clients = db.clientDao()
    private val orders = db.orderDao()
    private val components = db.componentDao()
    private val configs = db.orderConfigDao()

    val allPositions = positions.getAll()
    val allClients = clients.getAll()
    val employeesWithPosition = employees.getAllWithPosition()
    val allEmployees = employees.getAll()
    val ordersWithNames = orders.getAllWithNames()
    val allComponents = components.getAll()

    fun searchClients(q: String) = if (q.isBlank()) clients.getAll() else clients.search(q.trim())
    fun orderItems(orderId: Int) = configs.getItems(orderId)

    suspend fun positionCount() = positions.count()
    suspend fun getPosition(id: Int) = positions.getById(id)
    suspend fun savePosition(p: Position) { if (p.id == 0) positions.insert(p) else positions.update(p) }
    suspend fun deletePosition(p: Position) = positions.delete(p)

    suspend fun getClient(id: Int) = clients.getById(id)
    suspend fun saveClient(c: Client) { if (c.id == 0) clients.insert(c) else clients.update(c) }
    suspend fun deleteClient(c: Client) = clients.delete(c)

    suspend fun getEmployee(id: Int) = employees.getById(id)
    suspend fun saveEmployee(e: Employee) { if (e.id == 0) employees.insert(e) else employees.update(e) }
    suspend fun deleteEmployee(e: Employee) = employees.delete(e)

    suspend fun getOrder(id: Int) = orders.getById(id)
    suspend fun saveOrder(o: Order) { if (o.id == 0) orders.insert(o) else orders.update(o) }
    suspend fun deleteOrder(id: Int) = orders.deleteById(id)

    suspend fun getComponent(id: Int) = components.getById(id)
    suspend fun saveComponent(c: Component) { if (c.id == 0) components.insert(c) else components.update(c) }
    suspend fun deleteComponent(c: Component) = components.delete(c)

    /**
     * Аналог хранимой процедуры «Оформить заказ».
     * Заказ и все его позиции сохраняются ОДНОЙ транзакцией: если любая вставка
     * не прошла (например, триггер заблокировал нехватку товара), откатывается всё.
     */
    suspend fun placeOrder(order: Order, items: List<OrderConfiguration>): Int = db.withTransaction {
        val orderId = orders.insert(order.copy(id = 0)).toInt()
        items.forEach { configs.insert(it.copy(id = 0, idOrders = orderId)) }
        orderId
    }

    suspend fun saveItem(oc: OrderConfiguration) { if (oc.id == 0) configs.insert(oc) else configs.update(oc) }
    suspend fun deleteItem(id: Int) = configs.deleteById(id)
}
