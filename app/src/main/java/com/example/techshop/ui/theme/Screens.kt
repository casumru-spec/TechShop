package com.example.techshop.ui

import android.util.Patterns
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.techshop.data.*
import java.text.SimpleDateFormat
import java.util.*

private val cardShape = RoundedCornerShape(20.dp)
private fun money(v: Double) = String.format(Locale.getDefault(), "%,.2f ₽", v)
private fun fmtDate(ms: Long) = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ms))
val OrderStatuses = listOf("Новый", "В работе", "Выполнен", "Отменён")
// Поиск без учёта регистра (SQLite LIKE не игнорирует регистр для кириллицы)
private fun String.has(q: String) = contains(q.trim(), ignoreCase = true)

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, hint: String) = OutlinedTextField(
    value, onChange, placeholder = { Text(hint, color = SilkDim) }, singleLine = true,
    leadingIcon = { Icon(Icons.Default.Search, null, tint = Trace) },
    trailingIcon = { if (value.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Очистить") } },
    shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 0.dp),
    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = PanelColor, unfocusedContainerColor = PanelColor)
)

// ---------- Общие компоненты ----------
fun statusColor(status: String) = when (status) {
    "Новый" -> LedBlue; "В работе" -> LedAmber; "Выполнен" -> LedGreen; "Отменён" -> LedRed; else -> SilkDim
}

/** Светодиод: цветная точка с ореолом. */
@Composable
fun Led(color: Color) = Box(Modifier.size(14.dp).background(color.copy(alpha = 0.22f), CircleShape), Alignment.Center) {
    Box(Modifier.size(7.dp).background(color, CircleShape))
}

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) =
    Box(Modifier.fillMaxSize().background(BoardBg), content = content)

/** Дорожка под заголовком: линия, медный отрезок и контактная площадка. */
@Composable
fun TraceLine(modifier: Modifier = Modifier) = Canvas(modifier.height(12.dp)) {
    val y = size.height / 2
    val padX = 44.dp.toPx()
    drawLine(TraceDim, Offset(0f, y), Offset(size.width, y), 1.5.dp.toPx())
    drawLine(Copper, Offset(0f, y), Offset(padX, y), 2.5.dp.toPx(), StrokeCap.Round)
    drawCircle(Copper, 4.5.dp.toPx(), Offset(padX, y))
    drawCircle(BoardBg, 1.8.dp.toPx(), Offset(padX, y))
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?) {
    Column(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = if (onBack != null) 4.dp else 20.dp, end = 20.dp, top = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        }
        TraceLine(Modifier.fillMaxWidth().padding(top = 8.dp, start = if (onBack != null) 16.dp else 0.dp))
    }
}

@Composable
fun AppScaffold(
    title: String, vm: MainViewModel, onBack: (() -> Unit)? = null, onAdd: (() -> Unit)? = null,
    addLabel: String = "Добавить", content: @Composable ColumnScope.() -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent, contentColor = Silk, contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbar, Modifier.navigationBarsPadding()) },
            floatingActionButton = {
                if (onAdd != null) ExtendedFloatingActionButton(
                    onClick = onAdd, shape = ChamferSmall, containerColor = Copper,
                    contentColor = Color(0xFF2A1B05), modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp))
                    Text(addLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) { ScreenHeader(title, onBack); content() }
        }
    }
}

@Composable
fun EmptyState(text: String) = Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            if (text == "Ничего не найдено") "Измените запрос или сбросьте фильтр" else "Добавьте первую запись кнопкой справа внизу",
            color = SilkDim, fontSize = 14.sp
        )
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp), content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.fillMaxWidth().clip(Chamfer).background(PanelColor).border(1.dp, TraceDim, Chamfer)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(contentPadding),
        content = content
    )
}

