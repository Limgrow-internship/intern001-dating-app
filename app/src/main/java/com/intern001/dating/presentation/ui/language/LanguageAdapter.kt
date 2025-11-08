package com.intern001.dating.presentation.ui.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.databinding.ItemLanguageBinding
import com.intern001.dating.domain.model.Language

class LanguageAdapter(
    private val onSelect: (Language) -> Unit,
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback) {
    private var selectedLang: Language? = null

    fun setSelected(lang: Language?) {
        selectedLang = lang
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val lang = getItem(position)
        holder.bind(lang, lang == selectedLang, onSelect)
    }

    class LanguageViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Language, selected: Boolean, onSelect: (Language) -> Unit) {
            binding.tvLanguage.isSelected = selected
            binding.tvLanguage.text = language.name
            binding.ivCheck.visibility = if (selected) View.VISIBLE else View.INVISIBLE
            binding.root.setOnClickListener { onSelect(language) }
        }
    }

    object LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language) = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: Language, newItem: Language) = oldItem == newItem
    }
}
