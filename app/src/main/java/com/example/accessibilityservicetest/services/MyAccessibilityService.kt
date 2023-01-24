package com.example.accessibilityservicetest.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import com.example.accessibilityservicetest.R


class MyAccessibilityService : AccessibilityService() {

    private lateinit var view: View
    private var windowManager: WindowManager? = null

    companion object {
        private const val TAG = "MyAccessibilityService"
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

//        performGlobalAction()

        /*event?.packageName ?: return
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED || event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) return
        val packageName = event.packageName.toString()
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appLabel = packageManager.getApplicationLabel(applicationInfo)
        Log.d(TAG, "onAccessibilityEvent: ${appLabel.toString().uppercase()}")*/

        val packageName = event?.packageName.toString()
        val packageManager = this.packageManager

        try {
            val appInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val label = packageManager.getApplicationLabel(appInfo)
            Log.d(TAG, "App name is : ${label.toString().uppercase()}")

            Thread.sleep(500)
            // Todo add here blocked app list and check
            if (label.toString().uppercase() == "CONTACTS") {
//                val result = performGlobalAction(GLOBAL_ACTION_BACK)
//                Log.d(TAG, "make back action result: $result")

                displayOverlay()
            } else {
                if (label.toString()
                        .uppercase() != applicationContext.resources.getString(R.string.app_name)
                        .uppercase()
                ) {
                    removeOverlay()
                }
            }

            /*if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                if ((event.contentDescription == "back"
                            || event.contentDescription == "navigate up")
                ) {
                    // Handle back press event
//                    windowManager?.let {
                        removeOverlay()
                        val result = performGlobalAction(GLOBAL_ACTION_BACK)
                        Log.d(TAG, "make back action result: $result")
//                    }

                }
            }*/


        } catch (e: Exception) {
            e.printStackTrace()
        }

//        val apps = getLaunchableInstalledApps(this)
//        Log.d(TAG, "onAccessibilityEvent: $apps")
    }

    private fun displayOverlay() {

        val resources = applicationContext.resources
        val metrics = resources.displayMetrics
        val navigationBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier(
                "navigation_bar_height",
                "dimen",
                "android"
            )
        )
        val navigationBarWidth = metrics.widthPixels - resources.displayMetrics.widthPixels
        Log.d(TAG, "displayOverlay: Height: $navigationBarHeight and Width: $navigationBarWidth")

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            WindowManager.LayoutParams.TYPE_PHONE;
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        view = inflater.inflate(R.layout.floating_view, null)
        view.findViewById<TextView>(R.id.textView).setOnClickListener {
            val result = performGlobalAction(GLOBAL_ACTION_BACK)
            Log.d(TAG, "make back action result: $result")
        }
        windowManager?.addView(view, params)

    }

    private fun removeOverlay() {
        windowManager?.removeView(view)
        windowManager = null

        try {
            // remove the view from the window
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view)
            // invalidate the view
            view.invalidate()
            // remove all views
            (view.parent as ViewGroup).removeAllViews()

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (e: java.lang.Exception) {
            Log.d("Error2", e.toString())
        }
    }


    private fun getLaunchableInstalledApps(context: Context): List<String> {
        val pm: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list: ArrayList<ApplicationInfo> = ArrayList()
        for (resolveInfo in pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)) {
            try {
                val app: ApplicationInfo = context.packageManager
                    .getApplicationInfo(resolveInfo.activityInfo.packageName, 0)
                list.add(app)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        return list.filter {
            it.packageName.toString().uppercase() != applicationContext.resources.getString(
                R.string.app_name
            ).uppercase()
        }.map { it.packageName.toString() }.minus(
            applicationContext.resources.getString(
                R.string.app_name
            )
        )
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt: Something went wrong!")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo()
        info.apply {
            // Set the type of events that this service wants to listen to. Others
            // won't be passed to this service.
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED or AccessibilityEvent.TYPE_VIEW_FOCUSED

            // If you only want this service to work with specific applications, set their
            // package names here. Otherwise, when the service is activated, it will listen
            // to events from all applications.
//            packageNames = arrayOf("com.example.android.myFirstApp", "com.example.android.mySecondApp")
            packageNames = getLaunchableInstalledApps(applicationContext).toTypedArray()

            // Set the type of feedback your service will provide.
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN

            // Default services are invoked only if no package-specific ones are present
            // for the type of AccessibilityEvent generated. This service *is*
            // application-specific, so the flag isn't necessary. If this was a
            // general-purpose service, it would be worth considering setting the
            // DEFAULT flag.

            flags = AccessibilityServiceInfo.DEFAULT

            notificationTimeout = 100
        }

        this.serviceInfo = info
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()

        removeOverlay()
    }
}