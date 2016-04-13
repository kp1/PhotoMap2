package net.mmho.photomap2

internal interface ProgressChangeListener {
    fun showProgress(progress: Int)
    fun endProgress()
}
