package com.udacity.project4.ui.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var bind: ActivityAuthenticationBinding
    private val loginVM: LoginVM by lazy { ViewModelProvider(this).get(LoginVM::class.java) }

    companion object {
        const val SIGN_IN_RESULT_CODE = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        bind.lifecycleOwner = this
        bind.viewModel = loginVM
        bind.executePendingBindings()

        loginVM.status.observe(this) {
            Timber.d("Status = $it")
            when (it) {
                Status.NONE -> {
                }
                Status.ATTEMPT_LOGIN -> {
                    login()
                    loginVM.resetStatus()
                }
                Status.AUTHENTICATED -> {
                    val newAct = Intent(this, RemindersActivity::class.java)
                    startActivity(newAct)
                    finish()
                }
                Status.UNAUTHENTICATED -> {

                }
                else -> {
                }
            }
        }
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout


    }

    private fun login() {
        Timber.d("login()")
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("Login success user=${FirebaseAuth.getInstance().currentUser?.displayName}")
            } else {
                Timber.d("Login failed errorCode=${response?.error?.errorCode}")
            }
        }
    }
}

