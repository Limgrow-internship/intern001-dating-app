package com.example.heartondatingapp.presentation.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.heartondatingapp.R

@AndroidEntryPoint
class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        val btnSignIn = view.findViewById<Button>(R.id.btnSignIn)

        btnSignIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frg_container, VerifyFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
