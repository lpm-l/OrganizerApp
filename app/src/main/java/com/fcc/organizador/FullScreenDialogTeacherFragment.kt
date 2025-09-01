package com.fcc.organizador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.fcc.organizador.databinding.DialogTeacherBinding
import com.fcc.organizador.db.AppDatabaseHelper

class FullScreenDialogTeacherFragment: DialogFragment() {
    private var _binding: DialogTeacherBinding? = null
    private val binding get() = _binding!!
    private lateinit var teacherViewModel: TeacherViewModel
    private lateinit var db: AppDatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teacherViewModel = ViewModelProvider(requireActivity()).get(TeacherViewModel::class.java)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabaseHelper(requireContext())

        if (teacherViewModel.getEditing()){ //If the dialog was called by the edit swipe option
            fillOutTeacherInformation()
            binding.dialogTitle.text = "Editar Maestro"
        }else{
            binding.dialogTitle.text = "Agregar Nuevo Maestro"
        }

        binding.btnSave.setOnClickListener {
            saveTeacherInfo(teacherViewModel.getEditing())
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun fillOutTeacherInformation(){
        val teacher = teacherViewModel.getEditingTeacher()
        if (teacher != null) {
            binding.editTextName.setText(teacher.name)
            binding.editTextCubicle.setText(teacher.cubicle)
            binding.editTextMail.setText(teacher.contact)
            binding.editTextDescription.setText(teacher.description)
        }
    }

    private fun saveTeacherInfo(editing: Boolean){
        val name = binding.editTextName.text.toString().trim()
        val cubicle = binding.editTextCubicle.text.toString().trim()
        val email = binding.editTextMail.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        var position: Int
        if(!editing){
            position = teacherViewModel.getTeacherListLastPosition() + 1 //assign the position
            //with the value of the last teacher plus one
        }else{
            position = teacherViewModel.getEditedPosition() //maintain the the same position
            //because the teacher will stay at the same position while editing, a new value is not
            //needed
        }

        if (validateInputs(name, cubicle, email, description, editing)) {
            val newTeacher = Teacher(name, cubicle, email, description, position)

            if (!editing){
                teacherViewModel.setNewTeacher(newTeacher)
            }else{
                teacherViewModel.setEditTeacher(newTeacher)
            }

            dismiss()
        }
    }

    private fun validateInputs(name: String, cubicle: String, email: String, description: String, editing: Boolean): Boolean {
        if (!editing && db.teacherNameExists(name)){
            //Toast.makeText(context, "Ya hay un profesor con ese nombre, registra otro", Toast.LENGTH_SHORT).show()
            binding.nameLayout.error = "Ya hay un maestro con ese nombre"
            return false
        }
        var validated = true

        if(name == ""){
            binding.nameLayout.error = "Ingresa un nombre"
            validated = false
        }else{
            binding.nameLayout.error = null
        }

        if(cubicle == ""){
            binding.cubicleLayout.error = "Ingresa el cubículo"
            validated = false
        }else{
            binding.cubicleLayout.error = null
        }

        if(email == ""){
            binding.emailLayout.error = "Ingresa el email"
            validated = false
        }else{
            binding.emailLayout.error = null
        }

        //Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        return validated


    }

    companion object {
        fun newInstance() = FullScreenDialogTeacherFragment()
    }
}