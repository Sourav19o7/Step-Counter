@file:Suppress("DEPRECATION")

package com.level.stepcounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.schedule

class ArchiveActivity : AppCompatActivity() {

    lateinit var shimmerLayout : ShimmerFrameLayout
    lateinit var recyclerView: RecyclerView
    val DELAY_TIME = 3 * 1000;

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        recyclerView = findViewById(R.id.recycclerView)
        shimmerLayout = findViewById(R.id.shimmer_layout)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CommonAdapter.mAdapter
        shimmerLayout.startShimmer()
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }, 3000)

//        Timer().schedule(2000) {
//        }
    }
//    private fun fetchData() {
//        val list = ArrayList<String>()
//        list.add("Date : Steps")
//
//    }
}