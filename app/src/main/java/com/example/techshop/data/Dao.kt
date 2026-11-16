package com.example.techshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions ORDER BY title") fun getAll(): Flow<List<Position>>
    @Query("SELECT COUNT(*) FROM positions") suspend fun count(): Int
    @Query("SELECT * FROM positions WHERE id = :id") suspend fun getById(id: Int): Position?
    @Insert suspend fun insert(p: Position): Long
    @Update suspend fun update(p: Position)
    @Delete suspend fun delete(p: Position)
}

@Dao
interface EmployeeDao {
    @Query("SELECT e.*, p.title AS positionTitle FROM employees e INNER JOIN positions p ON p.id = e.idPosition ORDER BY e.lastName")
    fun getAllWithPosition(): Flow<List<EmployeeWithPosition>>
    @Query("SELECT * FROM employees ORDER BY lastName") fun getAll(): Flow<List<Employee>>
    @Query("SELECT * FROM employees WHERE id = :id") suspend fun getById(id: Int): Employee?
    @Insert suspend fun insert(e: Employee): Long
    @Update suspend fun update(e: Employee)
    @Delete suspend fun delete(e: Employee)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY lastName") fun getAll(): Flow<List<Client>>
    @Query("""SELECT * FROM clients
        WHERE lastName LIKE '%' || :q || '%' OR name LIKE '%' || :q || '%' OR middleName LIKE '%' || :q || '%'
        ORDER BY lastName""")
    fun search(q: String): Flow<List<Client>>
    @Query("SELECT * FROM clients WHERE id = :id") suspend fun getById(id: Int): Client?
    @Insert suspend fun insert(c: Client): Long
    @Update suspend fun update(c: Client)
    @Delete suspend fun delete(c: Client)
}

@Dao
interface OrderDao {
    // JOIN с Clients и Employees: выводим имена вместо ID
    @Query("""
        SELECT o.id AS id, o.date AS date, o.status AS status, o.price AS price,
               (c.lastName || ' ' || c.name) AS clientName,
               (e.lastName || ' ' || e.name) AS employeeName
        FROM orders o
        INNER JOIN clients c ON c.id = o.idClients
        INNER JOIN employees e ON e.id = o.idEmployees
        ORDER BY o.date DESC
    """)
    fun getAllWithNames(): Flow<List<OrderWithNames>>
    @Query("SELECT * FROM orders") fun getAll(): Flow<List<Order>>
    @Query("SELECT * FROM orders WHERE id = :id") suspend fun getById(id: Int): Order?
    @Insert suspend fun insert(o: Order): Long
    @Update suspend fun update(o: Order)
    @Delete suspend fun delete(o: Order)
    @Query("DELETE FROM orders WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface ComponentDao {
    @Query("SELECT * FROM components ORDER BY title") fun getAll(): Flow<List<Component>>
    @Query("SELECT * FROM components WHERE id = :id") suspend fun getById(id: Int): Component?
    @Insert suspend fun insert(c: Component): Long
    @Update suspend fun update(c: Component)
    @Delete suspend fun delete(c: Component)
}

@Dao
interface OrderConfigurationDao {
    @Query("SELECT * FROM order_configurations WHERE idOrders = :orderId") fun getForOrder(orderId: Int): Flow<List<OrderConfiguration>>
    // JOIN с Components: состав заказа с названиями
    @Query("""SELECT oc.id AS id, oc.idComponents AS idComponents, c.title AS title, c.price AS price, oc.quantity AS quantity
        FROM order_configurations oc INNER JOIN components c ON c.id = oc.idComponents
        WHERE oc.idOrders = :orderId ORDER BY oc.id""")
    fun getItems(orderId: Int): Flow<List<OrderItem>>
    @Query("DELETE FROM order_configurations WHERE id = :id") suspend fun deleteById(id: Int)
    @Insert suspend fun insert(oc: OrderConfiguration): Long
    @Update suspend fun update(oc: OrderConfiguration)
    @Delete suspend fun delete(oc: OrderConfiguration)
}
