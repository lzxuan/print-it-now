package com.triplicity.printitnow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        viewModel = ViewModelProvider(this).get(SignInViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            startMainActivity()
        } else if (!viewModel.isSigningIn) {
            createSignInIntent()
        }
    }

    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.MicrosoftBuilder().build(),
            AuthUI.IdpConfig.GitHubBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls("https://printitnow.web.app/terms",
                    "https://printitnow.web.app/privacy")
                .setLogo(R.drawable.ic_app)
                .build(),
            RC_SIGN_IN)

        viewModel.isSigningIn = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            viewModel.isSigningIn = false

            if (resultCode == Activity.RESULT_OK) {
                startMainActivity()
            } else {
                if (response == null) {
                    val string = resources.getString(R.string.sign_in_to_continue)
                    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
                    finish()
                } else if (response.error != null &&
                    response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showSignInErrorDialog(R.string.message_no_network)
                } else {
                    showSignInErrorDialog(R.string.message_unknown)
                }
            }
        }
    }

    private fun showSignInErrorDialog(@StringRes message: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_sign_in_error)
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton(R.string.option_exit) { _, _ -> signOut() }
            .setPositiveButton(R.string.option_retry) { _, _ -> retry() }
            .show()
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                finish()
            }
    }

    private fun retry() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                createSignInIntent()
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 1
    }

}