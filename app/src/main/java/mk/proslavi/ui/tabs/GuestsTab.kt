package mk.proslavi.ui.tabs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.data.Guest
import mk.proslavi.data.GuestSide
import mk.proslavi.data.Rsvp
import mk.proslavi.ui.ChoiceChips
import mk.proslavi.ui.EditDialog
import mk.proslavi.ui.EmptyState
import mk.proslavi.ui.Field

@Composable
fun GuestsTab(vm: EventViewModel, adding: Boolean, onAddDone: () -> Unit) {
    val guests by vm.guests.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<Rsvp?>(null) }
    var editing by remember { mutableStateOf<Guest?>(null) }

    val total = guests.sumOf { it.count }
    val coming = guests.filter { it.rsvp == Rsvp.DOAGJA }.sumOf { it.count }
    val waiting = guests.filter { it.rsvp == Rsvp.CEKA }.sumOf { it.count }

    Column {
        Text(
            "Поканети: $total лица  ·  Доаѓаат: $coming  ·  Чекаме: $waiting",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(Modifier.padding(horizontal = 16.dp)) {
            ChoiceChips(listOf(null) + Rsvp.entries, filter, { it?.label ?: "Сите" }) { filter = it }
        }
        val shown = guests.filter { filter == null || it.rsvp == filter }
        if (guests.isEmpty()) {
            EmptyState("Додајте ги гостите со копчето „+“.\nЗа семејство внесете го бројот на лица.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(shown, key = { it.id }) { guest ->
                    GuestRow(guest, onClick = { editing = guest }, onCycleRsvp = {
                        val next = Rsvp.entries[(guest.rsvp.ordinal + 1) % Rsvp.entries.size]
                        vm.save(guest.copy(rsvp = next))
                    })
                }
            }
        }
    }

    if (adding) GuestDialog(null, vm::save, onAddDone)
    editing?.let { g -> GuestDialog(g, vm::save, { editing = null }, onDelete = { vm.delete(g) }) }
}

@Composable
private fun GuestRow(guest: Guest, onClick: () -> Unit, onCycleRsvp: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (guest.count > 1) "${guest.name} (${guest.count})" else guest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                val details = listOfNotNull(
                    guest.side.label,
                    guest.tableNo.takeIf { it.isNotBlank() }?.let { "маса $it" },
                    guest.phone.takeIf { it.isNotBlank() },
                )
                Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
            val color = when (guest.rsvp) {
                Rsvp.DOAGJA -> MaterialTheme.colorScheme.primaryContainer
                Rsvp.NE_DOAGJA -> MaterialTheme.colorScheme.errorContainer
                Rsvp.CEKA -> MaterialTheme.colorScheme.surfaceVariant
            }
            AssistChip(
                onClick = onCycleRsvp,
                label = { Text(guest.rsvp.label) },
                colors = AssistChipDefaults.assistChipColors(containerColor = color),
            )
        }
    }
}

@Composable
private fun GuestDialog(initial: Guest?, onSave: (Guest) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var side by remember { mutableStateOf(initial?.side ?: GuestSide.ZAEDNICKI) }
    var count by remember { mutableStateOf((initial?.count ?: 1).toString()) }
    var rsvp by remember { mutableStateOf(initial?.rsvp ?: Rsvp.CEKA) }
    var table by remember { mutableStateOf(initial?.tableNo ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    EditDialog(
        title = if (initial == null) "Нов гостин" else "Уреди гостин",
        canSave = name.isNotBlank(),
        onSave = {
            onSave(
                (initial ?: Guest(name = name)).copy(
                    name = name.trim(), phone = phone.trim(), side = side,
                    count = count.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    rsvp = rsvp, tableNo = table.trim(), notes = notes.trim(),
                ),
            )
        },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        Field("Име и презиме / семејство", name, { name = it })
        Field("Број на лица", count, { s -> count = s.filter(Char::isDigit) }, KeyboardType.Number)
        Text("Страна", style = MaterialTheme.typography.labelMedium)
        ChoiceChips(GuestSide.entries, side, { it.label }) { side = it }
        Text("Одговор", style = MaterialTheme.typography.labelMedium)
        ChoiceChips(Rsvp.entries, rsvp, { it.label }) { rsvp = it }
        Field("Телефон", phone, { phone = it }, KeyboardType.Phone)
        Field("Маса", table, { table = it })
        Field("Белешки", notes, { notes = it }, singleLine = false)
    }
}

/** Го споделува списокот на гости (на пр. преку Viber или e-mail). */
fun shareGuestList(context: Context, eventName: String, guests: List<Guest>) {
    val text = buildString {
        appendLine("Гости – $eventName")
        appendLine("Вкупно: ${guests.sumOf { it.count }} лица")
        Rsvp.entries.forEach { r ->
            val group = guests.filter { it.rsvp == r }
            if (group.isNotEmpty()) {
                appendLine()
                appendLine("${r.label} (${group.sumOf { it.count }}):")
                group.forEach { g ->
                    append("• ${g.name}")
                    if (g.count > 1) append(" ×${g.count}")
                    if (g.tableNo.isNotBlank()) append(" – маса ${g.tableNo}")
                    appendLine()
                }
            }
        }
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Сподели список на гости"))
}
