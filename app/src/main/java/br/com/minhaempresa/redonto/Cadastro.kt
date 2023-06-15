package br.com.minhaempresa.redonto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.io.ByteArrayOutputStream
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class Cadastro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var imageView: ImageView
    private lateinit var selectedImage: Bitmap
    private lateinit var fcmToken: String

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val GALLERY_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        auth = Firebase.auth

        imageView = findViewById(R.id.ivCad)

        val botaoCadastro: Button = findViewById(R.id.btnCriarCad)
        botaoCadastro.setOnClickListener {
            fazerCadastro()
        }

        val botaoCamera: Button = findViewById(R.id.btnCamera)
        botaoCamera.setOnClickListener {
            if (checkCameraPermission()) {
                abrirCamera()
            } else {
                requestCameraPermission()
            }
        }

        val botaoGaleria: Button = findViewById(R.id.btnGallery)
        botaoGaleria.setOnClickListener {
            abrirGaleria()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fcmToken = task.result // Atribui o token FCM à propriedade fcmToken
            } else {
                Log.e("FCM", "Falha ao obter o token do FCM: ${task.exception}")
            }
        }
    }


    private fun checkCameraPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        val result = ContextCompat.checkSelfPermission(this, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        val permission = Manifest.permission.CAMERA
        ActivityCompat.requestPermissions(this, arrayOf(permission), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val image = data?.extras?.get("data") as Bitmap
                    exibirImagemSelecionada(image)
                }
                GALLERY_REQUEST_CODE -> {
                    val uri = data?.data
                    val image = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    exibirImagemSelecionada(image)
                }
            }
        }
    }

    private fun exibirImagemSelecionada(image: Bitmap) {
        selectedImage = image
        imageView.setImageBitmap(image)
    }

    private fun fazerCadastro() {
        val nome = findViewById<EditText>(R.id.etNomeCad)
        val cpf = findViewById<EditText>(R.id.etCpfCad)
        val telefone = findViewById<EditText>(R.id.etTelefoneCad)
        val email = findViewById<EditText>(R.id.etEmailCad)
        val senha = findViewById<EditText>(R.id.etSenhaCad)
        //end1
        val cep1 = findViewById<EditText>(R.id.etCep1Cad)
        val rua1 = findViewById<EditText>(R.id.etRua1Cad)
        val bairro1 = findViewById<EditText>(R.id.etBairro1Cad)
        val numero1 = findViewById<EditText>(R.id.etNumero1Cad)
        val complemento1 = findViewById<EditText>(R.id.etComplemento1Cad)
        //end2
        val cep2 = findViewById<EditText>(R.id.etCep2Cad)
        val rua2 = findViewById<EditText>(R.id.etRua2Cad)
        val bairro2 = findViewById<EditText>(R.id.etBairro2Cad)
        val numero2 = findViewById<EditText>(R.id.etNumero2Cad)
        val complemento2 = findViewById<EditText>(R.id.etComplemento2Cad)
        //end3
        val cep3 = findViewById<EditText>(R.id.etCep3Cad)
        val rua3 = findViewById<EditText>(R.id.etRua3Cad)
        val bairro3 = findViewById<EditText>(R.id.etBairro3Cad)
        val numero3 = findViewById<EditText>(R.id.etNumero3Cad)
        val complemento3 = findViewById<EditText>(R.id.etComplemento3Cad)
        val curriculo = findViewById<EditText>(R.id.etCurriculoCad)
        val status = "offline"
        val avaliacoes = ""
        val emergencias = ""
        val paciente = ""
        val senhaFim = findViewById<EditText>(R.id.etSenhaFimCad)

        if (nome.text.isEmpty() || cpf.text.isEmpty() || telefone.text.isEmpty() ||
            email.text.isEmpty() || senha.text.isEmpty() || rua1.text.isEmpty() || bairro1.text.isEmpty() || numero1.text.isEmpty() ||
            curriculo.text.isEmpty()
        ) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val inputNome = nome.text.toString()
        val inputCpf = cpf.text.toString()
        val inputTelefone = telefone.text.toString()
        //end1
        val inputCep1 = cep1.text.toString()
        val inputRua1 = rua1.text.toString()
        val inputBairro1 = bairro1.text.toString()
        val inputNumero1 = numero1.text.toString()
        val inputComplemento1 = complemento1.text.toString()
        //end2
        val inputCep2 = cep2.text.toString()
        val inputRua2 = rua2.text.toString()
        val inputBairro2 = bairro2.text.toString()
        val inputNumero2 = numero2.text.toString()
        val inputComplemento2 = complemento2.text.toString()
        //end3
        val inputCep3 = cep3.text.toString()
        val inputRua3 = rua3.text.toString()
        val inputBairro3 = bairro3.text.toString()
        val inputNumero3 = numero3.text.toString()
        val inputComplemento3 = complemento3.text.toString()
        val inputCurriculo = curriculo.text.toString()
        val inputEmail = email.text.toString()
        val inputSenha = senha.text.toString()
        val inputSenhaFim = senhaFim.text.toString()

        auth.createUserWithEmailAndPassword(inputEmail, inputSenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val imageRef = storageRef.child("images/$userId/${UUID.randomUUID()}.jpg")

                    // Upload image to Firebase Storage
                    val uploadTask = imageRef.putBytes(convertBitmapToByteArray(selectedImage))
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        imageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result

                            val usuarioMap = hashMapOf(
                                "senha" to inputSenha,
                                "nome" to inputNome,
                                "cpf" to inputCpf,
                                "email" to inputEmail,
                                "telefone" to inputTelefone,
                                "cep1" to inputCep1,
                                "rua1" to inputRua1,
                                "bairro1" to inputBairro1,
                                "numero1" to inputNumero1,
                                "complemento1" to inputComplemento1,
                                "cep2" to inputCep2,
                                "rua2" to inputRua2,
                                "bairro2" to inputBairro2,
                                "numero2" to inputNumero2,
                                "complemento2" to inputComplemento2,
                                "cep3" to inputCep3,
                                "rua3" to inputRua3,
                                "bairro3" to inputBairro3,
                                "numero3" to inputNumero3,
                                "complemento3" to inputComplemento3,
                                "curriculo" to inputCurriculo,
                                "status" to status,
                                "avaliacoes" to avaliacoes,
                                "emergencias" to emergencias,
                                "paciente" to paciente,
                                "fotoPerfil" to downloadUri.toString(),
                                "fcmToken" to fcmToken,
                                "senhaFim" to inputSenhaFim
                            )

                            db.collection("Usuarios").document(userId!!)
                                .set(usuarioMap)
                                .addOnCompleteListener {
                                    Log.d("db", "Sucesso ao salvar o usuário!")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("db", "Erro ao salvar o usuário", e)
                                }

                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)

                            Toast.makeText(baseContext, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show()
                        } else {
                            // Handle image upload failure
                            Log.d("db", "Failed to upload image: ${task.exception}")
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Falha no cadastro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Ocorreu um erro ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}
