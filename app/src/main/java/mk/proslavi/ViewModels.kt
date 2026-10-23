package mk.proslavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mk.proslavi.data.ChecklistTask
import mk.proslavi.data.Event
import mk.proslavi.data.Expense
import mk.proslavi.data.Guest
import mk.proslavi.data.ProslaviDao
import mk.proslavi.data.ScheduleItem
import mk.proslavi.data.Templates
import mk.proslavi.data.Vendor

private fun <T> Flow<T>.state(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

class EventsViewModel(private val dao: ProslaviDao) : ViewModel() {
    val events = dao.events().state(this, emptyList())

    fun create(event: Event, withTemplates: Boolean) = viewModelScope.launch {
        val id = dao.upsertEvent(event)
        if (withTemplates) {
            dao.insertTasks(Templates.tasksFor(event.type, event.dateMillis, id))
            dao.insertScheduleItems(Templates.scheduleFor(event.type, id))
        }
    }
}

class EventViewModel(private val dao: ProslaviDao, private val eventId: Long) : ViewModel() {
    val event = dao.event(eventId).state(this, null)
    val guests = dao.guests(eventId).state(this, emptyList())
    val expenses = dao.expenses(eventId).state(this, emptyList())
    val tasks = dao.tasks(eventId).state(this, emptyList())
    val vendors = dao.vendors(eventId).state(this, emptyList())
    val schedule = dao.schedule(eventId).state(this, emptyList())

    private fun io(block: suspend ProslaviDao.() -> Unit) {
        viewModelScope.launch { dao.block() }
    }

    fun saveEvent(e: Event) = io { upsertEvent(e.copy(id = eventId)) }
    fun deleteEvent(e: Event) = io { deleteEvent(e) }

    fun save(g: Guest) = io { upsertGuest(g.copy(eventId = eventId)) }
    fun delete(g: Guest) = io { deleteGuest(g) }

    fun save(x: Expense) = io { upsertExpense(x.copy(eventId = eventId)) }
    fun delete(x: Expense) = io { deleteExpense(x) }

    fun save(t: ChecklistTask) = io { upsertTask(t.copy(eventId = eventId)) }
    fun delete(t: ChecklistTask) = io { deleteTask(t) }

    fun save(v: Vendor) = io { upsertVendor(v.copy(eventId = eventId)) }
    fun delete(v: Vendor) = io { deleteVendor(v) }

    fun save(s: ScheduleItem) = io { upsertScheduleItem(s.copy(eventId = eventId)) }
    fun delete(s: ScheduleItem) = io { deleteScheduleItem(s) }
}
