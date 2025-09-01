package com.fcc.organizador

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fcc.organizador.adapter.TeacherAdapter
import com.fcc.organizador.adapter.TeacherViewHolder
import com.fcc.organizador.databinding.DialogTeacherSelectedBinding
import com.fcc.organizador.databinding.FragmentTeachersBinding
import com.fcc.organizador.databinding.HelpScreenBinding
import com.fcc.organizador.db.AppDatabaseHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeachersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeachersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentTeachersBinding? = null
    private val binding get() = _binding!!
    private lateinit var teacherMutableList: MutableList<Teacher>
    private lateinit var adapter: TeacherAdapter
    private lateinit var llmanager: LinearLayoutManager
    private lateinit var teacherViewModel: TeacherViewModel
    private lateinit var db: AppDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        teacherViewModel = ViewModelProvider(requireActivity())[TeacherViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val teacherAddedObserver = Observer<Teacher?>{ teacher ->
            if (teacher!=null && !teacherViewModel.getEditing()){
                addTeacher(teacher)
                teacherViewModel.teacherAdded()
            }
        }

        teacherViewModel.getNewTeacher().observe(viewLifecycleOwner, teacherAddedObserver)

        binding.addTeacherFloatingButton.setOnClickListener { createTeacher() }

        val teacherEditedObserver = Observer<Teacher?>{teacher ->
            if ( teacher!=null && teacherViewModel.getEditing()){
                editedTeacher(teacher)
                teacherViewModel.teacherEdited()
            }
        }

        teacherViewModel.getEditTeacher().observe(viewLifecycleOwner, teacherEditedObserver)

        db = AppDatabaseHelper(requireContext())
        teacherMutableList = db.getAllTeachers() //Initialize teacher mutable list

        llmanager = LinearLayoutManager(requireContext())
        initRecyclerView()

        val itemTouchHelperCallback = object : ItemTouchHelper.Callback(){// This is to permit move the teachers list, change the order
            //and delete when the element swipes to the left
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT) )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val initialPosition = viewHolder.adapterPosition
                val finalPosition = target.adapterPosition
                changeTeacherPosition(initialPosition, finalPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //Toast.makeText(requireContext(), direction.toString(), Toast.LENGTH_SHORT).show()
                val position = viewHolder.adapterPosition

                when (direction){
                    ItemTouchHelper.LEFT ->{
                        deleteFunction(position)
                    }
                    ItemTouchHelper.RIGHT ->{
                        editFunction(position)
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    TeacherViewHolder.handleSwipe(TeacherViewHolder(viewHolder.itemView), dX)
                } else {
                    super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                //Reset position when item is released
                viewHolder.itemView.translationX = 0f
                TeacherViewHolder(viewHolder.itemView).apply {
                    editBackground.visibility = View.GONE
                    deleteBackground.visibility = View.GONE
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerTeachers)

        binding.btnHelp.setOnClickListener {
            it.animate().rotationBy(360f).setDuration(300).start()
            showHelpScreen()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTeachersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(){
        adapter = TeacherAdapter(
            teacherList = teacherMutableList,
            onClickListener = { teacher -> onItemSelected(teacher) },
            onClickDelete = { position -> onDeletedItem(position) }
        )

        val decoration = DividerItemDecoration(requireContext(), llmanager.orientation)
        val recyclerView = binding.recyclerTeachers

        checkIfEmpty()

        recyclerView.layoutManager = llmanager
        recyclerView.adapter = adapter
        binding.recyclerTeachers.addItemDecoration(decoration)
    }

    private fun checkIfEmpty() {
        if (teacherMutableList.isEmpty()) {
            binding.recyclerTeachers.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerTeachers.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun onItemSelected(teacher: Teacher){
        //Toast.makeText(requireContext(), teacher.position.toString(), Toast.LENGTH_SHORT).show()

        val dialogBinding = DialogTeacherSelectedBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        with(dialogBinding) {
            textViewName.text = teacher.name
            textViewCubicle.text = teacher.cubicle
            textViewMail.text = teacher.contact
            textViewDescription.text = teacher.description

            btnClose.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()

    }

    private fun onDeletedItem(position: Int){
        teacherMutableList.removeAt(position)
        checkIfEmpty()
        db.deleteTeacher(position)
        adapter.notifyItemRemoved(position)
        reorderTeachersPositions()
    }

    private fun createTeacher() {
        val dialog = FullScreenDialogTeacherFragment.newInstance()
        dialog.show(parentFragmentManager, "AddTeacherDialog")
        teacherViewModel.setEditing(false)
        teacherViewModel.setTeacherListLastPosition(teacherMutableList.size - 1)
    }

    private fun addTeacher(teacher: Teacher) {
        teacherMutableList.add(teacher)
        checkIfEmpty()
        db.insertTeacher(teacher)

        adapter.notifyItemInserted(teacherMutableList.size - 1)
        llmanager.scrollToPositionWithOffset(teacherMutableList.size - 1, 10)

        //Toast.makeText(requireContext(), "${teacher.name} agregado", Toast.LENGTH_SHORT).show()
    }

    private fun restoreTeacher(position: Int, teacher: Teacher){
        teacherMutableList.add(position, teacher)
        checkIfEmpty()
        db.insertTeacher(teacher)
        adapter.notifyItemInserted(position)

        binding.recyclerTeachers.post { //This is to fix when a teacher is restored because if the item is not reDrawn, the item will
            //be an empty card
            val holder = binding.recyclerTeachers.findViewHolderForAdapterPosition(position)
            holder?.let {
                TeacherViewHolder(it.itemView).resetSwipePosition()
            }
        }
        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher
        reorderTeachersPositions()
    }

    private fun changeTeacherPosition(initialPosition: Int, finalPosition: Int){
        val teacher = teacherMutableList[initialPosition]
        teacherMutableList.removeAt(initialPosition)
        teacherMutableList.add(finalPosition, teacher)
        adapter.notifyItemMoved(initialPosition, finalPosition)
        reorderTeachersPositions()
    }

    private fun deleteFunction(position: Int){
        val deletedTeacher = teacherMutableList[position]
        onDeletedItem(position)
        val snackbar = Snackbar.make(binding.root, "Maestro eliminado", Snackbar.LENGTH_LONG)
        snackbar.setAction("Deshacer") {
            restoreTeacher(position, deletedTeacher)
        }
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }

    private fun editFunction(position: Int){
        //Toast.makeText(requireContext(), "Edit Function", Toast.LENGTH_SHORT).show()
        adapter.notifyItemChanged(position)
        binding.recyclerTeachers.post { //This is to fix when a teacher is restored because if the item is not reDrawn, the item will
            //be an empty card
            val holder = binding.recyclerTeachers.findViewHolderForAdapterPosition(position)
            holder?.let {
                TeacherViewHolder(it.itemView).resetSwipePosition()
            }
        }
        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher

        val editingTeacher = teacherMutableList[position]
        teacherViewModel.setEditing(true)
        teacherViewModel.setEditingTeacher(editingTeacher)
        teacherViewModel.setEditedPosition(position)

        val dialog = FullScreenDialogTeacherFragment.newInstance()
        dialog.show(parentFragmentManager, "AddTeacherDialog")


    }

    private fun editedTeacher(teacher: Teacher){
        val position = teacherViewModel.getEditedPosition() //obtain the correct position
        //to edit
        teacherMutableList[position] = teacher
        db.updateTeacher(teacher)
        adapter.notifyItemChanged(position)
        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher
    }

    private fun reorderTeachersPositions(){
        for ((index,teacher) in teacherMutableList.withIndex()){
            db.reorderTeacher(teacher, index)
            teacher.position = index
        }
    }

    private fun showHelpScreen() {
        val dialogBinding = HelpScreenBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.apply {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = (displayMetrics.widthPixels * 0.9).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Opcional: hacer más alto si es necesario
            val height = (displayMetrics.heightPixels * 0.8).toInt()
            setLayout(width, height)

            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }


        dialogBinding.btnCloseHelp.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeachersFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeachersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}