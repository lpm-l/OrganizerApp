package com.fcc.organizador.homework.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fcc.organizador.databinding.ItemHomeworkBinding
import com.fcc.organizador.homework.Homework
import kotlin.math.min

class HomeworkViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemHomeworkBinding.bind(view)
    val foregroundView = binding.foregroundView //These three references are to the swipe to the left and right functions
    val editBackground = binding.editBackground
    val deleteBackground = binding.deleteBackground

    fun render(homework: Homework, onClickListener: (Homework) -> Unit){
        binding.homeworkTitle.text = homework.title
        binding.homeworkDueTime.text = "Fecha programada: ${homework.dateText}"
        binding.homeworkTimeText.text = "Hora programada: ${homework.timeText}"
        //binding.homeworkDescription.text = homework.description

        binding.homeworkCardItem.setOnClickListener { onClickListener(homework) }

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
        fun handleSwipe(holder: HomeworkViewHolder, dX: Float) { //This is the logic to show an specific background behind the card
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