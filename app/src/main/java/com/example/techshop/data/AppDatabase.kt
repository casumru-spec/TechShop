package com.example.techshop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Position::class, Employee::class, Client::class, Order::class, Component::class, OrderConfiguration::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun clientDao(): ClientDao
    abstract fun orderDao(): OrderDao
    abstract fun componentDao(): ComponentDao
    abstract fun orderConfigDao(): OrderConfigurationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "techshop.db")
                .addCallback(object : Callback() {
                    // IF NOT EXISTS: триггеры создаются и в новой, и в уже существующей БД
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        TRIGGERS.forEach { db.execSQL(it) }
                    }
                })
                .build().also { INSTANCE = it }
        }
    }
}

/** Триггеры целостности данных (SQLite). */
private val TRIGGERS = listOf(
    // 1. Нельзя добавить в заказ больше, чем есть на складе
    """CREATE TRIGGER IF NOT EXISTS trg_item_before_insert BEFORE INSERT ON order_configurations
    BEGIN
        SELECT RAISE(ABORT, 'Недостаточно товара на складе')
        WHERE (SELECT quantity FROM components WHERE id = NEW.idComponents) < NEW.quantity;
    END""",
    // 2. Добавили позицию в заказ -> списываем со склада
    """CREATE TRIGGER IF NOT EXISTS trg_item_after_insert AFTER INSERT ON order_configurations
    BEGIN
        UPDATE components SET quantity = quantity - NEW.quantity WHERE id = NEW.idComponents;
    END""",
    // 3. Изменение позиции: проверяем остаток с учётом старого количества
    """CREATE TRIGGER IF NOT EXISTS trg_item_before_update BEFORE UPDATE ON order_configurations
    BEGIN
        SELECT RAISE(ABORT, 'Недостаточно товара на складе')
        WHERE (SELECT quantity FROM components WHERE id = NEW.idComponents)
              + (CASE WHEN NEW.idComponents = OLD.idComponents THEN OLD.quantity ELSE 0 END) < NEW.quantity;
    END""",
    // 4. Изменение позиции -> возвращаем старое количество и списываем новое
    """CREATE TRIGGER IF NOT EXISTS trg_item_after_update AFTER UPDATE ON order_configurations
    BEGIN
        UPDATE components SET quantity = quantity + OLD.quantity WHERE id = OLD.idComponents;
        UPDATE components SET quantity = quantity - NEW.quantity WHERE id = NEW.idComponents;
    END""",
    // 5. Удалили позицию (или весь заказ) -> возвращаем товар на склад
    """CREATE TRIGGER IF NOT EXISTS trg_item_after_delete AFTER DELETE ON order_configurations
    BEGIN
        UPDATE components SET quantity = quantity + OLD.quantity WHERE id = OLD.idComponents;
    END""",
    // 6-7. Цена заказа не может быть отрицательной
    """CREATE TRIGGER IF NOT EXISTS trg_order_price_insert BEFORE INSERT ON orders
    BEGIN
        SELECT RAISE(ABORT, 'Цена заказа не может быть отрицательной') WHERE NEW.price < 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_order_price_update BEFORE UPDATE ON orders
    BEGIN
        SELECT RAISE(ABORT, 'Цена заказа не может быть отрицательной') WHERE NEW.price < 0;
    END""",
    // 8. Остаток на складе не может быть отрицательным
    """CREATE TRIGGER IF NOT EXISTS trg_component_qty_check BEFORE UPDATE OF quantity ON components
    BEGIN
        SELECT RAISE(ABORT, 'Остаток на складе не может быть отрицательным') WHERE NEW.quantity < 0;
    END"""
)
