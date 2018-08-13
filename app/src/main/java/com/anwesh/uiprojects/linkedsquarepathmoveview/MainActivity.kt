package com.anwesh.uiprojects.linkedsquarepathmoveview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squarepathmoverview.SquarePathMoverView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquarePathMoverView.create(this)
    }
}
