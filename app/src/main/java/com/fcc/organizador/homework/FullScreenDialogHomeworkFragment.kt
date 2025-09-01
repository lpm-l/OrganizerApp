package com.fcc.organizador.homework

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.fcc.organizador.R
import com.fcc.organizador.databinding.DialogHomeworkBinding
import com.fcc.organizador.db.AppDatabaseHelper
import java.util.Calendar

class FullScreenDialogHomeworkFragment: DialogFragment() {
    private var _binding: DialogHomeworkBinding? = null

    private val binding get() = _binding!!
    private lateinit var homeworkViewModel: HomeworkViewModel
    private lateinit var db: AppDatabaseHelper

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = 0
    private var selectedMinute = 0
    private var dueTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeworkViewModel = ViewModelProvider(requireActivity()).get(HomeworkViewModel::class.java)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabaseHelper(requireContext())

        if (homeworkViewModel.getEditing()){ //If the dialog was called by the edit swipe option
            fillOutHomeworkInformation()
            binding.dialogTitle.text = "Editar Tarea"
        }else{
            binding.dialogTitle.text = "Agregar Nueva Tarea"
        }

        binding.btnSave.setOnClickListener {
            saveHomeworkInfo(homeworkViewModel.getEditing())
        }

        binding.textViewDate.setOnClickListener {
            showDatePicker()
        }

        binding.textViewTime.setOnClickListener {
            if (binding.textViewDate.text == "Seleccionar fecha") {
                Toast.makeText(context, "Primero selecciona una fecha", Toast.LENGTH_SHORT).show()
            } else {
                showTimePicker()
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun fillOutHomeworkInformation() {
        val homework = homeworkViewModel.getEditingHomework()
        if (homework != null) {
            binding.editTextTitle.setText(homework.title)
            binding.editTextDescription.setText(homework.description)

            if (homework.dueDateMillis > 0) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = homework.dueDateMillis
                }

                selectedYear = calendar.get(Calendar.YEAR)
                selectedMonth = calendar.get(Calendar.MONTH)
                selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
                selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
                selectedMinute = calendar.get(Calendar.MINUTE)

                val dateText = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
                val timeText = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"

                binding.textViewDate.text = dateText
                binding.textViewTime.text = timeText

                dueTimeMillis = homework.dueDateMillis
            }
        }
    }

    private fun saveHomeworkInfo(editing: Boolean) {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()

        if (!validation(title, editing)) return //only continue if the homework is validated

        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueTimeMillis
        }

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)

        val dateText = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
        val timeText = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"

        // the id value is provisional if the homework is being created
        val idValue = if(editing && homeworkViewModel.getEditingHomework()!= null) homeworkViewModel.getEditingHomework()!!.id else 0
        val homework = Homework(idValue, title, description, dueTimeMillis, dateText, timeText)

        if (!editing) {
            homeworkViewModel.setNewHomework(homework)
        } else {
            homeworkViewModel.setEditHomework(homework)
        }

        dismiss()
    }

    private fun validation(title: String, editing: Boolean): Boolean{
        var validated = true

        if (title.isEmpty()) {
            binding.titleLayout.error = "Ingresa un título"
            validated = false
        }else if (!editing && db.homeworkTitleExists(title)){
            binding.titleLayout.error = "El titulo ya fue registrado"
            validated = false
        }else{
            binding.titleLayout.error = null
        }

        if (binding.textViewDate.text == "Seleccionar fecha"){
            binding.datePickerLayout.error = "Ingresa una fecha"
            validated = false
        }else{
            binding.datePickerLayout.error = null
        }

        if (binding.textViewTime.text == "Seleccionar hora"){
            binding.timePickerLayout.error = "Ingresa una hora"
            validated = false
        }else{
            binding.timePickerLayout.error = null
        }
        return validated
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                binding.textViewDate.text = "$day/${month + 1}/$year"
                showTimePicker()
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                binding.textViewTime.text = "$hour:$minute"

                // Guardar fecha completa en millis
                val calendarSelected = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, hour, minute, 0)
                }
                dueTimeMillis = calendarSelected.timeInMillis
            },
            selectedHour,
            selectedMinute,
            false
        )
        timePickerDialog.show()
    }

    companion object {
        fun newInstance() = FullScreenDialogHomeworkFragment()
    }


}