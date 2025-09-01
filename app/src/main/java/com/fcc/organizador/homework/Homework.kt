package com.fcc.organizador.homework

data class Homework (
    var id: Int,
    var title: String,
    var description: String,
    var dueDateMillis: Long,
    var dateText: String,
    var timeText: String
)