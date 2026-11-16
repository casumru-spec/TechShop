package com.example.techshop.data

import androidx.room.*

@Entity(tableName = "positions")
data class Position(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val salary: Double
)

@Entity(
    tableName = "employees",
    foreignKeys = [ForeignKey(Position::class, ["id"], ["idPosition"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("idPosition")]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val name: String,
    val middleName: String,
    val idPosition: Int,
    val phoneNumber: String,
    val experience: Int
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val name: String,
    val middleName: String,
    val phoneNumber: String,
    val email: String,
    val address: String
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(Client::class, ["id"], ["idClients"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Employee::class, ["id"], ["idEmployees"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idClients"), Index("idEmployees")]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idClients: Int,
    val idEmployees: Int,
    val date: Long,            // epoch millis
    val status: String,
    val price: Double
)

@Entity(tableName = "components")
data class Component(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,
    val characteristics: String,
    val price: Double,
    val quantity: Int
)

@Entity(
    tableName = "order_configurations",
    foreignKeys = [
        ForeignKey(Order::class, ["id"], ["idOrders"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Component::class, ["id"], ["idComponents"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idOrders"), Index("idComponents")]
)
data class OrderConfiguration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idOrders: Int,
    val idComponents: Int,
    val quantity: Int
)

// ---- JOIN-результаты ----
data class OrderWithNames(
    val id: Int,
    val date: Long,
    val status: String,
    val price: Double,
    val clientName: String,
    val employeeName: String
)

data class EmployeeWithPosition(
    @Embedded val employee: Employee,
    val positionTitle: String
)

data class OrderItem(
    val id: Int,
    val idComponents: Int,
    val title: String,
    val price: Double,
    val quantity: Int
)
