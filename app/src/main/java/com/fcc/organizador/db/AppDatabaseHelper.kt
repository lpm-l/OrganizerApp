package com.fcc.organizador.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fcc.organizador.Teacher
import com.fcc.organizador.homework.Homework
import com.fcc.organizador.schedule.Schedule
import com.fcc.organizador.schedule.ScheduleProvider

//The app uses a unique database helper to create all the necessary tables, this class has all the methods to add and delete elements
//of all the tables
class AppDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "organizer_app.db"
        private const val DATABASE_VERSION = 6


        //TABLE: TEACHER
        private const val TABLE_TEACHER = "teacher"
        private const val COL_TEACHER_ID = "id"
        private const val COL_TEACHER_NAME = "name"
        private const val COL_TEACHER_CUBICLE = "cubicle"
        private const val COL_TEACHER_CONTACT = "contact"
        private const val COL_TEACHER_DESCRIPTION = "description"
        private const val COL_TEACHER_POSITION = "position"

        //TABLE: SCHEDULE
        private const val TABLE_SCHEDULE = "schedule_cells"
        private const val COL_SCHEDULE_ID = "id"
        private const val COL_SCHEDULE_TEXT = "content"
        private const val COL_SCHEDULE_COLOR = "color"
        private const val COL_SCHEDULE_POSITION = "position"
        private val scheduleList: List<Schedule> = ScheduleProvider.scheduleList

        //TABLE: Homework
        private const val TABLE_HOMEWORK = "homework"
        private const val COL_HOMEWORK_ID = "id"
        private const val COL_HOMEWORK_TITLE = "title"
        private const val COL_HOMEWORK_DESCRIPTION = "description"
        private const val COL_HOMEWORK_DUE_DATE_MILLIS = "due_date_millis"
        private const val COL_HOMEWORK_DATE_TEXT = "date_text"
        private const val COL_HOMEWORK_TIME_TEXT = "time_text"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //TABLE SCHEDULE
        var createTableQuery = "CREATE TABLE $TABLE_SCHEDULE ($COL_SCHEDULE_ID INTEGER PRIMARY KEY, $COL_SCHEDULE_TEXT TEXT NOT NULL, $COL_SCHEDULE_COLOR INTEGER NOT NULL, $COL_SCHEDULE_POSITION INTEGER NOT NULL UNIQUE)"
        db?.execSQL(createTableQuery)

        //Adding initial weekdays
        for (schedule in scheduleList){
            val values = ContentValues().apply{
                put(COL_SCHEDULE_TEXT, schedule.content)
                put(COL_SCHEDULE_COLOR, schedule.color)
                put(COL_SCHEDULE_POSITION, schedule.position)
            }
            db?.insert(TABLE_SCHEDULE, null, values)
        }

        //TABLE TEACHER
        createTableQuery = "CREATE TABLE $TABLE_TEACHER ($COL_TEACHER_ID INTEGER PRIMARY KEY NOT NULL, $COL_TEACHER_NAME TEXT UNIQUE NOT NULL, $COL_TEACHER_CUBICLE TEXT NOT NULL, $COL_TEACHER_DESCRIPTION TEXT NOT NULL, $COL_TEACHER_CONTACT TEXT NOT NULL, $COL_TEACHER_POSITION INTEGER NOT NULL)"
        db?.execSQL(createTableQuery)

        //TABLE HOMEWORK
        createTableQuery = "CREATE TABLE $TABLE_HOMEWORK ($COL_HOMEWORK_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_HOMEWORK_TITLE TEXT UNIQUE NOT NULL, $COL_HOMEWORK_DESCRIPTION TEXT NOT NULL, $COL_HOMEWORK_DUE_DATE_MILLIS INTEGER NOT NULL, $COL_HOMEWORK_DATE_TEXT TEXT NOT NULL, $COL_HOMEWORK_TIME_TEXT TEXT NOT NULL )"
        db?.execSQL(createTableQuery)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        var dropTableQuery = "DROP TABLE IF EXISTS $TABLE_SCHEDULE"
        db?.execSQL(dropTableQuery)
        dropTableQuery = "DROP TABLE IF EXISTS $TABLE_TEACHER"
        db?.execSQL(dropTableQuery)
        dropTableQuery = "DROP TABLE IF EXISTS $TABLE_HOMEWORK"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertScheduleCell(schedule: Schedule){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_SCHEDULE_TEXT, schedule.content)
            put(COL_SCHEDULE_COLOR, schedule.color)
            put(COL_SCHEDULE_POSITION, schedule.position)
        }
        db.insert(TABLE_SCHEDULE, null, values)
        db.close()
    }

    fun getAllScheduleCells(): MutableList<Schedule>{
        val scheduleList = mutableListOf<Schedule>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_SCHEDULE ORDER BY $COL_SCHEDULE_POSITION ASC"
        val cursor = db.rawQuery(query, null)

        with(cursor) {
            while (moveToNext()) {
                val content = getString(getColumnIndexOrThrow(COL_SCHEDULE_TEXT))
                val color = getInt(getColumnIndexOrThrow(COL_SCHEDULE_COLOR))
                val position = getInt(getColumnIndexOrThrow(COL_SCHEDULE_POSITION))

                scheduleList.add(Schedule(content, color, position))
            }
            close()
        }
        return scheduleList
    }

    fun deleteScheduleCell(position: Int){
        val db = writableDatabase
        try {
            db.delete(TABLE_SCHEDULE, "$COL_SCHEDULE_POSITION = ?", arrayOf(position.toString()))
        } finally {
            db.close()
        }
    }

    fun updateScheduleCell(schedule: Schedule){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_SCHEDULE_TEXT, schedule.content)
            put(COL_SCHEDULE_COLOR, schedule.color)
        }

        db.update(TABLE_SCHEDULE, values, "$COL_SCHEDULE_POSITION = ?", arrayOf(schedule.position.toString()))
        db.close()
    }

    fun insertTeacher(teacher: Teacher){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_TEACHER_NAME, teacher.name)
            put(COL_TEACHER_CUBICLE, teacher.cubicle)
            put(COL_TEACHER_CONTACT, teacher.contact)
            put(COL_TEACHER_DESCRIPTION, teacher.description)
            put(COL_TEACHER_POSITION, teacher.position)
        }
        db.insert(TABLE_TEACHER, null, values)
        db.close()
    }

    fun getAllTeachers(): MutableList<Teacher>{
        val teachersList = mutableListOf<Teacher>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_TEACHER ORDER BY $COL_TEACHER_POSITION ASC"
        val cursor = db.rawQuery(query, null)

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COL_TEACHER_NAME))
                val cubicle = getString(getColumnIndexOrThrow(COL_TEACHER_CUBICLE))
                val contact = getString(getColumnIndexOrThrow(COL_TEACHER_CONTACT))
                val description = getString(getColumnIndexOrThrow(COL_TEACHER_DESCRIPTION))
                val position = getInt(getColumnIndexOrThrow(COL_TEACHER_POSITION))

                teachersList.add(Teacher(name, cubicle, contact, description, position))
            }
            close()
        }
        return teachersList
    }

    fun deleteTeacher(position: Int){
        val db = writableDatabase
        try {
            db.delete(TABLE_TEACHER, "$COL_TEACHER_POSITION = ?", arrayOf(position.toString()))
        } finally {
            db.close()
        }
    }

    fun updateTeacher(teacher: Teacher){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_TEACHER_NAME, teacher.name)
            put(COL_TEACHER_CUBICLE, teacher.cubicle)
            put(COL_TEACHER_CONTACT, teacher.contact)
            put(COL_TEACHER_DESCRIPTION, teacher.description)
        }

        db.update(TABLE_TEACHER, values, "$COL_TEACHER_POSITION = ?", arrayOf(teacher.position.toString()))
        db.close()
    }

    fun teacherNameExists(name: String): Boolean {
        val db = readableDatabase
        return db.query(TABLE_TEACHER, arrayOf(COL_TEACHER_ID), "$COL_TEACHER_NAME = ?", arrayOf(name), null, null, null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    fun reorderTeacher(teacher: Teacher, newPosition: Int){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_TEACHER_POSITION, newPosition)
        }

        db.update(TABLE_TEACHER, values, "$COL_TEACHER_NAME = ?", arrayOf(teacher.name))
        db.close()
    }

    fun insertHomework(homework: Homework): Int{
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_HOMEWORK_TITLE, homework.title)
            put(COL_HOMEWORK_DESCRIPTION, homework.description)
            put(COL_HOMEWORK_DUE_DATE_MILLIS, homework.dueDateMillis)
            put(COL_HOMEWORK_DATE_TEXT, homework.dateText)
            put(COL_HOMEWORK_TIME_TEXT, homework.timeText)
        }
        val id = db.insert(TABLE_HOMEWORK, null, values).toInt() //return the id of the inserted homework
        db.close()
        return id
    }

    fun getAllHomework(): MutableList<Homework>{
        val homeworkList = mutableListOf<Homework>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_HOMEWORK ORDER BY $COL_HOMEWORK_DUE_DATE_MILLIS ASC"
        val cursor = db.rawQuery(query, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COL_HOMEWORK_ID))
                val title = getString(getColumnIndexOrThrow(COL_HOMEWORK_TITLE))
                val description = getString(getColumnIndexOrThrow(COL_HOMEWORK_DESCRIPTION))
                val dueDateMillis = getLong(getColumnIndexOrThrow(COL_HOMEWORK_DUE_DATE_MILLIS))
                val dateText = getString(getColumnIndexOrThrow(COL_HOMEWORK_DATE_TEXT))
                val timeText = getString(getColumnIndexOrThrow(COL_HOMEWORK_TIME_TEXT))

                homeworkList.add(Homework(id, title, description, dueDateMillis, dateText, timeText))
            }
            close()
        }
        return homeworkList
    }

    fun deleteHomework(id: Int){
        val db = writableDatabase
        try {
            db.delete(TABLE_HOMEWORK, "$COL_HOMEWORK_ID = $id", null)
        } finally {
            db.close()
        }
    }

    fun updateHomework(homework: Homework){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(COL_HOMEWORK_TITLE, homework.title)
            put(COL_HOMEWORK_DESCRIPTION, homework.description)
            put(COL_HOMEWORK_DUE_DATE_MILLIS, homework.dueDateMillis)
            put(COL_HOMEWORK_DATE_TEXT, homework.dateText)
            put(COL_HOMEWORK_TIME_TEXT, homework.timeText)
        }

        db.update(TABLE_HOMEWORK, values, "$COL_HOMEWORK_ID = ?", arrayOf(homework.id.toString()))
        db.close()
    }

    fun homeworkTitleExists(title: String): Boolean {
        val db = readableDatabase
        return db.query(
            TABLE_HOMEWORK, arrayOf(COL_HOMEWORK_ID), "$COL_HOMEWORK_TITLE = ?", arrayOf(title), null, null, null
        ).use { cursor ->
            cursor.count > 0
        }
    }
}