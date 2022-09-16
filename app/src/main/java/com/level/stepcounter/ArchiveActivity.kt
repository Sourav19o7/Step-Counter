package com.level.stepcounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArchiveActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        //fetchData()

        val recyclerView = findViewById<RecyclerView>(R.id.recycclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CommonAdapter.mAdapter
    }
//    private fun fetchData() {
//        val list = ArrayList<String>()
//        list.add("Date : Steps")
//
//    }
}