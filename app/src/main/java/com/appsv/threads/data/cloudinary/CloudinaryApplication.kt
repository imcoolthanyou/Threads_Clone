package com.appsv.threads.data.cloudinary

import android.app.Application
import com.cloudinary.android.MediaManager



class CloudinaryApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val url="CLOUDINARY_URL=cloudinary://<your_api_key>:<your_api_secret>@dtadsslm3"

        val config = HashMap<String,String>()

        config["cloud_name"]="dtadsslm3"
        config["api_key"]="344157851432799"
        config["api_secret"]="JTUThj3_ldHsF0WeIds3_E2T3q4"

     MediaManager.init(this,config)
    }
}