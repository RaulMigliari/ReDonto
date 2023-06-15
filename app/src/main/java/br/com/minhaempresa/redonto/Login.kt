package br.com.minhaempresa.redonto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val fazerCadastro: TextView = findViewById(R.id.tvCad)

        fazerCadastro.setOnClickListener {
            val intent = Intent(this, Cadastro::class.java)
            startActivity(intent)
        }

        val botaoLogin: Button = findViewById(R.id.btnLogin)
        botaoLogin.setOnClickListener {
            fazerLogin()
        }
    }

    private fun fazerLogin(){
        val email: EditText = findViewById(R.id.etEmail)
        val senha: EditText = findViewById(R.id.etSenha)

        if (email.text.isEmpty() || senha.text.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()

            return
        }

        val emailInput = email.text.toString()
        val senhaInput = senha.text.toString()

        auth.signInWithEmailAndPassword(emailInput,senhaInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    Toast.makeText(baseContext, "Success",
                        Toast.LENGTH_SHORT).show()

                } else {
                    // If sign in fails, display a message to the user.

                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Ocorreu um erro ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}