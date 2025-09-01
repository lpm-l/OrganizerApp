package com.fcc.organizador.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fcc.organizador.Teacher
import com.fcc.organizador.databinding.ItemTeacherBinding
import kotlin.math.min

class TeacherViewHolder(view: View): ViewHolder(view) {

    private val binding = ItemTeacherBinding.bind(view)
    val foregroundView = binding.foregroundView //These three references are to the swipe to the left and right functions
    val editBackground = binding.editBackground
    val deleteBackground = binding.deleteBackground

    fun render(teacher: Teacher, onClickListener: (Teacher) -> Unit, onClickDelete: (Int) -> Unit){
        binding.teacherName.text = teacher.name
        binding.teacherCubicle.text = teacher.cubicle
        binding.teacherContact.text = teacher.contact
        //binding.teacherDescription.text = teacher.description

        binding.teacherCardItem.setOnClickListener { onClickListener(teacher) }

        resetSwipePosition()
    }

    fun resetSwipePosition() {
        foregroundView.translationX = 0f
        editBackground.visibility = View.GONE
        deleteBackground.visibility = View.GONE
    }

    companion object {
        const val SWIPE_EDIT = 1
        const val SWIPE_DELETE = -1

        // Handle the displacement
        fun handleSwipe(holder: TeacherViewHolder, dX: Float) { //This is the logic to show an specific background behind the card
            //the alpha is for an visual effect, the option is "appearing"
            when {
                dX > 0 -> { // Right swipe (edit)
                    holder.deleteBackground.visibility = View.GONE
                    holder.editBackground.visibility = View.VISIBLE
                    holder.editBackground.alpha = min(1f, dX / holder.itemView.width * 2)
                }
                dX < 0 -> { // Left swipe (delete)
                    holder.editBackground.visibility = View.GONE
                    holder.deleteBackground.visibility = View.VISIBLE
                    holder.deleteBackground.alpha = min(1f, -dX / holder.itemView.width * 2)
                }
            }
            holder.foregroundView.translationX = dX
        }
    }

}