/** Статус заказа в виде светодиода. */
@Composable
fun StatusBadge(status: String) {
    val c = statusColor(status)
    Row(
        Modifier.clip(CircleShape).border(1.dp, c.copy(alpha = 0.45f), CircleShape).padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Led(c); Spacer(Modifier.width(6.dp))
        Text(status, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedContainerColor = PanelColor, unfocusedContainerColor = PanelColor)

@Composable
fun FormField(value: String, onChange: (String) -> Unit, label: String, error: String?, keyboard: KeyboardType = KeyboardType.Text) =
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, isError = error != null,
        supportingText = { error?.let { Text(it) } }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(10.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, options: List<Pair<Int, String>>, selectedId: Int?, error: String?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selectedId }?.second ?: "", onValueChange = {}, readOnly = true,
            label = { Text(label) }, isError = error != null, supportingText = { error?.let { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(10.dp), colors = fieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it.second) }, onClick = { onSelect(it.first); expanded = false }) }
        }
    }
}

@Composable
fun FormColumn(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)

@Composable
fun SaveButton(label: String = "Сохранить", onClick: () -> Unit) = Button(
    onClick, Modifier.fillMaxWidth().padding(top = 8.dp).height(54.dp), shape = ChamferSmall,
    colors = ButtonDefaults.buttonColors(containerColor = Copper, contentColor = Color(0xFF2A1B05))
) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold) }

private const val REQ = "Обязательное поле"

