package com.fcc.organizador.homework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.organizador.R
import com.fcc.organizador.homework.Homework

class HomeworkAdapter(
    private val homeworkList: List<Homework>,
    private val onClickListener: (Homework) -> Unit
): RecyclerView.Adapter<HomeworkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HomeworkViewHolder(layoutInflater.inflate(R.layout.item_homework, parent, false))
    }

    override fun getItemCount(): Int {
        return homeworkList.size
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        val item = homeworkList[position]
        holder.render(item, onClickListener)
    }
}