package mk.proslavi.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.ui.tabs.BudgetTab
import mk.proslavi.ui.tabs.GuestsTab
import mk.proslavi.ui.tabs.OverviewTab
import mk.proslavi.ui.tabs.ScheduleTab
import mk.proslavi.ui.tabs.TasksTab
import mk.proslavi.ui.tabs.VendorsTab
import mk.proslavi.ui.tabs.shareGuestList

private val tabs = listOf("Преглед", "Гости", "Буџет", "Задачи", "Добавувачи", "Распоред")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(vm: EventViewModel, onBack: () -> Unit) {
    val event by vm.event.collectAsStateWithLifecycle()
    val guests by vm.guests.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by rememberSaveable { mutableStateOf(false) }
    // Копчето „+“ отвора дијалог за додавање во тековниот таб.
    var adding by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                actions = {
                    if (tab == 1 && guests.isNotEmpty()) {
                        IconButton(onClick = { shareGuestList(context, event?.name ?: "", guests) }) {
                            Icon(Icons.Default.Share, "Сподели список на гости")
                        }
                    }
                    IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "Уреди прослава") }
                },
            )
        },
        floatingActionButton = {
            if (tab != 0) {
                FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, "Додај") }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i; adding = false }, text = { Text(title) })
                }
            }
            val current = event ?: return@Column
            val onAddDone = { adding = false }
            when (tab) {
                0 -> OverviewTab(vm, current) { tab = it }
                1 -> GuestsTab(vm, adding, onAddDone)
                2 -> BudgetTab(vm, current, adding, onAddDone)
                3 -> TasksTab(vm, adding, onAddDone)
                4 -> VendorsTab(vm, adding, onAddDone)
                5 -> ScheduleTab(vm, adding, onAddDone)
            }
        }
    }

    val current = event
    if (editing && current != null) {
        EventDialog(
            initial = current,
            onSave = { e, _ -> vm.saveEvent(e) },
            onDismiss = { editing = false },
            onDelete = { vm.deleteEvent(current); onBack() },
        )
    }
}
