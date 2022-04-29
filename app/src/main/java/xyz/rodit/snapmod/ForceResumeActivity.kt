package xyz.rodit.snapmod

import android.app.Activity
import android.os.Bundle

class ForceResumeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}