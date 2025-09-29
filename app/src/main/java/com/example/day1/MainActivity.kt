package com.example.day1

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.day1.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ini yang benar untuk view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager = CredentialManager.create(this)
        auth = Firebase.auth

        registerEvents()
    }

    private fun registerEvents() {
        binding.btnLogin.setOnClickListener {
            Log.d("APP_DAY1", "button di click")
            lifecycleScope.launch {
                val request = prepareRequest()
                loginByGoogle(request)
            }
        }
    }

    private fun prepareRequest(): GetCredentialRequest {
        val serverClientId = "56018237941-vn5hkqoa7ptn4v9qsqhnhbu6fjckd74v.apps.googleusercontent.com"
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun loginByGoogle(request: GetCredentialRequest) {
        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )

            val credential = result.credential
            val idTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = idTokenCredential.idToken

            firebaseLoginCallback(idToken)
        } catch (exc: NoCredentialException) {
            Toast.makeText(this, "Login gagal: ${exc.message}", Toast.LENGTH_LONG).show()
        } catch (exc: Exception) {
            Toast.makeText(this, "Login gagal: ${exc.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseLoginCallback(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Login Gagal", Toast.LENGTH_LONG).show()
                }
            }
    }
}
