package net.mmho.photomap2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)
        showAbout()
        list.adapter = LicenseAdapter(this, R.layout.layout_license,
            resources.getStringArray(R.array.oss))
    }

    private fun showAbout() {
        about.text = buildString{
            append(getString(R.string.about_software,getString(R.string.app_name)))
            append("\n\n")
            for(oss in resources.getStringArray(R.array.oss)){
                append("\t・$oss\n")
            }
        }
    }
}
