package com.fcc.organizador.homework

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeworkViewModel: ViewModel() {
    private var newHomework: MutableLiveData<Homework?> = MutableLiveData()
    private var editing = false //This variable will be checked when the FullScreenDialogHomeworkFragment is called, if the floating
    //button was pressed then editing will be false, but if the HomeworkItem is swiped to the right then editing will be true
    private var editingHomework: Homework? = null //This homework object is for the edit logic
    private var editHomework: MutableLiveData<Homework?> = MutableLiveData() //This MutableLiveData teacher is for the edit logic
    //an observer will be save the teacher value saved here
    private var editedPosition: Int = 0 //save the position value to know which teacher edit
    private var homeworkListLastPosition = 0 //save the position that will be used as reference for a new homework

    fun getNewHomework(): MutableLiveData<Homework?> {
        return newHomework
    }

    fun homeworkAdded(){//This function is used by the FullScreenDialogTeacherFragment when the teacher is correctly added
        //it is to make the observer know that it doesn't have to add a new teacher until the MutableLiveData is not null
        newHomework.value = null
    }

    fun setNewHomework(h: Homework){
        newHomework.value = h
    }

    fun getEditing(): Boolean{
        return editing
    }

    fun setEditing(value: Boolean){
        editing = value
    }

    fun getEditingHomework(): Homework?{
        return editingHomework
    }

    fun setEditingHomework(homework: Homework){
        editingHomework = homework
    }

    fun getEditHomework(): MutableLiveData<Homework?> {
        return editHomework
    }

    fun homeworkEdited(){//This function is used by the FullScreenDialogHomeworkFragment when the fragment is correctly added.
        //It is to make the observer know that it doesn't have to add a new homework until the MutableLiveData is not null
        editHomework.value = null
    }

    fun setEditHomework(h: Homework){
        editHomework.value = h
    }

    fun getEditedPosition(): Int{
        return editedPosition
    }

    fun setEditedPosition(position: Int){
        editedPosition = position
    }

    fun getHomeworkListLastPosition(): Int{
        return homeworkListLastPosition
    }

    fun setHomeworkListLastPosition(value: Int){
        homeworkListLastPosition = value
    }
}