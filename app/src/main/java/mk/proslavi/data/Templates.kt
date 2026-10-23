package mk.proslavi.data

import java.time.LocalDate
import java.time.ZoneOffset

/** Почетни листи на задачи и распоред за нов настан. */
object Templates {

    // Пар: (колку дена пред настанот, задача)
    private val weddingTasks = listOf(
        365 to "Договорете буџет и приближен број на гости",
        330 to "Изберете датум и резервирајте ресторан/сала",
        300 to "Резервирајте фотограф и снимател",
        300 to "Резервирајте музика (бенд или DJ)",
        270 to "Изберете кумови и деверови",
        240 to "Договорете црковна венчавка",
        210 to "Изберете венчаница и костум",
        180 to "Направете список на гости",
        150 to "Нарачајте покани",
        120 to "Резервирајте декорација и цвеќе",
        120 to "Нарачајте торта",
        90 to "Испратете покани",
        90 to "Купете бурми",
        60 to "Закажете матичар за граѓанска венчавка",
        60 to "Договорете фризер и шминка",
        45 to "Изберете мени и пијалаци со ресторанот",
        30 to "Соберете ги потврдите од гостите",
        21 to "Распоред на седење по маси",
        14 to "Последна проба на венчаницата",
        7 to "Потврдете ги сите добавувачи и термини",
        2 to "Подгответе плик/пари за доплати на денот",
    )

    private val genericTasks = listOf(
        60 to "Договорете буџет и број на гости",
        50 to "Резервирајте простор",
        40 to "Направете список на гости",
        30 to "Испратете покани",
        21 to "Нарачајте торта",
        14 to "Договорете музика и декорација",
        7 to "Соберете потврди од гостите",
        2 to "Потврдете ги сите добавувачи",
    )

    private val weddingSchedule = listOf(
        "10:00" to "Фризер и шминка за невестата",
        "11:00" to "Бербер и облекување на младоженецот",
        "12:00" to "Свирење и собирање кај младоженецот",
        "13:30" to "Земање на кумот",
        "14:30" to "Земање на невестата",
        "16:00" to "Црковна венчавка",
        "17:30" to "Фотографирање",
        "19:00" to "Пречек на гостите во ресторанот",
        "20:00" to "Граѓанска венчавка",
        "20:30" to "Прв танц",
        "22:30" to "Сечење на тортата",
        "23:30" to "Фрлање на букетот",
    )

    fun tasksFor(type: EventType, dateMillis: Long?, eventId: Long): List<ChecklistTask> {
        val list = if (type == EventType.SVADBA) weddingTasks else genericTasks
        val eventDate = dateMillis?.let { LocalDate.ofEpochDay(it / 86_400_000L) }
        val today = LocalDate.now()
        return list.map { (daysBefore, title) ->
            val due = eventDate?.minusDays(daysBefore.toLong())?.let { if (it.isBefore(today)) today else it }
            ChecklistTask(
                eventId = eventId,
                title = title,
                dueMillis = due?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            )
        }
    }

    fun scheduleFor(type: EventType, eventId: Long): List<ScheduleItem> =
        if (type == EventType.SVADBA) weddingSchedule.map { (t, title) -> ScheduleItem(eventId = eventId, time = t, title = title) }
        else emptyList()
}
