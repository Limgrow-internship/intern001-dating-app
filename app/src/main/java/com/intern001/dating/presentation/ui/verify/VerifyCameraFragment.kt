package com.intern001.dating.presentation.ui.verify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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

        verifyViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            if (result?.isVerified == true) {
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
            val availableCameraInfos = cameraProvider.availableCameraInfos
            availableCameraInfos.forEachIndexed { idx, info ->
                Log.d("CameraDebug", "Camera $idx: $info")
            }
            val selectors = listOf(
                CameraSelector.DEFAULT_FRONT_CAMERA,
                CameraSelector.DEFAULT_BACK_CAMERA,
            )
            var bound = false
            for (selector in selectors) {
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, selector, preview, imageCapture)
                    Log.d("CameraDebug", "Bound with selector $selector")
                    bound = true
                    break
                } catch (e: Exception) {
                    Log.e("CameraDebug", "Could not bind to camera: ${e.message}")
                }
            }
            if (!bound) {
                Toast.makeText(requireContext(), "No camera available!", Toast.LENGTH_LONG).show()
            }
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
                    val file = File(requireContext().cacheDir, "selfie.jpg")
                    val byteArray = outputFile.readBytes()

                    android.util.Log.d("VerifyCamera", "Selfie bytes size: ${byteArray.size}")

                    android.util.Log.d("VerifyCamera", "Selfie path: ${file.absolutePath}")
                    verifyViewModel.uploadSelfie(byteArray)
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
