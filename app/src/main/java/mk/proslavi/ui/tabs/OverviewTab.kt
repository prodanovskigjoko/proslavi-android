package mk.proslavi.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.Fmt
import mk.proslavi.data.Event
import mk.proslavi.data.Rsvp
import mk.proslavi.ui.StatCard

@Composable
fun OverviewTab(vm: EventViewModel, event: Event, openTab: (Int) -> Unit) {
    val guests by vm.guests.collectAsStateWithLifecycle()
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val vendors by vm.vendors.collectAsStateWithLifecycle()
    val budget = budgetSummary(expenses, vendors)

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(event.type.label, style = MaterialTheme.typography.labelLarge)
                Text(Fmt.countdown(event.dateMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(Fmt.date(event.dateMillis), style = MaterialTheme.typography.bodyLarge)
                if (event.location.isNotBlank()) Text(event.location, style = MaterialTheme.typography.bodyMedium)
                if (event.notes.isNotBlank()) {
                    Text(event.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Гости",
                "${guests.filter { it.rsvp == Rsvp.DOAGJA }.sumOf { it.count }} / ${guests.sumOf { it.count }}",
                "потврдени / поканети",
                Modifier.weight(1f).clickable { openTab(1) },
            )
            StatCard(
                "Задачи",
                "${tasks.count { it.done }} / ${tasks.size}",
                "завршени",
                Modifier.weight(1f).clickable { openTab(3) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Трошоци",
                Fmt.money(budget.planned),
                if (event.budget > 0) "од ${Fmt.money(event.budget)}" else "планирано",
                Modifier.weight(1f).clickable { openTab(2) },
            )
            StatCard(
                "Добавувачи",
                vendors.size.toString(),
                "за доплата ${Fmt.money(vendors.sumOf { it.price - it.paid })}",
                Modifier.weight(1f).clickable { openTab(4) },
            )
        }

        val next = tasks.filter { !it.done }.take(5)
        if (next.isNotEmpty()) {
            Text("Следни задачи", style = MaterialTheme.typography.titleMedium)
            next.forEach { t -> TaskRow(t, onToggle = { vm.save(t.copy(done = true)) }, onClick = { openTab(3) }) }
        }
    }
}
