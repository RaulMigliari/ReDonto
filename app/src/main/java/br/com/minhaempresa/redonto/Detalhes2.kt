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

class Detalhes2 : Fragment() {

    private var chamado: ListChamados? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var userRef: DocumentReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val ARG_CHAMADO = "chamado"

        fun newInstance(chamado: ListChamados): Detalhes2 {
            val fragment = Detalhes2()
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
        val view = inflater.inflate(R.layout.fragment_detalhes2, container, false)
        val nomeTextView = view.findViewById<TextView>(R.id.tvNomeDetalhes2)
        val ivImagemDetalhe1 = view.findViewById<ImageView>(R.id.ivImagemDetalhe1)
        val ivImagemDetalhe2 = view.findViewById<ImageView>(R.id.ivImagemDetalhe2)
        val ivImagemDetalhe3 = view.findViewById<ImageView>(R.id.ivImagemDetalhe3)


        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser!!
        userRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)


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


        btnCancelar.setOnClickListener {
            val userId = currentUser.uid

            chamado?.let { chamado ->
                val chamadoId = chamado.id
                Log.d(TAG, "Chamado ID: $chamadoId")

                // Get a reference to the chamado document
                val chamadoRef = db.collection("Chamados").document(chamadoId)

                // Get a reference to the user document
                val userRef = db.document("Usuarios/$userId")

                // Remove the chamadoId from the 'emergencias' field of the user document
                userRef.update("emergencias", FieldValue.arrayRemove(chamadoId))
                    .addOnSuccessListener {
                        // Successfully removed chamadoId from the 'emergencias' field
                        Log.d(TAG, "ChamadoId removed from 'emergencias' field")

                        // Remove the userRef from the 'interesse' field of the chamado document
                        chamadoRef.update("interesse", FieldValue.arrayRemove(userRef))
                            .addOnSuccessListener {
                                // Successfully removed userRef from the 'interesse' field
                                Log.d(TAG, "UserRef removed from 'interesse' field")

                                // Replace the fragment and navigate to the 'ChamadosEspera' screen
                                val chamadosEsperaFragment = ChamadosEspera()
                                val transaction = requireFragmentManager().beginTransaction()
                                transaction.replace(R.id.frame_layout, chamadosEsperaFragment)
                                transaction.addToBackStack(null)
                                transaction.commit()
                            }
                            .addOnFailureListener { exception ->
                                // Handle the failure to remove userRef from the 'interesse' field
                                Log.d(TAG, "Failed to remove userRef from 'interesse' field: $exception")
                            }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the failure to remove chamadoId from the 'emergencias' field
                        Log.d(TAG, "Failed to remove chamadoId from 'emergencias' field: $exception")
                    }
            }
        }

        return view
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}
