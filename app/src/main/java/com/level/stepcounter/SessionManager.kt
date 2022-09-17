package com.level.stepcounter

import android.content.Context
import android.content.SharedPreferences


class SessionManager(context: Context?) {

    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var _context: Context

    private val PRIVATE_MODE = Context.MODE_PRIVATE
    private val PREF_NAME = "Target_Steps"
    private val KEY_STEPS = "total_steps"


    init {
        _context = context!!
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun updateTarget(totalSteps: Int)
    {
        editor.putInt(KEY_STEPS,totalSteps).apply()
    }

    fun getSteps(): Int {
        return pref.getInt(KEY_STEPS,10000)
    }
}