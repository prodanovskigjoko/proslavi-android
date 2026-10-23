package mk.proslavi.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.Fmt
import mk.proslavi.data.ChecklistTask
import mk.proslavi.ui.DateField
import mk.proslavi.ui.EditDialog
import mk.proslavi.ui.EmptyState
import mk.proslavi.ui.Field

@Composable
fun TasksTab(vm: EventViewModel, adding: Boolean, onAddDone: () -> Unit) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<ChecklistTask?>(null) }

    if (tasks.isEmpty()) {
        EmptyState("Нема задачи. Додајте ги со „+“.")
    } else {
        val done = tasks.count { it.done }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                Text("Завршени $done од ${tasks.size}", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { done.toFloat() / tasks.size },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }
            items(tasks, key = { it.id }) { t -> TaskRow(t, onToggle = { vm.save(t.copy(done = !t.done)) }, onClick = { editing = t }) }
        }
    }

    if (adding) TaskDialog(null, vm::save, onAddDone)
    editing?.let { t -> TaskDialog(t, vm::save, { editing = null }, onDelete = { vm.delete(t) }) }
}

@Composable
fun TaskRow(task: ChecklistTask, onToggle: () -> Unit, onClick: () -> Unit) {
    val overdue = !task.done && task.dueMillis != null && Fmt.daysUntil(task.dueMillis) < 0
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(end = 16.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                if (task.dueMillis != null) {
                    Text(
                        (if (overdue) "Задоцнето · " else "До ") + Fmt.shortDate(task.dueMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDialog(initial: ChecklistTask?, onSave: (ChecklistTask) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var due by remember { mutableStateOf(initial?.dueMillis) }

    EditDialog(
        title = if (initial == null) "Нова задача" else "Уреди задача",
        canSave = title.isNotBlank(),
        onSave = { onSave((initial ?: ChecklistTask(title = title)).copy(title = title.trim(), dueMillis = due)) },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        Field("Задача", title, { title = it }, singleLine = false)
        DateField("Рок", due) { due = it }
    }
}