// ---------- Home ----------
@Composable
fun HomeScreen(vm: MainViewModel, nav: NavController) {
    val clients by vm.allClients.collectAsState()
    val employees by vm.employees.collectAsState()
    val orders by vm.orders.collectAsState()
    val components by vm.components.collectAsState()
    val positions by vm.positions.collectAsState()
    val revenue = remember(orders) { orders.filter { it.status == "Выполнен" }.sumOf { it.price } }
    val active = remember(orders) { orders.count { it.status == "Новый" || it.status == "В работе" } }
    val units = remember(components) { components.sumOf { it.quantity } }
    val low = remember(components) { components.count { it.quantity < 5 } }

    AppBackground {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Hero(revenue, active)
            Column(Modifier.padding(16.dp, 4.dp, 16.dp, 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    { nav.navigate("order_edit/0") }, Modifier.fillMaxWidth().height(54.dp), shape = ChamferSmall,
                    colors = ButtonDefaults.buttonColors(containerColor = Copper, contentColor = Color(0xFF2A1B05))
                ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Новый заказ", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                AppCard(onClick = { nav.navigate("orders") }) {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Заказы", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("${orders.size}", color = Copper, fontFamily = FontFamily.Monospace, fontSize = 46.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OrderStatuses.forEach { st ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Led(statusColor(st)); Spacer(Modifier.width(8.dp))
                                    Text(st, color = SilkDim, fontSize = 13.sp, modifier = Modifier.width(76.dp))
                                    Text("${orders.count { it.status == st }}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                AppCard(onClick = { nav.navigate("components") }) {
                    Text("Склад", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$units", color = Copper, fontFamily = FontFamily.Monospace, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("  шт. на складе", color = SilkDim, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Led(if (low > 0) LedRed else LedGreen); Spacer(Modifier.width(8.dp))
                        Text(if (low > 0) "Заканчивается: $low поз." else "Остатки в норме", fontSize = 13.sp)
                    }
                }

                AppCard(contentPadding = PaddingValues(0.dp)) {
                    NavRow("Клиенты", clients.size, Icons.Default.Groups) { nav.navigate("clients") }
                    HorizontalDivider(color = TraceDim)
                    NavRow("Сотрудники", employees.size, Icons.Default.Badge) { nav.navigate("employees") }
                    HorizontalDivider(color = TraceDim)
                    NavRow("Должности", positions.size, Icons.Default.Work) { nav.navigate("positions") }
                }
            }
        }
    }
}

@Composable
private fun NavRow(title: String, count: Int, icon: ImageVector, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp, 15.dp), verticalAlignment = Alignment.CenterVertically
) {
    Icon(icon, null, tint = Trace); Spacer(Modifier.width(14.dp))
    Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
    Text("$count", color = Copper, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    Icon(Icons.Default.ChevronRight, null, tint = SilkDim)
}

/** Шапка: плата с процессором. Один раз при открытии сигнал бежит по дорожкам к чипу. */
@Composable
private fun Hero(revenue: Double, active: Int) {
    val chipTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
    val chipSize = 92.dp
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2400, easing = FastOutSlowInEasing)) }
    val shown by animateFloatAsState(revenue.toFloat(), tween(1600), label = "revenue")

    Box(Modifier.fillMaxWidth().height(chipTop + chipSize + 100.dp)) {
        Canvas(Modifier.matchParentSize()) { drawBoard(progress.value, chipTop.toPx(), chipSize.toPx()) }
        Column(Modifier.fillMaxWidth(0.6f).padding(start = 24.dp, top = chipTop)) {
            Text("TechShop", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
            Text("Сборка и продажа ПК", color = SilkDim, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Text("Выручка по выполненным заказам", color = SilkDim, fontSize = 12.sp)
            Text(money(shown.toDouble()).substringBefore(','), color = Copper, fontFamily = FontFamily.Monospace, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.align(Alignment.TopEnd).padding(top = chipTop, end = 24.dp).size(chipSize),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$active", color = Copper, fontFamily = FontFamily.Monospace, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("в работе", color = SilkDim, fontSize = 11.sp)
        }
    }
}

private fun DrawScope.drawBoard(p: Float, chipTop: Float, chipSize: Float) {
    val chipLeft = size.width - 24.dp.toPx() - chipSize
    val y0 = chipTop + chipSize
    val bend = 12.dp.toPx()
    val startX = 24.dp.toPx()
    val measure = PathMeasure()
    for (i in 0..3) {
        val px = chipLeft + chipSize * (i + 1) / 5f
        val laneY = y0 + 44.dp.toPx() + 12.dp.toPx() * i
        val path = Path().apply {
            moveTo(startX, laneY); lineTo(px - bend, laneY); lineTo(px, laneY - bend); lineTo(px, y0)
        }
        drawPath(path, TraceDim, style = Stroke(1.5.dp.toPx()))
        measure.setPath(path, false)
        val len = measure.length
        val local = (p * 1.5f - i * 0.12f).coerceIn(0f, 1f)
        if (local > 0f) {
            val seg = Path()
            measure.getSegment(0f, len * local, seg, true)
            drawPath(seg, Copper.copy(alpha = 0.85f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            if (local < 1f) drawCircle(Silk, 3.5.dp.toPx(), measure.getPosition(len * local))
        }
        drawCircle(TraceDim, 4.dp.toPx(), Offset(startX, laneY))
        drawCircle(BoardBg, 1.6.dp.toPx(), Offset(startX, laneY))
        drawCircle(Copper, 3.dp.toPx(), Offset(px, y0))
        drawLine(Copper.copy(alpha = 0.6f), Offset(px, chipTop - 7.dp.toPx()), Offset(px, chipTop), 2.dp.toPx())
    }
    val lit = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
    val r = CornerRadius(8.dp.toPx())
    drawRoundRect(Copper.copy(alpha = 0.14f * lit), Offset(chipLeft, chipTop), Size(chipSize, chipSize), r)
    drawRoundRect(Copper, Offset(chipLeft, chipTop), Size(chipSize, chipSize), r, style = Stroke(2.dp.toPx()))
}

// ---------- Orders ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.orders.collectAsState()
    var q by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val orders = remember(all, q, status) {
        all.filter { o ->
            (status == null || o.status == status) &&
                (q.isBlank() || "№${o.id} ${o.clientName} ${o.employeeName} ${o.status}".has(q))
        }
    }
    AppScaffold("Заказы", vm, onBack = { nav.popBackStack() }, onAdd = { nav.navigate("order_edit/0") }, addLabel = "Новый заказ") {
        SearchField(q, { q = it }, "Поиск: клиент, сотрудник, № заказа")
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(status == null, { status = null }, { Text("Все") })
            OrderStatuses.forEach { st -> FilterChip(status == st, { status = st }, { Text(st) }) }
        }
        if (orders.isEmpty()) EmptyState(if (all.isEmpty()) "Заказов пока нет" else "Ничего не найдено")
        else LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(orders, key = { it.id }) { o ->
                AppCard(modifier = Modifier.animateItem(), onClick = { nav.navigate("order_edit/${o.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("№ ${o.id.toString().padStart(4, '0')}", color = Copper, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                        StatusBadge(o.status)
                    }
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Default.Person, "Клиент: ${o.clientName}")
                    InfoRow(Icons.Default.Badge, "Сотрудник: ${o.employeeName}")
                    InfoRow(Icons.Default.CalendarMonth, fmtDate(o.date))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(money(o.price), color = Copper, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        IconButton({ vm.deleteOrder(o.id) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) = Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp))
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val clients by vm.allClients.collectAsState()
    val employees by vm.allEmployees.collectAsState()
    val components by vm.components.collectAsState()
    val items by remember(id) { vm.orderItems(id) }.collectAsState(emptyList())
    var clientId by remember { mutableStateOf<Int?>(null) }
    var employeeId by remember { mutableStateOf<Int?>(null) }
    var statusIdx by remember { mutableStateOf<Int?>(0) }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }
    var tried by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<OrderItem?>(null) }
    var addItem by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val draft = remember { mutableStateListOf<Pair<Int, Int>>() }   // (id компонента, количество) нового заказа

    LaunchedEffect(id) {
        if (id != 0) vm.getOrder(id)?.let {
            clientId = it.idClients; employeeId = it.idEmployees; date = it.date
            statusIdx = OrderStatuses.indexOf(it.status).coerceAtLeast(0); price = it.price.toString()
        }
    }
    val priceVal = price.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
    val draftTotal = draft.sumOf { (cid, q) -> (components.firstOrNull { it.id == cid }?.price ?: 0.0) * q }
    val finalPrice = priceVal ?: if (id == 0 && price.isBlank() && draft.isNotEmpty()) draftTotal else null

    AppScaffold(if (id == 0) "Новый заказ" else "Заказ №$id", vm, onBack = { nav.popBackStack() }) {
        FormColumn {
            Dropdown("Клиент", clients.map { it.id to "${it.lastName} ${it.name}" }, clientId, if (tried && clientId == null) "Выберите клиента" else null) { clientId = it }
            Dropdown("Сотрудник", employees.map { it.id to "${it.lastName} ${it.name}" }, employeeId, if (tried && employeeId == null) "Выберите сотрудника" else null) { employeeId = it }
            Dropdown("Статус", OrderStatuses.mapIndexed { i, s -> i to s }, statusIdx, null) { statusIdx = it }
            OutlinedTextField(
                fmtDate(date), {}, readOnly = true, label = { Text("Дата") }, singleLine = true,
                trailingIcon = { IconButton({ showDate = true }) { Icon(Icons.Default.CalendarMonth, null) } },
                shape = RoundedCornerShape(10.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            FormField(price, { price = it }, if (id == 0) "Цена, ₽ (пусто — по составу)" else "Цена, ₽", if (tried && finalPrice == null) "Введите число ≥ 0 или добавьте состав" else null, KeyboardType.Decimal)
            SaveButton(if (id == 0) "Оформить заказ" else "Сохранить") {
                tried = true
                if (clientId != null && employeeId != null && finalPrice != null && !saving) {
                    val order = Order(id, clientId!!, employeeId!!, date, OrderStatuses[statusIdx ?: 0], finalPrice)
                    if (id == 0) {
                        saving = true
                        scope.launch {
                            val ok = vm.placeOrder(order, draft.map { OrderConfiguration(idOrders = 0, idComponents = it.first, quantity = it.second) })
                            saving = false
                            if (ok) nav.popBackStack()
                        }
                    } else {
                        vm.saveOrder(order)
                        nav.popBackStack()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Состав заказа", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (id == 0) {
                Spacer(Modifier.height(8.dp))
                draft.forEachIndexed { index, (cid, q) ->
                    val c = components.firstOrNull { it.id == cid }
                    AppCard(modifier = Modifier.padding(bottom = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(c?.title ?: "—", fontWeight = FontWeight.SemiBold)
                                Text("$q × ${money(c?.price ?: 0.0)}", color = SilkDim, fontSize = 13.sp)
                            }
                            IconButton({ draft.removeAt(index) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                        }
                    }
                }
                OutlinedButton({ addItem = true }, Modifier.fillMaxWidth(), shape = ChamferSmall) { Text("Добавить компонент") }
                if (draft.isNotEmpty()) Text("Итого по составу: ${money(draftTotal)}", color = Copper, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text("Заказ и состав сохраняются одной транзакцией: если товара не хватит, не сохранится ничего.", color = SilkDim, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            } else {
                Spacer(Modifier.height(8.dp))
                items.forEach { it ->
                    AppCard(onClick = { editItem = it }, modifier = Modifier.padding(bottom = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(it.title, fontWeight = FontWeight.SemiBold)
                                Text("${it.quantity} × ${money(it.price)}", color = Color.Gray, fontSize = 13.sp)
                            }
                            IconButton({ vm.deleteItem(it.id) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                        }
                    }
                }
                OutlinedButton({ addItem = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Добавить компонент") }
                if (items.isNotEmpty()) TextButton({ price = items.sumOf { it.price * it.quantity }.toString() }, Modifier.fillMaxWidth()) {
                    Text("Пересчитать цену по составу (${money(items.sumOf { it.price * it.quantity })})")
                }
            }
        }
    }

    if (showDate) {
        val st = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = { TextButton({ st.selectedDateMillis?.let { date = it }; showDate = false }) { Text("OK") } },
            dismissButton = { TextButton({ showDate = false }) { Text("Отмена") } }
        ) { DatePicker(st) }
    }
    if (addItem || editItem != null) {
        ItemDialog(components, editItem, onDismiss = { addItem = false; editItem = null }) { compId, qty ->
            if (id == 0) {
                val i = draft.indexOfFirst { it.first == compId }
                if (i >= 0) draft[i] = compId to (draft[i].second + qty) else draft.add(compId to qty)
            } else vm.saveItem(OrderConfiguration(editItem?.id ?: 0, id, compId, qty))
            addItem = false; editItem = null
        }
    }
}

@Composable
private fun ItemDialog(components: List<Component>, initial: OrderItem?, onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var compId by remember { mutableStateOf(initial?.idComponents) }
    var qty by remember { mutableStateOf(initial?.quantity?.toString() ?: "1") }
    var tried by remember { mutableStateOf(false) }
    val q = qty.toIntOrNull()?.takeIf { it > 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Добавить компонент" else "Изменить позицию") },
        text = {
            Column {
                Dropdown("Компонент", components.map { it.id to "${it.title} (на складе: ${it.quantity})" }, compId, if (tried && compId == null) "Выберите компонент" else null) { compId = it }
                FormField(qty, { qty = it }, "Количество", if (tried && q == null) "Целое число > 0" else null, KeyboardType.Number)
            }
        },
        confirmButton = { TextButton({ tried = true; if (compId != null && q != null) onSave(compId!!, q) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}

// ---------- Clients ----------
@Composable
fun ClientsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.allClients.collectAsState()
    var q by remember { mutableStateOf("") }
    val clients = remember(all, q) {
        all.filter { q.isBlank() || "${it.lastName} ${it.name} ${it.middleName} ${it.phoneNumber} ${it.email}".has(q) }
    }
    AppScaffold("Клиенты", vm, onBack = { nav.popBackStack() }, onAdd = { nav.navigate("client_edit/0") }, addLabel = "Новый клиент") {
        SearchField(q, { q = it }, "Поиск: имя, телефон, email")
        if (clients.isEmpty()) EmptyState("Ничего не найдено")
        else LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(clients, key = { it.id }) { c ->
                AppCard(modifier = Modifier.animateItem(), onClick = { nav.navigate("client_edit/${c.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar("${c.lastName.firstOrNull() ?: ' '}${c.name.firstOrNull() ?: ' '}")
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${c.lastName} ${c.name} ${c.middleName}", fontWeight = FontWeight.Bold)
                            InfoRow(Icons.Default.Phone, c.phoneNumber)
                            InfoRow(Icons.Default.Email, c.email)
                            if (c.address.isNotBlank()) InfoRow(Icons.Default.Home, c.address)
                        }
                        IconButton({ vm.deleteClient(c) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(initials: String) = Box(
    Modifier.size(46.dp).clip(CutCornerShape(topEnd = 12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        .border(1.dp, TraceDim, CutCornerShape(topEnd = 12.dp)), Alignment.Center
) { Text(initials.uppercase(), color = Copper, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }

@Composable
fun ClientFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var last by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    var middle by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getClient(id)?.let { last = it.lastName; name = it.name; middle = it.middleName; phone = it.phoneNumber; email = it.email; address = it.address }
    }
    val phoneOk = phone.count { it.isDigit() } >= 10
    val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    AppScaffold(if (id == 0) "Новый клиент" else "Редактирование", vm, onBack = { nav.popBackStack() }) {
        FormColumn {
            FormField(last, { last = it }, "Фамилия", if (tried && last.isBlank()) REQ else null)
            FormField(name, { name = it }, "Имя", if (tried && name.isBlank()) REQ else null)
            FormField(middle, { middle = it }, "Отчество", null)
            FormField(phone, { phone = it }, "Телефон", if (tried && !phoneOk) "Минимум 10 цифр" else null, KeyboardType.Phone)
            FormField(email, { email = it }, "Email", if (tried && !emailOk) "Некорректный email" else null, KeyboardType.Email)
            FormField(address, { address = it }, "Адрес", null)
            SaveButton {
                tried = true
                if (last.isNotBlank() && name.isNotBlank() && phoneOk && emailOk) {
                    vm.saveClient(Client(id, last.trim(), name.trim(), middle.trim(), phone.trim(), email.trim(), address.trim()))
                    nav.popBackStack()
                }
            }
        }
    }
}

// ---------- Employees ----------
@Composable
fun EmployeesScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.employees.collectAsState()
    var q by remember { mutableStateOf("") }
    val list = remember(all, q) {
        all.filter { q.isBlank() || "${it.employee.lastName} ${it.employee.name} ${it.employee.middleName} ${it.positionTitle} ${it.employee.phoneNumber}".has(q) }
    }
    AppScaffold("Сотрудники", vm, onBack = { nav.popBackStack() }, onAdd = { nav.navigate("employee_edit/0") }, addLabel = "Новый сотрудник") {
        SearchField(q, { q = it }, "Поиск: имя, должность, телефон")
        if (list.isEmpty()) EmptyState(if (all.isEmpty()) "Сотрудников пока нет" else "Ничего не найдено")
        else LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list, key = { it.employee.id }) { item ->
                val e = item.employee
                AppCard(modifier = Modifier.animateItem(), onClick = { nav.navigate("employee_edit/${e.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar("${e.lastName.firstOrNull() ?: ' '}${e.name.firstOrNull() ?: ' '}")
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${e.lastName} ${e.name} ${e.middleName}", fontWeight = FontWeight.Bold)
                            Text(item.positionTitle, color = Trace, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            InfoRow(Icons.Default.Phone, e.phoneNumber)
                            InfoRow(Icons.Default.Star, "Опыт: ${e.experience} лет")
                        }
                        IconButton({ vm.deleteEmployee(e) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val positions by vm.positions.collectAsState()
    var last by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    var middle by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }; var posId by remember { mutableStateOf<Int?>(null) }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getEmployee(id)?.let { last = it.lastName; name = it.name; middle = it.middleName; phone = it.phoneNumber; exp = it.experience.toString(); posId = it.idPosition }
    }
    val expVal = exp.toIntOrNull()?.takeIf { it in 0..80 }
    val phoneOk = phone.count { it.isDigit() } >= 10
    AppScaffold(if (id == 0) "Новый сотрудник" else "Редактирование", vm, onBack = { nav.popBackStack() }) {
        FormColumn {
            FormField(last, { last = it }, "Фамилия", if (tried && last.isBlank()) REQ else null)
            FormField(name, { name = it }, "Имя", if (tried && name.isBlank()) REQ else null)
            FormField(middle, { middle = it }, "Отчество", null)
            Dropdown("Должность", positions.map { it.id to "${it.title} (${money(it.salary)})" }, posId, if (tried && posId == null) "Выберите должность" else null) { posId = it }
            FormField(phone, { phone = it }, "Телефон", if (tried && !phoneOk) "Минимум 10 цифр" else null, KeyboardType.Phone)
            FormField(exp, { exp = it }, "Опыт (лет)", if (tried && expVal == null) "Целое число 0–80" else null, KeyboardType.Number)
            SaveButton {
                tried = true
                if (last.isNotBlank() && name.isNotBlank() && posId != null && phoneOk && expVal != null) {
                    vm.saveEmployee(Employee(id, last.trim(), name.trim(), middle.trim(), posId!!, phone.trim(), expVal))
                    nav.popBackStack()
                }
            }
        }
    }
}

// ---------- Склад ----------
@Composable
fun ComponentsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.components.collectAsState()
    var q by remember { mutableStateOf("") }
    val list = remember(all, q) { all.filter { q.isBlank() || "${it.title} ${it.type} ${it.characteristics}".has(q) } }
    AppScaffold("Склад", vm, onBack = { nav.popBackStack() }, onAdd = { nav.navigate("component_edit/0") }, addLabel = "Новый компонент") {
        SearchField(q, { q = it }, "Поиск: название, тип, характеристики")
        if (list.isEmpty()) EmptyState(if (all.isEmpty()) "Компонентов на складе нет" else "Ничего не найдено")
        else LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list, key = { it.id }) { c ->
                AppCard(modifier = Modifier.animateItem(), onClick = { nav.navigate("component_edit/${c.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(c.type, color = Trace, fontSize = 13.sp)
                            Text(c.characteristics, color = Color.Gray, fontSize = 13.sp)
                            Row(Modifier.padding(top = 6.dp)) {
                                Text(money(c.price), color = Copper, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("В наличии: ${c.quantity}", color = if (c.quantity < 5) LedRed else Color.Unspecified, fontWeight = if (c.quantity < 5) FontWeight.Bold else null)
                            }
                        }
                        IconButton({ vm.deleteComponent(c) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var title by remember { mutableStateOf("") }; var type by remember { mutableStateOf("") }
    var chars by remember { mutableStateOf("") }; var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }; var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getComponent(id)?.let { title = it.title; type = it.type; chars = it.characteristics; price = it.price.toString(); qty = it.quantity.toString() }
    }
    val priceVal = price.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
    val qtyVal = qty.toIntOrNull()?.takeIf { it >= 0 }
    AppScaffold(if (id == 0) "Новый компонент" else "Редактирование", vm, onBack = { nav.popBackStack() }) {
        FormColumn {
            FormField(title, { title = it }, "Название", if (tried && title.isBlank()) REQ else null)
            FormField(type, { type = it }, "Тип", if (tried && type.isBlank()) REQ else null)
            FormField(chars, { chars = it }, "Характеристики", null)
            FormField(price, { price = it }, "Цена, ₽", if (tried && priceVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
            FormField(qty, { qty = it }, "Количество на складе", if (tried && qtyVal == null) "Целое число ≥ 0" else null, KeyboardType.Number)
            SaveButton {
                tried = true
                if (title.isNotBlank() && type.isNotBlank() && priceVal != null && qtyVal != null) {
                    vm.saveComponent(Component(id, title.trim(), type.trim(), chars.trim(), priceVal, qtyVal))
                    nav.popBackStack()
                }
            }
        }
    }
}

// ---------- Должности ----------
@Composable
fun PositionsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.positions.collectAsState()
    var q by remember { mutableStateOf("") }
    val list = remember(all, q) { all.filter { q.isBlank() || it.title.has(q) } }
    AppScaffold("Должности", vm, onBack = { nav.popBackStack() }, onAdd = { nav.navigate("position_edit/0") }, addLabel = "Новая должность") {
        SearchField(q, { q = it }, "Поиск по названию")
        if (list.isEmpty()) EmptyState(if (all.isEmpty()) "Должностей пока нет" else "Ничего не найдено")
        else LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list, key = { it.id }) { p ->
                AppCard(modifier = Modifier.animateItem(), onClick = { nav.navigate("position_edit/${p.id}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(money(p.salary), color = Copper, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton({ vm.deletePosition(p) }) { Icon(Icons.Default.Delete, null, tint = LedRed) }
                    }
                }
            }
        }
    }
}

@Composable
fun PositionFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var title by remember { mutableStateOf("") }; var salary by remember { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) { if (id != 0) vm.getPosition(id)?.let { title = it.title; salary = it.salary.toString() } }
    val salaryVal = salary.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
    AppScaffold(if (id == 0) "Новая должность" else "Редактирование", vm, onBack = { nav.popBackStack() }) {
        FormColumn {
            FormField(title, { title = it }, "Название", if (tried && title.isBlank()) REQ else null)
            FormField(salary, { salary = it }, "Оклад, ₽", if (tried && salaryVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
            SaveButton {
                tried = true
                if (title.isNotBlank() && salaryVal != null) { vm.savePosition(Position(id, title.trim(), salaryVal)); nav.popBackStack() }
            }
        }
    }
}
