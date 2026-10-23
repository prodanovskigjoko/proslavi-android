package mk.proslavi.ui.tabs

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.proslavi.EventViewModel
import mk.proslavi.Fmt
import mk.proslavi.data.Vendor
import mk.proslavi.ui.AmountField
import mk.proslavi.ui.EditDialog
import mk.proslavi.ui.EmptyState
import mk.proslavi.ui.Field

@Composable
fun VendorsTab(vm: EventViewModel, adding: Boolean, onAddDone: () -> Unit) {
    val vendors by vm.vendors.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Vendor?>(null) }
    val context = LocalContext.current

    if (vendors.isEmpty()) {
        EmptyState("Додајте ги ресторанот, фотографот, музиката, цвеќарницата…\nЦените автоматски влегуваат во буџетот.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(vendors, key = { it.id }) { v ->
                Card(Modifier.fillMaxWidth().clickable { editing = v }) {
                    Row(Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            if (v.service.isNotBlank()) {
                                Text(v.service, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(v.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "${Fmt.money(v.price)} · капар/платено ${Fmt.money(v.paid)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (v.phone.isNotBlank()) {
                            IconButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${v.phone}")))
                            }) { Icon(Icons.Default.Call, "Јави се") }
                        }
                    }
                }
            }
        }
    }

    if (adding) VendorDialog(null, vm::save, onAddDone)
    editing?.let { v -> VendorDialog(v, vm::save, { editing = null }, onDelete = { vm.delete(v) }) }
}

@Composable
private fun VendorDialog(initial: Vendor?, onSave: (Vendor) -> Unit, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var service by remember { mutableStateOf(initial?.service ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var price by remember { mutableStateOf(Fmt.amountInput(initial?.price ?: 0.0)) }
    var paid by remember { mutableStateOf(Fmt.amountInput(initial?.paid ?: 0.0)) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    EditDialog(
        title = if (initial == null) "Нов добавувач" else "Уреди добавувач",
        canSave = name.isNotBlank(),
        onSave = {
            onSave(
                (initial ?: Vendor(name = name)).copy(
                    name = name.trim(), service = service.trim(), phone = phone.trim(),
                    price = Fmt.parseAmount(price), paid = Fmt.parseAmount(paid), notes = notes.trim(),
                ),
            )
        },
        onDismiss = onDismiss,
        onDelete = onDelete,
    ) {
        Field("Услуга (на пр. Фотограф, Музика)", service, { service = it })
        Field("Име / фирма", name, { name = it })
        Field("Телефон", phone, { phone = it }, KeyboardType.Phone)
        AmountField("Цена (ден.)", price) { price = it }
        AmountField("Капар / платено (ден.)", paid) { paid = it }
        Field("Белешки", notes, { notes = it }, singleLine = false)
    }
}
