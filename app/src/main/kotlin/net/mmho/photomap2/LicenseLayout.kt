package net.mmho.photomap2

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_license.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class LicenseLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    fun setData(oss: String) {
        (findViewById(R.id.title) as TextView).text = oss
        try {
            val id = resources.getIdentifier("license_" + oss.toLowerCase(Locale.US), "raw", context.packageName)
            license.text = getStringResource(id)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun getStringResource(id: Int): String {
        val inputStream = resources.openRawResource(id)
        val os = ByteArrayOutputStream()
        val buffer = ByteArray(1024 * 4) // 4kB

        var i: Int
        try {
            do {
                i = inputStream.read(buffer, 0, buffer.size)
                if (i > 0) os.write(buffer, 0, i)
            } while (i != -1)

        } finally {
            inputStream.close()
        }
        return os.toString()
    }

}
