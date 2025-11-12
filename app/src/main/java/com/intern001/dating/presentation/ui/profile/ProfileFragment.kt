package com.intern001.dating.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : com.intern001.dating.presentation.common.viewmodel.BaseFragment() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val btnDeleteAccount = view.findViewById<LinearLayout>(R.id.btnDeleteAccount)
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountBottomSheet()
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                showDeleteAccountSuccessSheet()
            } else {
                Toast.makeText(context, "Delete failed!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showDeleteAccountBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.btnDelete).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteAccount()
        }
        dialog.show()
    }

    private fun showDeleteAccountSuccessSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_delete_success, null)
        dialog.setContentView(view)
        view.findViewById<TextView>(R.id.btnDone).setOnClickListener {
            dialog.dismiss()
            goToLoginScreen()
        }
        dialog.show()
    }

    private fun goToLoginScreen() {
        findNavController().navigate(R.id.action_profile_to_login)
    }
}
