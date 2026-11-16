package com.example.techshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.techshop.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { TechShopTheme { AppNav() } }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    val idArg = listOf(navArgument("id") { type = NavType.IntType })

    NavHost(
        nav, startDestination = "home",
        enterTransition = { fadeIn(tween(220)) }, exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) }, popExitTransition = { fadeOut(tween(160)) }
    ) {
        composable("home") { HomeScreen(vm, nav) }
        composable("orders") { OrdersScreen(vm, nav) }
        composable("order_edit/{id}", idArg) { OrderFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("clients") { ClientsScreen(vm, nav) }
        composable("client_edit/{id}", idArg) { ClientFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("employees") { EmployeesScreen(vm, nav) }
        composable("employee_edit/{id}", idArg) { EmployeeFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("components") { ComponentsScreen(vm, nav) }
        composable("component_edit/{id}", idArg) { ComponentFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("positions") { PositionsScreen(vm, nav) }
        composable("position_edit/{id}", idArg) { PositionFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
    }
}
