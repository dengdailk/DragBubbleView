package com.study.dragbubbleview

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity()  , View.OnClickListener,
    DragBubbleView.OnBubbleStateListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reCreateBtn.setOnClickListener(this)
        dragBubbleView.setText("泡泡")
        dragBubbleView.setOnBubbleStateListener(this)
    }

    override fun onDrag() {
        Log.e("---> ", "拖拽气泡")
    }

    override fun onRestore() {
        Log.e("---> ", "气泡恢复原来位置")
    }

    override fun onDismiss() {
        Log.e("---> ", "气泡消失")
    }

    override fun onMove() {
        Log.e("---> ", "移动气泡")
    }

    override fun onClick(v: View?) {
        dragBubbleView.reCreate()
    }
}
