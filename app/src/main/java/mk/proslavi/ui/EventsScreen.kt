package mk.proslavi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventsViewModel
import mk.proslavi.Fmt
import mk.proslavi.data.Event
import mk.proslavi.data.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(vm: EventsViewModel, onOpen: (Long) -> Unit) {
    val events by vm.events.collectAsStateWithLifecycle()
    var creating by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои прослави") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Нова прослава") },
            )
        },
    ) { padding ->
        if (events.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("Сѐ уште немате прослави.\nДопрете „Нова прослава“ за да започнете со планирање.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(events, key = { it.id }) { event -> EventCard(event) { onOpen(event.id) } }
            }
        }
    }

    if (creating) {
        EventDialog(
            initial = null,
            onSave = { event, withTemplates -> vm.create(event, withTemplates) },
            onDismiss = { creating = false },
        )
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(event.type.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(event.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    listOf(Fmt.date(event.dateMillis), event.location).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                Fmt.countdown(event.dateMillis),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Креирање (initial == null) или уредување на прослава. */
@Composable
fun EventDialog(initial: Event?, onSave: (Event, Boolean) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: EventType.SVADBA) }
    var date by remember { mutableStateOf(initial?.dateMillis) }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var budget by remember { mutableStateOf(Fmt.amountInput(initial?.budget ?: 0.0)) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var templates by remember { mutableStateOf(true) }

    EditDialog(
        title = if (initial == null) "Нова прослава" else "Уреди прослава",
        canSave = name.isNotBlank(),
        onSave = {
            val event = (initial ?: Event(name = name)).copy(
                name = name.trim(), type = type, dateMillis = date, location = location.trim(),
                budget = Fmt.parseAmount(budget), notes = notes.trim(),
            )
            onSave(event, initial == null && templates)
        },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        Field("Назив (на пр. Свадба Ана и Марко)", name, { name = it })
        ChoiceChips(EventType.entries, type, { it.label }) { type = it }
        DateField("Датум", date) { date = it }
        Field("Место / ресторан", location, { location = it })
        AmountField("Вкупен буџет (ден.)", budget) { budget = it }
        Field("Белешки", notes, { notes = it }, singleLine = false)
        if (initial == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = templates, onCheckedChange = { templates = it })
                Text("Додај готова листа на задачи и распоред", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
