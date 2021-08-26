package com.udacity.project4.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

enum class Status { NONE, ATTEMPT_LOGIN, AUTHENTICATED, UNAUTHENTICATED }
class LoginVM : ViewModel() {
    private val _status = MediatorLiveData<Status>().apply {
        addSource(FirebaseUserLiveData().map { user ->
            if (user != null) Status.AUTHENTICATED else Status.UNAUTHENTICATED
        }) {
            this.value = it
        }
    }
    val status: LiveData<Status>
        get() = _status

    fun login() {
        this._status.value = Status.ATTEMPT_LOGIN
    }

    fun resetStatus() {
        this._status.value = Status.NONE
    }
}
