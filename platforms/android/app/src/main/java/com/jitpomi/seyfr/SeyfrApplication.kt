package com.jitpomi.seyfr

import android.app.Application

class SeyfrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ndk-context before any Rust code that needs it
        // (e.g. hickory-resolver DNS via iroh::Endpoint::bind)
        // Putting this in Application ensures it runs exactly once per process,
        // avoiding JNI panics on Activity restarts.
        JffiAndroidInit.initNdkContext(applicationContext)
    }
}
