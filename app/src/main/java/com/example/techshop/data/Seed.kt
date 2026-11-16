package com.example.techshop.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

suspend fun seedIfEmpty(db: AppDatabase) {
    if (db.clientDao().getAll().first().isNotEmpty()) return
    withContext(Dispatchers.IO) { db.clearAllTables() }
    val day = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()

    val positions = listOf(
        "Сборщик ПК" to 70000.0, "Менеджер по продажам" to 85000.0, "Системный администратор" to 95000.0,
        "Мастер-консультант" to 65000.0, "Инженер-тестировщик" to 90000.0, "Специалист по гарантии" to 72000.0,
        "Логист" to 68000.0, "Кладовщик" to 55000.0, "Руководитель отдела" to 130000.0, "Технический директор" to 180000.0
    ).mapIndexed { i, p -> Position(i + 1, p.first, p.second) }

    val employees = listOf(
        listOf("Иванов", "Алексей", "Петрович", "+79001110001", 5),
        listOf("Смирнова", "Мария", "Игоревна", "+79001110002", 3),
        listOf("Кузнецов", "Дмитрий", "Сергеевич", "+79001110003", 8),
        listOf("Попова", "Анна", "Олеговна", "+79001110004", 2),
        listOf("Васильев", "Игорь", "Николаевич", "+79001110005", 6),
        listOf("Соколова", "Елена", "Викторовна", "+79001110006", 4),
        listOf("Михайлов", "Павел", "Андреевич", "+79001110007", 7),
        listOf("Новикова", "Ольга", "Дмитриевна", "+79001110008", 1),
        listOf("Фёдоров", "Сергей", "Александрович", "+79001110009", 12),
        listOf("Морозова", "Татьяна", "Павловна", "+79001110010", 15)
    ).mapIndexed { i, e -> Employee(i + 1, e[0] as String, e[1] as String, e[2] as String, i + 1, e[3] as String, e[4] as Int) }

    val clients = listOf(
        listOf("Петров", "Андрей", "Викторович", "+79101230001", "petrov@mail.ru", "Москва, ул. Ленина, 10"),
        listOf("Сидорова", "Наталья", "Ивановна", "+79101230002", "sidorova@mail.ru", "Санкт-Петербург, Невский пр., 25"),
        listOf("Орлов", "Максим", "Денисович", "+79101230003", "orlov@gmail.com", "Казань, ул. Баумана, 7"),
        listOf("Лебедева", "Ксения", "Романовна", "+79101230004", "lebedeva@yandex.ru", "Екатеринбург, ул. Мира, 14"),
        listOf("Громов", "Артём", "Евгеньевич", "+79101230005", "gromov@gmail.com", "Новосибирск, Красный пр., 50"),
        listOf("Зайцева", "Виктория", "Алексеевна", "+79101230006", "zaytseva@mail.ru", "Самара, ул. Победы, 3"),
        listOf("Беляев", "Роман", "Сергеевич", "+79101230007", "belyaev@yandex.ru", "Ростов-на-Дону, ул. Садовая, 21"),
        listOf("Ковалёва", "Дарья", "Михайловна", "+79101230008", "kovaleva@gmail.com", "Краснодар, ул. Красная, 88"),
        listOf("Тихонов", "Глеб", "Олегович", "+79101230009", "tikhonov@mail.ru", "Воронеж, пр. Революции, 12"),
        listOf("Абрамова", "Полина", "Андреевна", "+79101230010", "abramova@yandex.ru", "Уфа, ул. Чернышевского, 5")
    ).mapIndexed { i, c -> Client(i + 1, c[0], c[1], c[2], c[3], c[4], c[5]) }

    val components = listOf(
        listOf("Intel Core i5-13400F", "Процессор", "10 ядер, 2.5 ГГц, LGA1700", 17500.0, 25),
        listOf("NVIDIA GeForce RTX 4060", "Видеокарта", "8 ГБ GDDR6", 32000.0, 12),
        listOf("Kingston Fury 16GB DDR5", "Оперативная память", "5600 МГц, 1x16 ГБ", 5200.0, 40),
        listOf("Samsung 980 1TB", "SSD", "NVMe M.2, 3500 МБ/с", 7800.0, 30),
        listOf("MSI PRO B760M-P", "Материнская плата", "mATX, LGA1700, DDR5", 11500.0, 15),
        listOf("be quiet! 650W", "Блок питания", "650 Вт, 80+ Gold", 8900.0, 20),
        listOf("DeepCool CC560", "Корпус", "Mid-Tower, 4 вентилятора", 5600.0, 18),
        listOf("DeepCool AK400", "Кулер", "Башенный, 120 мм", 2900.0, 35),
        listOf("Samsung Odyssey G5", "Монитор", "27\", 2K, 144 Гц", 24500.0, 8),
        listOf("Seagate Barracuda 2TB", "HDD", "SATA III, 7200 об/мин", 4800.0, 22)
    ).mapIndexed { i, c -> Component(i + 1, c[0] as String, c[1] as String, c[2] as String, c[3] as Double, c[4] as Int) }

    val statuses = OrderStatusesSeed
    val orders = listOf(
        listOf(1, 2, 3, 85000.0), listOf(2, 2, 2, 62000.0), listOf(3, 1, 0, 110000.0), listOf(4, 4, 1, 48000.0),
        listOf(5, 1, 2, 95000.0), listOf(6, 6, 3, 38000.0), listOf(7, 2, 1, 72000.0), listOf(8, 5, 2, 130000.0),
        listOf(9, 2, 0, 56000.0), listOf(10, 1, 1, 89000.0)
    ).mapIndexed { i, o ->
        Order(i + 1, o[0] as Int, o[1] as Int, now - (10 - i) * 3 * day, statuses[o[2] as Int], o[3] as Double)
    }

    val configs = listOf(
        listOf(1, 1, 1), listOf(1, 2, 1), listOf(2, 3, 2), listOf(3, 4, 1), listOf(4, 5, 1),
        listOf(5, 6, 1), listOf(6, 7, 1), listOf(7, 8, 2), listOf(8, 9, 1), listOf(9, 10, 2)
    ).mapIndexed { i, c -> OrderConfiguration(i + 1, c[0], c[1], c[2]) }

    db.withTransaction {
        positions.forEach { db.positionDao().insert(it) }
        employees.forEach { db.employeeDao().insert(it) }
        clients.forEach { db.clientDao().insert(it) }
        components.forEach { db.componentDao().insert(it) }
        orders.forEach { db.orderDao().insert(it) }
        configs.forEach { db.orderConfigDao().insert(it) }
    }
}

private val OrderStatusesSeed = listOf("Новый", "В работе", "Выполнен", "Отменён")
