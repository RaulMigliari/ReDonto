package br.com.minhaempresa.redonto

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Detalhes : Fragment() {

    private var chamado: ListChamados? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var userRef: DocumentReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val ARG_CHAMADO = "chamado"

        fun newInstance(chamado: ListChamados): Detalhes {
            val fragment = Detalhes()
            val args = Bundle()
            args.putParcelable(ARG_CHAMADO, chamado)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chamado = it.getParcelable(ARG_CHAMADO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalhes, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser!!
        userRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        val nomeTextView = view.findViewById<TextView>(R.id.tvNomeDetalhes)
        val ivImagemDetalhe1 = view.findViewById<ImageView>(R.id.ivImagemDetalhe1)
        val ivImagemDetalhe2 = view.findViewById<ImageView>(R.id.ivImagemDetalhe2)
        val ivImagemDetalhe3 = view.findViewById<ImageView>(R.id.ivImagemDetalhe3)

        val btnAceitar = view.findViewById<Button>(R.id.btnAceitar)
        val btnRecusar = view.findViewById<Button>(R.id.btnRecusar)

        chamado?.let { chamado ->
            nomeTextView.text = chamado.nome

            val imagens = chamado.imagens
            if (imagens.isNotEmpty()) {
                if (imagens.size >= 1) {
                    loadImage(imagens[0], ivImagemDetalhe1)
                }
                if (imagens.size >= 2) {
                    loadImage(imagens[1], ivImagemDetalhe2)
                }
                if (imagens.size >= 3) {
                    loadImage(imagens[2], ivImagemDetalhe3)
                }
            }
        }

        btnAceitar.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val chamadoId = chamado?.id
                if (chamadoId != null) {
                    val chamadoRef = db.collection("Chamados").document(chamadoId)
                    val userRef = db.collection("Usuarios").document(userId)

                    chamadoRef.update("interesse", FieldValue.arrayUnion(userRef))
                        .addOnSuccessListener {
                            // Atualização bem-sucedida do campo 'interesse'
                            Log.d(TAG, "Campo 'interesse' atualizado")

                            userRef.update("emergencias", FieldValue.arrayUnion(chamadoId))
                                .addOnSuccessListener {
                                    // Atualização bem-sucedida do campo 'emergencias'
                                    Log.d(TAG, "Campo 'emergencias' atualizado")

                                    val chamadosEsperaFragment = ChamadosEspera()
                                    val transaction = requireFragmentManager().beginTransaction()
                                    transaction.replace(R.id.frame_layout, chamadosEsperaFragment)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }
                                .addOnFailureListener { exception ->
                                    // Lidar com a falha ao atualizar o campo 'emergencias'
                                    Log.d(TAG, "Falha ao atualizar o campo 'emergencias'")
                                }
                        }
                        .addOnFailureListener { exception ->
                            // Lidar com a falha ao atualizar o campo 'interesse'
                            Log.d(TAG, "Falha ao atualizar o campo 'interesse'")
                        }
                }
            }
        }


        btnRecusar.setOnClickListener {
            val userId = currentUser.uid

            chamado?.let { chamado ->
                val chamadoId = chamado.id
                Log.d(TAG, "Chamado ID: $chamadoId")

                // Obter uma referência ao documento do chamado
                val chamadoRef = db.collection("Chamados").document(chamadoId)

                // Obter uma referência ao documento do usuário
                val userRef = db.collection("Usuarios").document(userId)

                // Adicionar a referência do usuário ao campo 'naoInteresse' do documento do chamado
                chamadoRef.update("naoInteresse", FieldValue.arrayUnion(userRef))
                    .addOnSuccessListener {
                        // Referência do usuário adicionada com sucesso ao campo 'naoInteresse'
                        Log.d(TAG, "Referência do usuário adicionada ao campo 'naoInteresse'")

                        // Substituir o fragmento e navegar para a tela 'Home'
                        val homeFragment = Home() // Substitua pela classe real do fragmento 'Home'
                        val transaction = requireFragmentManager().beginTransaction()
                        transaction.replace(R.id.frame_layout, homeFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    .addOnFailureListener { exception ->
                        // Lidar com a falha ao adicionar a referência do usuário ao campo 'naoInteresse'
                        Log.d(TAG, "Falha ao adicionar referência do usuário ao campo 'naoInteresse': $exception")
                    }
            }
        }

        return view
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(requireContext())
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}
