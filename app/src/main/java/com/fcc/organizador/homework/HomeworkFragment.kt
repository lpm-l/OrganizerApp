package com.fcc.organizador.homework

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fcc.organizador.databinding.DialogHomeworkSelectedBinding
import com.fcc.organizador.databinding.FragmentHomeworkBinding
import com.fcc.organizador.db.AppDatabaseHelper
import com.fcc.organizador.homework.adapter.HomeworkAdapter
import com.fcc.organizador.homework.adapter.HomeworkViewHolder
import com.fcc.organizador.homework.notification.cancelNotification
import com.fcc.organizador.homework.notification.scheduleExactNotification
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeworkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeworkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentHomeworkBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeworkMutableList: MutableList<Homework>
    private lateinit var adapter: HomeworkAdapter
    private lateinit var llmanager: LinearLayoutManager
    private lateinit var homeworkViewModel: HomeworkViewModel
    private lateinit var db: AppDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        homeworkViewModel = ViewModelProvider(requireActivity())[HomeworkViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabaseHelper(requireContext())
        homeworkMutableList = db.getAllHomework() //Initialize homework mutable list

        val homeworkAddedObserver = Observer<Homework?>{ homework ->
            if (homework!=null && !homeworkViewModel.getEditing()){
                addHomework(homework)
                homeworkViewModel.homeworkAdded()
            }
        }

        homeworkViewModel.getNewHomework().observe(viewLifecycleOwner, homeworkAddedObserver)

        binding.addHomeworkFloatingButton.setOnClickListener { createHomework() }

        val homeworkEditedObserver = Observer<Homework?>{ homework ->
            if (homework!=null && homeworkViewModel.getEditing()){
                editedHomework(homework)
                homeworkViewModel.homeworkEdited()
            }
        }

        homeworkViewModel.getEditHomework().observe(viewLifecycleOwner, homeworkEditedObserver)

        llmanager = LinearLayoutManager(requireContext())
        initRecyclerView()

        val itemTouchHelperCallback = object : ItemTouchHelper.Callback(){// This is to permit move the teachers list, change the order
        //and delete when the element swipes to the left
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT) )
        }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
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
                    HomeworkViewHolder.handleSwipe(HomeworkViewHolder(viewHolder.itemView), dX)
                } else {
                    super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                //Reset position when item is released
                viewHolder.itemView.translationX = 0f
                HomeworkViewHolder(viewHolder.itemView).apply {
                    editBackground.visibility = View.GONE
                    deleteBackground.visibility = View.GONE
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerHomework)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(){
        adapter = HomeworkAdapter(
            homeworkList = homeworkMutableList,
            onClickListener = { homework -> onItemSelected(homework) }
        )

        val decoration = DividerItemDecoration(requireContext(), llmanager.orientation)
        val recyclerView = binding.recyclerHomework

        checkIfEmpty()

        recyclerView.layoutManager = llmanager
        recyclerView.adapter = adapter
        binding.recyclerHomework.addItemDecoration(decoration)
    }

    private fun checkIfEmpty() {
        if (homeworkMutableList.isEmpty()) {
            binding.recyclerHomework.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerHomework.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun onItemSelected(homework: Homework){
        //Toast.makeText(requireContext(), homework.title + " " + homework.id, Toast.LENGTH_SHORT).show()

        val dialogBinding = DialogHomeworkSelectedBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        with(dialogBinding) {
            textViewHomeworkTitle.text = homework.title
            textViewHomeworkDateText.text = homework.dateText
            textViewHomeworkTimeText.text = homework.timeText
            textViewHomeworkDescription.text = homework.description

            btnClose.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()
    }

    private fun onDeletedItem(position: Int, id: Int){
        homeworkMutableList.removeAt(position)
        checkIfEmpty()
        db.deleteHomework(id)
        adapter.notifyItemRemoved(position)
    }

    private fun createHomework() {
        val dialog = FullScreenDialogHomeworkFragment.newInstance()
        dialog.show(parentFragmentManager, "AddTeacherDialog")
        homeworkViewModel.setEditing(false)
        homeworkViewModel.setHomeworkListLastPosition(homeworkMutableList.size - 1)
    }

    private fun addHomework(homework: Homework) {
        homeworkMutableList.add(homework)

        db.insertHomework(homework)

        checkIfEmpty()

        adapter.notifyItemInserted(homeworkMutableList.size - 1)
        llmanager.scrollToPositionWithOffset(homeworkMutableList.size - 1, 10)

        homework.id =  (System.currentTimeMillis() + homework.hashCode()).toInt()

        scheduleExactNotification(requireContext(), homework)

        //Toast.makeText(requireContext(), "${homework.title} agregado", Toast.LENGTH_SHORT).show()
    }

    private fun restoreHomework(position: Int, homework: Homework){
        homeworkMutableList.add(position, homework)

        val id = db.insertHomework(homework)
        homework.id = id //the is reassigned by database

        checkIfEmpty()

        adapter.notifyItemInserted(position)

        binding.recyclerHomework.post { //This is to fix when a teacher is restored because if the item is not reDrawn, the item will
            //be an empty card
            val holder = binding.recyclerHomework.findViewHolderForAdapterPosition(position)
            holder?.let {
                HomeworkViewHolder(it.itemView).resetSwipePosition()
            }
        }

        scheduleExactNotification(requireContext(), homework) // re-do the notification

        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher
    }

    private fun deleteFunction(position: Int){
        val deletedHomework = homeworkMutableList[position]

        cancelNotification(requireContext(), deletedHomework.id)

        onDeletedItem(position, deletedHomework.id)
        val snackbar = Snackbar.make(binding.root, "Tarea eliminada", Snackbar.LENGTH_LONG)
        snackbar.setAction("Deshacer") {
            restoreHomework(position, deletedHomework)
        }
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }

    private fun editFunction(position: Int){
        //Toast.makeText(requireContext(), "Edit Function", Toast.LENGTH_SHORT).show()
        adapter.notifyItemChanged(position)
        binding.recyclerHomework.post { //This is to fix when a teacher is restored because if the item is not reDrawn, the item will
            //be an empty card
            val holder = binding.recyclerHomework.findViewHolderForAdapterPosition(position)
            holder?.let {
                HomeworkViewHolder(it.itemView).resetSwipePosition()
            }
        }
        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher

        val editingHomework = homeworkMutableList[position]
        homeworkViewModel.setEditing(true)
        homeworkViewModel.setEditingHomework(editingHomework)
        homeworkViewModel.setEditedPosition(position)

        val dialog = FullScreenDialogHomeworkFragment.newInstance()
        dialog.show(parentFragmentManager, "AddTeacherDialog")


    }

    private fun editedHomework(homework: Homework){
        val position = homeworkViewModel.getEditedPosition() //obtain the correct position
        //to edit
        homeworkMutableList[position] = homework

        db.updateHomework(homework)

        cancelNotification(requireContext(), homework.id)
        scheduleExactNotification(requireContext(), homework)

        adapter.notifyItemChanged(position)
        llmanager.scrollToPositionWithOffset(position, 10) //Correct position to see the restored teacher
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeworkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeworkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}