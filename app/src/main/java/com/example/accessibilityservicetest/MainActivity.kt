package com.example.accessibilityservicetest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.accessibilityservicetest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    companion object{
        const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }

    private lateinit var binding: ActivityMainBinding
    private val context = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickListener()
    }

    private fun clickListener() {
        binding.btnLetsGo.setOnClickListener {
            /*if (!checkAccessibilityPermission()) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            }*/

            checkAccessibilityPermission()
        }
    }

    private fun checkAccessibilityPermission() {
        // Get the AccessibilityManager instance
        val accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        // Check if the accessibility service is enabled
        if (!accessibilityManager.isEnabled) {
            // The accessibility service is not enabled
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // request permission via start activity for result
            startActivity(intent)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
            } else {
                // Permission already granted, you can create the overlay
            }
        }


    }
}