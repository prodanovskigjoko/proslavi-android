package mk.proslavi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProslaviDao {
    @Query("SELECT * FROM events ORDER BY dateMillis IS NULL, dateMillis")
    fun events(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun event(id: Long): Flow<Event?>

    @Upsert
    suspend fun upsertEvent(event: Event): Long

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM guests WHERE eventId = :eventId ORDER BY name COLLATE NOCASE")
    fun guests(eventId: Long): Flow<List<Guest>>

    @Upsert
    suspend fun upsertGuest(guest: Guest)

    @Delete
    suspend fun deleteGuest(guest: Guest)

    @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY category COLLATE NOCASE, title COLLATE NOCASE")
    fun expenses(eventId: Long): Flow<List<Expense>>

    @Upsert
    suspend fun upsertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM tasks WHERE eventId = :eventId ORDER BY done, dueMillis IS NULL, dueMillis, id")
    fun tasks(eventId: Long): Flow<List<ChecklistTask>>

    @Upsert
    suspend fun upsertTask(task: ChecklistTask)

    @Insert
    suspend fun insertTasks(tasks: List<ChecklistTask>)

    @Delete
    suspend fun deleteTask(task: ChecklistTask)

    @Query("SELECT * FROM vendors WHERE eventId = :eventId ORDER BY service COLLATE NOCASE, name COLLATE NOCASE")
    fun vendors(eventId: Long): Flow<List<Vendor>>

    @Upsert
    suspend fun upsertVendor(vendor: Vendor)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)

    @Query("SELECT * FROM schedule WHERE eventId = :eventId ORDER BY time, id")
    fun schedule(eventId: Long): Flow<List<ScheduleItem>>

    @Upsert
    suspend fun upsertScheduleItem(item: ScheduleItem)

    @Insert
    suspend fun insertScheduleItems(items: List<ScheduleItem>)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)
}
