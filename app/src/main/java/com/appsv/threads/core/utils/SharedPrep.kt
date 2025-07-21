package com.appsv.threads.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object SharedPrep {

    fun storeData(
         username: String ,
         email: String ,
         password: String ,
         profileImageUrl: String,
         context: Context
    ){
        val sharedPreferences=context.getSharedPreferences("users",MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putString("username",username)
        editor.putString("email",email)
        editor.putString("password",password)
        editor.putString("profileImageUrl",profileImageUrl)
        editor.apply()
    }

    fun getUserName(context: Context):String?{
        val sharedPreferences=context.getSharedPreferences("users", MODE_PRIVATE)
        return sharedPreferences.getString("username","")!!

    }
    fun getEmail(context: Context):String? {
        val sharedPreferences = context.getSharedPreferences("users", MODE_PRIVATE)
        return sharedPreferences.getString("email", "")!!
    }
    fun getPassword(context: Context):String? {
        val sharedPreferences = context.getSharedPreferences("users", MODE_PRIVATE)
        return sharedPreferences.getString("password", "")!!
    }
    fun getProfileImageUrl(context: Context):String? {
        val sharedPreferences = context.getSharedPreferences("users", MODE_PRIVATE)
        return sharedPreferences.getString("profileImageUrl", "")!!
    }
}