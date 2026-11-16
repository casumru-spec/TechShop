package com.example.techshop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.techshop.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val repo = AppRepository(db)
    private fun <T> Flow<T>.state(init: T) = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), init)

    val query = MutableStateFlow("")
    val messages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val clients = query.flatMapLatest { repo.searchClients(it) }.state(emptyList())
    val allClients = repo.allClients.state(emptyList())
    val employees = repo.employeesWithPosition.state(emptyList())
    val allEmployees = repo.allEmployees.state(emptyList())
    val positions = repo.allPositions.state(emptyList())
    val orders = repo.ordersWithNames.state(emptyList())
    val components = repo.allComponents.state(emptyList())

    init {
        viewModelScope.launch {
            try { seedIfEmpty(db) } catch (e: Exception) { android.util.Log.e("TechShop", "Seed failed", e) }
        }
    }

    fun setQuery(q: String) { query.value = q }
    suspend fun getClient(id: Int) = repo.getClient(id)
    suspend fun getEmployee(id: Int) = repo.getEmployee(id)

    private fun errorText(e: Exception): String {
        val m = e.message.orEmpty()
        return when {
            "Недостаточно товара" in m -> "Недостаточно товара на складе"
            "Цена заказа" in m -> "Цена заказа не может быть отрицательной"
            "отрицательным" in m -> "Остаток на складе не может быть отрицательным"
            else -> "Операция невозможна: запись связана с другими данными"
        }
    }

    private fun safe(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() } catch (e: Exception) { messages.emit(errorText(e)) }
    }

    /** «Оформить заказ»: заказ + состав одной транзакцией. true — успех, false — всё откатано. */
    suspend fun placeOrder(order: Order, items: List<OrderConfiguration>): Boolean =
        try { repo.placeOrder(order, items); true } catch (e: Exception) { messages.emit(errorText(e)); false }

    fun saveClient(c: Client) = safe { repo.saveClient(c) }
    fun deleteClient(c: Client) = safe { repo.deleteClient(c) }
    fun saveEmployee(e: Employee) = safe { repo.saveEmployee(e) }
    fun deleteEmployee(e: Employee) = safe { repo.deleteEmployee(e) }
    fun saveOrder(o: Order) = safe { repo.saveOrder(o) }
    fun deleteOrder(id: Int) = safe { repo.deleteOrder(id) }

    suspend fun getOrder(id: Int) = repo.getOrder(id)
    fun orderItems(orderId: Int) = repo.orderItems(orderId)
    fun saveItem(oc: OrderConfiguration) = safe { repo.saveItem(oc) }
    fun deleteItem(id: Int) = safe { repo.deleteItem(id) }

    suspend fun getComponent(id: Int) = repo.getComponent(id)
    fun saveComponent(c: Component) = safe { repo.saveComponent(c) }
    fun deleteComponent(c: Component) = safe { repo.deleteComponent(c) }

    suspend fun getPosition(id: Int) = repo.getPosition(id)
    fun savePosition(p: Position) = safe { repo.savePosition(p) }
    fun deletePosition(p: Position) = safe { repo.deletePosition(p) }
}
