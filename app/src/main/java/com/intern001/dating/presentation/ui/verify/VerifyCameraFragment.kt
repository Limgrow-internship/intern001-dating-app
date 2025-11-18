package com.intern001.dating.presentation.ui.verify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentVerifyCameraBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.VerifyViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class VerifyCameraFragment : BaseFragment() {
    private var _binding: FragmentVerifyCameraBinding? = null
    private val binding get() = _binding!!

    private val verifyViewModel: VerifyViewModel by viewModels()
    private var imageCapture: ImageCapture? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVerifyCameraBinding.inflate(inflater, container, false)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }

        binding.progressBar.visibility = View.GONE
        binding.btnTryAgain.visibility = View.GONE
        binding.textDesc.visibility = View.GONE
        binding.btnTakePhoto.visibility = View.VISIBLE

        binding.btnTakePhoto.setOnClickListener {
            binding.btnTakePhoto.visibility = View.GONE
            binding.btnTryAgain.visibility = View.GONE
            binding.textDesc.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            takePhoto()
        }
        binding.btnTryAgain.setOnClickListener {
            binding.btnTryAgain.visibility = View.GONE
            binding.textDesc.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            takePhoto()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.verifyAccountFragment)
        }

        verifyViewModel.verificationResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            if (result != null && result.success) {
                Toast.makeText(requireContext(), "Verification Successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_verifyCameraFragment_to_verificationSuccessFragment)
            } else {
                binding.textDesc.visibility = View.VISIBLE
                binding.textDesc.text = "We cannot recognize your face."
                binding.btnTryAgain.visibility = View.VISIBLE
                binding.btnTakePhoto.visibility = View.GONE
            }
        }

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val outputFile = File(requireContext().cacheDir, "selfie.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val byteArray = outputFile.readBytes()
                    verifyViewModel.verifyFace(byteArray)
                }

                override fun onError(exc: ImageCaptureException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Photo capture failed!", Toast.LENGTH_SHORT).show()
                    binding.btnTakePhoto.visibility = View.VISIBLE
                    binding.btnTryAgain.visibility = View.GONE
                }
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
