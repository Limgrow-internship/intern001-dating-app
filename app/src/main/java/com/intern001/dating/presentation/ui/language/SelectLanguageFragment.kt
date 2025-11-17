package com.intern001.dating.presentation.ui.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.local.AppPreferencesManager
import com.intern001.dating.databinding.FragmentSelectLanguageBinding
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectLanguageFragment : BaseFragment() {
    private var _binding: FragmentSelectLanguageBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SelectLanguageViewModel>()
    private val adapter = LanguageAdapter { lang -> viewModel.selectLanguage(lang) }

    @Inject
    lateinit var appPreferencesManager: AppPreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSelectLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(requireContext().resources.configuration)
        config.setLocale(locale)

        requireContext().createConfigurationContext(config)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter

        // Only show ads if user hasn't purchased "no ads"
        if (!viewModel.hasActiveSubscription()) {
            NativeAdHelper.bindNativeAdSmall(
                requireContext(),
                binding.adContainer,
                AdManager.nativeAdSmall,
            )
        } else {
            binding.adContainer.visibility = View.GONE
        }

        viewModel.languages.observe(viewLifecycleOwner) { languages ->
            adapter.submitList(languages)
            if (viewModel.selectedLanguage.value == null && languages.isNotEmpty()) {
                viewModel.selectLanguage(languages[0])
            }
        }
        viewModel.selectedLanguage.observe(viewLifecycleOwner) { lang ->
            adapter.setSelected(lang)
        }

        binding.btnContinue.setOnClickListener {
            val lang = viewModel.selectedLanguage.value
            if (lang != null) {
                val langCode = lang.code.take(2)

                requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    .edit { putString("language", langCode) }

                viewLifecycleOwner.lifecycleScope.launch {
                    appPreferencesManager.setOnboardingCompleted(true)
                    appPreferencesManager.setLanguageSelected(true)

                    setAppLocale(langCode)

                    findNavController().navigate(R.id.action_selectLanguageFragment_to_login)
                }
            } else {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please select a language",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            }
        }

        viewModel.fetchLanguages()
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        _binding = null
        super.onDestroyView()
    }
}
