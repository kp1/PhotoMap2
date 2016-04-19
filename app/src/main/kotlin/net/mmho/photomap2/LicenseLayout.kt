package net.mmho.photomap2

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_license.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class LicenseLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    fun setData(oss: String) {
        title.text = oss
        try {
            val id = resources.getIdentifier("license_${oss.toLowerCase(Locale.US)}", "raw", context.packageName)
            license.text = getStringResource(id)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getStringResource(id: Int): String {
        val inputStream = resources.openRawResource(id)
        val os = ByteArrayOutputStream()
        inputStream.use{ it.copyTo(os) }
        return os.toString()
    }

}
