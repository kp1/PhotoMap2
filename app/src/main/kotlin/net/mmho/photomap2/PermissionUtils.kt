package net.mmho.photomap2

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView

internal object PermissionUtils {
    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermission(rootView: View?, c: Context) {
        if (rootView != null) {
            val snackBar = Snackbar.make(rootView, R.string.request_permission, Snackbar.LENGTH_LONG)
            val text = snackBar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView

            snackBar.setActionTextColor(c.resources.getColor(R.color.primary,null))
            text.setTextColor(c.resources.getColor(R.color.textPrimary, null))
            snackBar.setAction(R.string.setting) {
                val uri = Uri.parse("package:" + c.applicationContext.packageName)
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                c.startActivity(i)
            }
            snackBar.show()
        }

    }
}
