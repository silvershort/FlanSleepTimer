package com.example.sleeptimer.viewmodel

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.sleeptimer.R
import com.example.sleeptimer.util.Flan

object BindingUtil {
    @JvmStatic
    @BindingAdapter("flanImage")
    fun flanImage(view: ImageView, state: Int) {
        when(state) {
            Flan.AWAKE.state -> view.setImageResource(R.drawable.image1)
            Flan.SLEEPY.state -> view.setImageResource(R.drawable.image2)
            Flan.SLEEP.state -> view.setImageResource(R.drawable.image3)
        }
    }

    @JvmStatic
    @BindingAdapter("timerString")
    fun timerString(view: TextView, run: Boolean) {
        when (run) {
            true -> view.text = view.context.getString(R.string.btn_stop)
            false -> view.text = view.context.getString(R.string.btn_start)
        }
    }
}