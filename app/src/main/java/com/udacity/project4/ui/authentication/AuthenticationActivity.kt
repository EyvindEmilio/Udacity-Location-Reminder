package com.udacity.project4.ui.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var bind: ActivityAuthenticationBinding
    private val authVM: AuthVM by viewModel()

    companion object {
        const val SIGN_IN_RESULT_CODE = 12345
        fun launch(context: Context) {
            context.startActivity(
                Intent(context, AuthenticationActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        bind.lifecycleOwner = this
        bind.viewModel = authVM
        bind.executePendingBindings()

        authVM.status.observe(this) {
            Timber.d("Status = $it")
            when (it) {
                Status.NONE -> {
                }
                Status.ATTEMPT_LOGIN -> {
                    login()
                    authVM.resetStatus()
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

