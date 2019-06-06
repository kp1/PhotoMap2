package net.mmho.photomap2

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.TextView

internal object PermissionUtils {
    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermission(rootView: View, c: Context) {
        val snackBar = Snackbar.make(rootView, R.string.request_permission, Snackbar.LENGTH_INDEFINITE)
        val text:TextView = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text)

        snackBar.setActionTextColor(c.resources.getColor(R.color.primary,null))
        snackBar.view.setBackgroundColor(c.resources.getColor(R.color.snackbar_background,null))
        text.setTextColor(c.resources.getColor(R.color.textPrimaryInverse, null))
        snackBar.setAction(R.string.setting) {
            val uri = Uri.parse("package:" + c.applicationContext.packageName)
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            c.startActivity(i)
        }
        snackBar.show()
    }
}

