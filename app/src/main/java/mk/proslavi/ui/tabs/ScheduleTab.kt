package mk.proslavi.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.data.ScheduleItem
import mk.proslavi.ui.EditDialog
import mk.proslavi.ui.EmptyState
import mk.proslavi.ui.Field
import mk.proslavi.ui.TimeField

@Composable
fun ScheduleTab(vm: EventViewModel, adding: Boolean, onAddDone: () -> Unit) {
    val schedule by vm.schedule.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<ScheduleItem?>(null) }

    if (schedule.isEmpty()) {
        EmptyState("Распоредот за денот е празен.\nДодајте точки со „+“ (на пр. 16:00 Црковна венчавка).")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(schedule, key = { it.id }) { s ->
                Card(Modifier.fillMaxWidth().clickable { editing = s }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            s.time,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(64.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(s.title, style = MaterialTheme.typography.bodyLarge)
                            if (s.notes.isNotBlank()) Text(s.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (adding) ScheduleDialog(null, vm::save, onAddDone)
    editing?.let { s -> ScheduleDialog(s, vm::save, { editing = null }, onDelete = { vm.delete(s) }) }
}

@Composable
private fun ScheduleDialog(initial: ScheduleItem?, onSave: (ScheduleItem) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var time by remember { mutableStateOf(initial?.time ?: "12:00") }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    EditDialog(
        title = if (initial == null) "Нова точка од распоредот" else "Уреди точка",
        canSave = title.isNotBlank(),
        onSave = {
            onSave((initial ?: ScheduleItem(time = time, title = title)).copy(time = time, title = title.trim(), notes = notes.trim()))
        },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        TimeField("Време", time) { time = it }
        Field("Што се случува", title, { title = it })
        Field("Белешки", notes, { notes = it }, singleLine = false)
    }
}
