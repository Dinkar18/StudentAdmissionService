package com.dp.padhobihar.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.padhobihar.databinding.ItemCollegeBinding
import com.dp.padhobihar.domain.model.College
import java.io.File
import java.io.FileOutputStream

class CollegeAdapter(
    private val onCollegeClick: ((College) -> Unit)? = null
) : ListAdapter<College, CollegeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private val binding: ItemCollegeBinding,
        private val onClick: ((College) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(college: College) {
            val ctx = binding.root.context
            binding.tvCollegeName.text = college.name
            binding.tvCollegeInitial.text = college.name.firstOrNull()?.uppercase() ?: "?"
            binding.tvAddress.text = college.district
            binding.tvType.text = college.type
            binding.tvUniversity.text = college.university
            binding.tvCourses.text = college.courses.joinToString(", ") { it.name }.ifEmpty { "Courses info available in brochure" }

            // Website button
            if (college.websiteUrl.isNotEmpty()) {
                binding.btnWebsite.visibility = View.VISIBLE
                binding.btnWebsite.setOnClickListener {
                    val url = if (college.websiteUrl.startsWith("http")) college.websiteUrl else "https://${college.websiteUrl}"
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            } else {
                binding.btnWebsite.visibility = View.GONE
            }

            // Brochure button
            if (college.brochureData.isNotEmpty()) {
                binding.btnBrochure.visibility = View.VISIBLE
                binding.btnBrochure.setOnClickListener {
                    openBrochure(ctx, college)
                }
            } else {
                binding.btnBrochure.visibility = View.GONE
            }

            // Card click for selection
            binding.root.setOnClickListener { onClick?.invoke(college) }
        }

        private fun openBrochure(ctx: Context, college: College) {
            try {
                val bytes = android.util.Base64.decode(college.brochureData, android.util.Base64.NO_WRAP)
                val file = File(ctx.cacheDir, "${college.id}_brochure.pdf")
                FileOutputStream(file).use { it.write(bytes) }

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.fileprovider", file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(ctx, "Cannot open brochure: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCollegeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onCollegeClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<College>() {
        override fun areItemsTheSame(oldItem: College, newItem: College) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: College, newItem: College) = oldItem == newItem
    }
}
