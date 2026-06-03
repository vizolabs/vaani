package com.vaani.keyboard.ui

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.R
import com.vaani.keyboard.util.PermissionHelper
import com.vaani.keyboard.util.Prefs

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        prefs.markActive()
        setupStatusBar()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#16161F")
            window.navigationBarColor = Color.parseColor("#16161F")
        }
    }

    protected fun finishWithTransition() {
        finish()
        overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
        )
    }

    protected fun requestMicrophonePermission() {
        if (PermissionHelper.hasRecordAudio(this)) {
            onPermissionResult(true)
            return
        }
        if (PermissionHelper.shouldShowRationale(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.perm_mic_rationale_title)
                .setMessage(R.string.perm_mic_rationale_message)
                .setPositiveButton(R.string.perm_dialog_allow) { _, _ ->
                    PermissionHelper.requestRecordAudio(this)
                }
                .setNegativeButton(R.string.perm_dialog_not_now, null)
                .show()
        } else {
            PermissionHelper.requestRecordAudio(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionHelper.isRecordAudioRequest(requestCode)) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            onPermissionResult(granted)
        }
    }

    protected open fun onPermissionResult(granted: Boolean) {
    }
}
