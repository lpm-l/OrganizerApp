package com.fcc.organizador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fcc.organizador.databinding.FragmentDashboardBinding
import com.fcc.organizador.db.AppDatabaseHelper

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabaseHelper(requireContext())
        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val allHomework = db.getAllHomework()
        val currentTime = System.currentTimeMillis()

        var completedCount = 0
        var pendingCount = 0
        var overdueCount = 0

        for (homework in allHomework) {
            if (homework.statusCompleted) {
                completedCount++
            } else {
                if (currentTime > homework.dueDateMillis) {
                    overdueCount++
                } else {
                    pendingCount++
                }
            }
        }

        binding.tvCompletedValue.text = completedCount.toString()
        binding.tvPendingValue.text = pendingCount.toString()
        binding.tvOverdueValue.text = overdueCount.toString()

        val maxCount = maxOf(completedCount, pendingCount, overdueCount, 1)

        // Post to make sure chartContainer has its height measured
        binding.chartContainer.post {
            val chartHeight = binding.chartContainer.height - binding.chartContainer.paddingTop - binding.chartContainer.paddingBottom
            
            // Adjust for text height (approx 20dp in pixels)
            val adjustedHeight = chartHeight - (20 * resources.displayMetrics.density).toInt()

            binding.barCompleted.layoutParams.height = (completedCount.toFloat() / maxCount * adjustedHeight).toInt()
            binding.barPending.layoutParams.height = (pendingCount.toFloat() / maxCount * adjustedHeight).toInt()
            binding.barOverdue.layoutParams.height = (overdueCount.toFloat() / maxCount * adjustedHeight).toInt()

            binding.barCompleted.requestLayout()
            binding.barPending.requestLayout()
            binding.barOverdue.requestLayout()
        }

        if ((pendingCount == 0) and (overdueCount == 0)){
            "No hay ninguno trabajo pendiente. Buen trabajo".also { binding.DashboardMessage.text = it }
        }
        else{
            if (pendingCount < completedCount){
                "Tienes $pendingCount tareas pendientes. Vas en buen camino".also { binding.DashboardMessage.text = it }

            } else{
                if (overdueCount < 0){
                    "Tienes $pendingCount tareas pendientes. No olvides completar tus pendientes".also { binding.DashboardMessage.text = it }
                }
                else{
                    "Tienes $pendingCount tareas pendientes y $overdueCount tareas atrasadas. ¡Debes esforzarte más!".also { binding.DashboardMessage.text = it }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        private fun newInstance() = DashboardFragment()
    }
}
