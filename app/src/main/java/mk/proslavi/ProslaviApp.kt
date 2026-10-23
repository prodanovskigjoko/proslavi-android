package mk.proslavi

import android.app.Application
import mk.proslavi.data.AppDatabase

class ProslaviApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
}
