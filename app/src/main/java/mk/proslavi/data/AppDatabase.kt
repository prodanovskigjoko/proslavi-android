package mk.proslavi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Event::class, Guest::class, Expense::class, ChecklistTask::class, Vendor::class, ScheduleItem::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ProslaviDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "proslavi.db").build()
    }
}
