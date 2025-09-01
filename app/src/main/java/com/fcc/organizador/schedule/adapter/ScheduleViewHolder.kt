package com.fcc.organizador.schedule.adapter

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fcc.organizador.databinding.ItemScheduleBinding
import com.fcc.organizador.schedule.Schedule
import kotlin.math.pow

class ScheduleViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemScheduleBinding.bind(view)

    fun render(schedule: Schedule, onClickListener: (Schedule) -> Unit){
        binding.scheduleCellText.text = schedule.content
        binding.scheduleCellText.setBackgroundColor(schedule.color)
        binding.scheduleCard.setOnClickListener { onClickListener(schedule) }

        val textColor = schedule.color.getContrastColor()
        binding.scheduleCellText.setTextColor(textColor)

    }

    private fun Int.getContrastColor(): Int {
        // WCAG formula
        val r = Color.red(this) / 255f
        val g = Color.green(this) / 255f
        val b = Color.blue(this) / 255f

        val luminance = 0.2126f * r.srgbToLinear() +
                0.7152f * g.srgbToLinear() +
                0.0722f * b.srgbToLinear()

        return if (luminance > 0.179f) Color.BLACK else Color.WHITE //Decide what textColor use to improve its visibility
    }

    private fun Float.srgbToLinear(): Float {
        return if (this <= 0.04045f) {
            this / 12.92f
        } else {
            ((this + 0.055f) / 1.055f).pow(2.4f)
        }
    }
}