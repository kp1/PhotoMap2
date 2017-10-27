package net.mmho.photomap2

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils {
    companion object {
        fun networkCheck(context: Context):Boolean{
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo?.isConnected ?: false
        }
    }
}