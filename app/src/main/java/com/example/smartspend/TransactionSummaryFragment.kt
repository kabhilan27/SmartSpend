package com.example.smartspend

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.smartspend.databinding.FragmentTransactionSummaryBinding
import java.text.DecimalFormat

class TransactionSummaryFragment : Fragment() {

    private var _binding: FragmentTransactionSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val totalExpenses = sharedPrefs.getFloat("totalExpenses", 0f).toDouble()
        val totalIncome = sharedPrefs.getFloat("totalIncome", 0f).toDouble()
        val budgetLimit = sharedPrefs.getFloat("budgetLimit", 1000f).toDouble()

        val df = DecimalFormat("#,##0.00")
        binding.totalIncome.text = "$${df.format(totalIncome)}"
        binding.totalExpenses.text = "$${df.format(totalExpenses)}"
        binding.netBalance.text = "$${df.format(totalIncome - totalExpenses)}"
        binding.budgetLimit.text = "$${df.format(budgetLimit)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TransactionSummaryFragment()
    }
}