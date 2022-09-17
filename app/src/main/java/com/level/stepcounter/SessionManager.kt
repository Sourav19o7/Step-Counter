package com.level.stepcounter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences


class SessionManager(context: Context?) {

    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var _context: Context

    var PRIVATE_MODE = Context.MODE_PRIVATE

    private val PREF_NAME = "Target_Steps"

    val KEY_STEPS = "total_steps"
    val IS_SET = "target_set"


    init {
        _context = context!!
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun updateTarget(totalSteps: Int)
    {
        editor.putInt(KEY_STEPS,totalSteps)
        editor.putBoolean(IS_SET, true)
        editor.commit()
    }

    fun getSteps(): Int {
        return if (isSet())
            pref.getInt(KEY_STEPS, 10000)
        else
            10000
    }

    fun isSet(): Boolean {
        return pref.getBoolean(IS_SET, false)
    }
}