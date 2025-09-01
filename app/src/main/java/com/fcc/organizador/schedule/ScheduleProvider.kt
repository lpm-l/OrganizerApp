package com.fcc.organizador.schedule

import android.graphics.Color

class ScheduleProvider {

    companion object{
        val scheduleList = listOf(
            Schedule("Horario",  Color.parseColor("#C42021"), 0), Schedule("Lunes", Color.parseColor("#007991"), 1), Schedule("Martes", Color.parseColor("#439A86"), 2),
            Schedule("Miércoles", Color.parseColor("#9E1946"), 3), Schedule("Jueves", Color.parseColor("#E9D985"), 4), Schedule("Viernes", Color.parseColor("#EDAFB8"), 5),
            Schedule("Sábado", Color.parseColor("#BCD8C1"), 6)
        )
    }
}