package mk.proslavi.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EventType(val label: String) {
    SVADBA("Свадба"),
    VERIDBA("Веридба"),
    KRSTEVKA("Крштевка"),
    RODENDEN("Роденден"),
    JUBILEJ("Јубилеј"),
    DRUGO("Друго"),
}

enum class GuestSide(val label: String) {
    NEVESTA("Невеста"),
    MLADOZENEC("Младоженец"),
    ZAEDNICKI("Заеднички"),
}

enum class Rsvp(val label: String) {
    CEKA("Чека одговор"),
    DOAGJA("Доаѓа"),
    NE_DOAGJA("Не доаѓа"),
}

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: EventType = EventType.SVADBA,
    /** Датум како UTC полноќ (како што го враќа DatePicker), или null ако уште не е одреден. */
    val dateMillis: Long? = null,
    val location: String = "",
    val budget: Double = 0.0,
    val notes: String = "",
)

@Entity(
    tableName = "guests",
    foreignKeys = [ForeignKey(Event::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")],
)
data class Guest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long = 0,
    val name: String,
    val phone: String = "",
    val side: GuestSide = GuestSide.ZAEDNICKI,
    /** Број на лица (на пр. семејство од 4). */
    val count: Int = 1,
    val rsvp: Rsvp = Rsvp.CEKA,
    val tableNo: String = "",
    val notes: String = "",
)

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(Event::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")],
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long = 0,
    val title: String,
    val category: String = "",
    val estimated: Double = 0.0,
    val paid: Double = 0.0,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(Event::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")],
)
data class ChecklistTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long = 0,
    val title: String,
    val dueMillis: Long? = null,
    val done: Boolean = false,
)

@Entity(
    tableName = "vendors",
    foreignKeys = [ForeignKey(Event::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")],
)
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long = 0,
    val name: String,
    val service: String = "",
    val phone: String = "",
    val price: Double = 0.0,
    val paid: Double = 0.0,
    val notes: String = "",
)

@Entity(
    tableName = "schedule",
    foreignKeys = [ForeignKey(Event::class, ["id"], ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")],
)
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long = 0,
    /** Време во формат ЧЧ:ММ, па сортирањето по текст е и хронолошко. */
    val time: String,
    val title: String,
    val notes: String = "",
)
