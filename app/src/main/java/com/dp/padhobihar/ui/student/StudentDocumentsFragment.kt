package com.dp.padhobihar.ui.student

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.FragmentStudentDocumentsBinding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentDocumentsFragment : Fragment() {

    private var _binding: FragmentStudentDocumentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()
    private var currentDocType = ""

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadDocument(currentDocType, it) }
    }

    // Document types with labels and icons
    private data class DocItem(val type: String, val label: String, val iconRes: Int)

    private val documents = listOf(
        DocItem("10th_marksheet", "10th Marksheet", R.drawable.ic_doc_marksheet),
        DocItem("12th_marksheet", "12th Marksheet", R.drawable.ic_doc_marksheet),
        DocItem("aadhaar", "Aadhaar Card", R.drawable.ic_doc_aadhaar),
        DocItem("income_certificate", "Income Certificate", R.drawable.ic_doc_certificate),
        DocItem("caste_certificate", "Caste Certificate (if applicable)", R.drawable.ic_doc_certificate),
        DocItem("domicile", "Domicile / Residential Certificate", R.drawable.ic_doc_certificate),
        DocItem("photo", "Passport Size Photo", R.drawable.ic_doc_photo),
        DocItem("bank_passbook", "Bank Passbook (first page)", R.drawable.ic_doc_bank)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val docViews = listOf(
            binding.doc10thMarksheet.root,
            binding.doc12thMarksheet.root,
            binding.docAadhaar.root,
            binding.docIncome.root,
            binding.docCaste.root,
            binding.docDomicile.root,
            binding.docPhoto.root,
            binding.docBankPassbook.root
        )

        docViews.forEachIndexed { index, docView ->
            val doc = documents[index]
            docView.findViewById<ImageView>(R.id.ivDocIcon).setImageResource(doc.iconRes)
            docView.findViewById<TextView>(R.id.tvDocTitle).text = doc.label
            docView.findViewById<MaterialButton>(R.id.btnUpload).setOnClickListener {
                currentDocType = doc.type
                if (doc.type == "photo") pickFile.launch("image/*") else pickFile.launch("*/*")
            }
        }

        viewModel.documents.observe(viewLifecycleOwner) { uploadedDocs ->
            docViews.forEachIndexed { index, docView ->
                val doc = documents[index]
                val status = docView.findViewById<TextView>(R.id.tvDocStatus)
                if (uploadedDocs.contains(doc.type)) {
                    status.text = "✅ Uploaded"
                    status.setTextColor(resources.getColor(R.color.status_admitted, null))
                } else {
                    status.text = "Not uploaded"
                    status.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loadDocuments()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
