package com.fcc.organizador.schedule

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.organizador.R
import com.fcc.organizador.databinding.FragmentScheduleBinding
import com.fcc.organizador.db.AppDatabaseHelper
import com.fcc.organizador.schedule.adapter.ScheduleAdapter
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var scheduleMutableList: MutableList<Schedule>
    private lateinit var adapter: ScheduleAdapter
    private lateinit var glmanager: LinearLayoutManager
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var db: AppDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        scheduleViewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabaseHelper(requireContext())
        scheduleMutableList = db.getAllScheduleCells() //Initialize schedule mutable list

        glmanager = GridLayoutManager(requireContext(),scheduleViewModel.getColumnsCount())
        initRecyclerView()
        binding.btnAddRow.setOnClickListener { addRowSection() }
        binding.btnDeleteRow.setOnClickListener { deleteRowSection() }
    }

    private fun initRecyclerView(){
        adapter = ScheduleAdapter(
            scheduleList = scheduleMutableList,
            onClickListener = { schedule -> onItemSelected(schedule) }
        )

        val recyclerView = binding.recyclerSchedule

        recyclerView.layoutManager = glmanager
        recyclerView.adapter = adapter
    }

    private fun addRowSection(){
        val columnsCount = scheduleViewModel.getColumnsCount()
        val startPosition = scheduleMutableList.size
        var cellSchedule: Schedule
        var newPosition = startPosition

        for (i in 1..columnsCount){
            cellSchedule = Schedule("Presiona para editar", Color.argb(255,249, 231, 151), newPosition)
            scheduleMutableList.add(cellSchedule)
            db.insertScheduleCell(cellSchedule)
            newPosition++
        }
        adapter.notifyItemRangeInserted(startPosition, columnsCount)
    }

    private fun deleteRowSection(){
        val columnsCount = scheduleViewModel.getColumnsCount()

        if (scheduleMutableList.size <= columnsCount){
            Toast.makeText(requireContext(), "No se puede eliminar la primera fila", Toast.LENGTH_SHORT).show()
            return
        }

        val rangePositions = scheduleMutableList.size - columnsCount
        for (i in 1..columnsCount){
            val removingPosition = scheduleMutableList.size - 1
            scheduleMutableList.removeAt(removingPosition)
            db.deleteScheduleCell(removingPosition)
        }
        adapter.notifyItemRangeRemoved(rangePositions, columnsCount)

    }

    private fun onItemSelected(schedule: Schedule){
        //Toast.makeText(requireContext(), schedule.toString(), Toast.LENGTH_SHORT).show()

        val dialogView = layoutInflater.inflate(R.layout.dialog_config_schedule, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextActivity)
        val viewColor = dialogView.findViewById<View>(R.id.viewColorPreview)
        val btnPickColor = dialogView.findViewById<Button>(R.id.btnPickColor)
        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        val btnCancel= dialogView.findViewById<Button>(R.id.btnClose)

        var selectedColor: Int = schedule.color //initial color
        if (schedule.content == "Presiona para editar"){
            editText.setText("")
        }else{
            editText.setText(schedule.content)
        }

        viewColor.setBackgroundColor(selectedColor)

        btnPickColor.setOnClickListener {
            ColorPickerDialog.Builder(requireContext())
                .setTitle("Selecciona un color")
                .setPreferenceName("ColorPickerDialog")
                .setPositiveButton("Aceptar", ColorEnvelopeListener { envelope, _ ->
                    selectedColor = envelope.color
                    viewColor.setBackgroundColor(selectedColor)
                })
                .setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(true) //
                .attachBrightnessSlideBar(true)
                .show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnAccept.setOnClickListener {
            var content = editText.text.toString()
            if (content == ""){
                content = "Click para editar"
            }
            schedule.content = content
            schedule.color = selectedColor
            val updatedSchedule = Schedule(content, selectedColor, schedule.position)
            db.updateScheduleCell(updatedSchedule)
            adapter.notifyItemChanged(schedule.position)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScheduleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}