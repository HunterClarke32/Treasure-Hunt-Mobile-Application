/*
* Hunter Clarke
* OSU CS 492
* */

package com.example.treasure

import androidx.compose.runtime.mutableLongStateOf

class TimerState {
    var startTime = 0L
    var isRunning = false

    val elapsedTime = mutableLongStateOf(0L)

    fun startTimer() {
        startTime = System.currentTimeMillis()
        isRunning = true
    }

    fun pauseTimer() {
        isRunning = false
    }

    fun resumeTimer() {
        startTime = System.currentTimeMillis() - elapsedTime.value
        isRunning = true
    }

    fun stopTimer() {
        isRunning = false
    }

    fun getElapsedTime(): Long {
        return elapsedTime.value
    }
}