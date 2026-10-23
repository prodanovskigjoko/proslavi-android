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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.Fmt
import mk.proslavi.data.Event
import mk.proslavi.data.Expense
import mk.proslavi.ui.AmountField
import mk.proslavi.ui.EditDialog
import mk.proslavi.ui.Field
import mk.proslavi.ui.StatCard

data class BudgetSummary(val planned: Double, val paid: Double)

fun budgetSummary(expenses: List<Expense>, vendors: List<mk.proslavi.data.Vendor>) = BudgetSummary(
    planned = expenses.sumOf { it.estimated } + vendors.sumOf { it.price },
    paid = expenses.sumOf { it.paid } + vendors.sumOf { it.paid },
)

@Composable
fun BudgetTab(vm: EventViewModel, event: Event, adding: Boolean, onAddDone: () -> Unit) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val vendors by vm.vendors.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Expense?>(null) }
    val summary = budgetSummary(expenses, vendors)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Буџет", Fmt.money(event.budget), modifier = Modifier.weight(1f))
                StatCard(
                    "Останува", Fmt.money(event.budget - summary.planned),
                    if (event.budget > 0 && summary.planned > event.budget) "Надминат буџет!" else "",
                    Modifier.weight(1f),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Планирано", Fmt.money(summary.planned), "вкл. добавувачи", Modifier.weight(1f))
                StatCard("Платено", Fmt.money(summary.paid), "за доплата ${Fmt.money(summary.planned - summary.paid)}", Modifier.weight(1f))
            }
        }
        if (event.budget > 0) {
            item {
                LinearProgressIndicator(
                    progress = { (summary.planned / event.budget).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }
        }
        if (event.budget == 0.0) {
            item {
                Text(
                    "Вкупниот буџет се внесува преку копчето за уредување горе десно.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item { Text("Трошоци", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        if (expenses.isEmpty()) {
            item {
                Text(
                    "Нема внесени трошоци. Додајте ги со „+“ (на пр. бурми, венчаница, покани).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(expenses, key = { it.id }) { x ->
            Card(Modifier.fillMaxWidth().clickable { editing = x }) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(x.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        if (x.category.isNotBlank()) Text(x.category, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Fmt.money(x.estimated), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (x.paid >= x.estimated && x.estimated > 0) "платено" else "платено ${Fmt.money(x.paid)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }

    if (adding) ExpenseDialog(null, vm::save, onAddDone)
    editing?.let { x -> ExpenseDialog(x, vm::save, { editing = null }, onDelete = { vm.delete(x) }) }
}

@Composable
private fun ExpenseDialog(initial: Expense?, onSave: (Expense) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var estimated by remember { mutableStateOf(Fmt.amountInput(initial?.estimated ?: 0.0)) }
    var paid by remember { mutableStateOf(Fmt.amountInput(initial?.paid ?: 0.0)) }

    EditDialog(
        title = if (initial == null) "Нов трошок" else "Уреди трошок",
        canSave = title.isNotBlank(),
        onSave = {
            onSave(
                (initial ?: Expense(title = title)).copy(
                    title = title.trim(), category = category.trim(),
                    estimated = Fmt.parseAmount(estimated), paid = Fmt.parseAmount(paid),
                ),
            )
        },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        Field("Опис", title, { title = it })
        Field("Категорија (на пр. Облека, Декорација)", category, { category = it })
        AmountField("Цена (ден.)", estimated) { estimated = it }
        AmountField("Платено (ден.)", paid) { paid = it }
    }
}
