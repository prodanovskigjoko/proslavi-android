package mk.proslavi

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object Fmt {
    private val mk = Locale.forLanguageTag("mk-MK")
    private val number = NumberFormat.getNumberInstance(mk).apply { maximumFractionDigits = 0 }
    private val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", mk)
    private val shortDate = DateTimeFormatter.ofPattern("d.M.yyyy", mk)

    fun money(value: Double): String = number.format(value) + " ден."

    fun localDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun date(millis: Long?, empty: String = "Без датум"): String =
        millis?.let { localDate(it).format(dateFormat) } ?: empty

    fun shortDate(millis: Long?): String = millis?.let { localDate(it).format(shortDate) } ?: ""

    fun daysUntil(millis: Long): Long = ChronoUnit.DAYS.between(LocalDate.now(), localDate(millis))

    fun countdown(millis: Long?): String {
        if (millis == null) return "Датумот уште не е одреден"
        val d = daysUntil(millis)
        return when {
            d > 1 -> "Уште $d дена"
            d == 1L -> "Утре е денот!"
            d == 0L -> "Денес е денот!"
            else -> "Поминато пред ${-d} дена"
        }
    }

    fun parseAmount(text: String): Double =
        text.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0

    fun amountInput(value: Double): String = if (value == 0.0) "" else value.toLong().toString()
}
