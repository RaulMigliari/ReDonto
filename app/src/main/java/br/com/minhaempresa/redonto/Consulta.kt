package br.com.minhaempresa.redonto

import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

class Consulta : Fragment() {

    private var chamado: ListChamados? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var userRef: DocumentReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var paciente: String = ""
    private lateinit var tvNomeConsulta: TextView
    private lateinit var tvNomeConsul: TextView
    private lateinit var tvTelefoneConsulta: TextView
    private lateinit var tvTelefoneConsul: TextView
    private lateinit var tvEntreContat: TextView
    private lateinit var ivImagemConsulta1: ImageView
    private lateinit var ivImagemConsulta2: ImageView
    private lateinit var ivImagemConsulta3: ImageView
    private lateinit var tvSemPaciente: TextView
    private lateinit var btnReturnHome: Button
    private var listenerRegistration: ListenerRegistration? = null

    companion object {
        private const val ARG_CHAMADO = "chamado"

        fun newInstance(chamado: ListChamados): Consulta {
            val fragment = Consulta()
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
        val view = inflater.inflate(R.layout.fragment_consulta, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser!!
        userRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())
        userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Ocorreu um erro ao obter o snapshot do documento
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val paciente = snapshot.getString("paciente")
                updateView(paciente)
            } else {
                // O documento do usuário logado não existe
                updateView(null)
            }
        }

        // Encontra os elementos de layout
        tvNomeConsulta = view.findViewById(R.id.tvNomeConsulta)
        tvNomeConsul = view.findViewById(R.id.tvNomeConsul)
        tvTelefoneConsulta = view.findViewById(R.id.tvTelefoneConsulta)
        tvTelefoneConsul = view.findViewById(R.id.tvTelefoneConsul)
        tvEntreContat = view.findViewById(R.id.tvContatoConsul)
        ivImagemConsulta1 = view.findViewById(R.id.ivImagemConsulta1)
        ivImagemConsulta2 = view.findViewById(R.id.ivImagemConsulta2)
        ivImagemConsulta3 = view.findViewById(R.id.ivImagemConsulta3)
        tvSemPaciente = view.findViewById(R.id.tvSemPaciente)
        btnReturnHome = view.findViewById(R.id.btnReturnHome)

        btnReturnHome.setOnClickListener {
            // Navegar de volta para a tela inicial
            val homeFragment = Home()
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, homeFragment)
                .commit()
        }

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    paciente = documentSnapshot.getString("paciente") ?: ""
                    updateView(paciente)
                    if (!paciente.isNullOrEmpty()) {
                        val chamadoRef = db.collection("Chamados").document(paciente)
                        listenerRegistration = chamadoRef.addSnapshotListener { chamadoSnapshot, error ->
                            if (error != null) {
                                // Ocorreu um erro ao consultar o documento do chamado
                                return@addSnapshotListener
                            }

                            if (chamadoSnapshot != null && chamadoSnapshot.exists()) {
                                val nome = chamadoSnapshot.getString("nome")
                                val telefone = chamadoSnapshot.getString("telefone")
                                val imagens = chamadoSnapshot.get("imagens") as? List<String>

                                // Define os valores nos elementos de layout
                                tvNomeConsulta.text = nome
                                tvTelefoneConsulta.text = telefone

                                if (imagens != null) {
                                    if (imagens.isNotEmpty()) {
                                        if (imagens.size >= 1) {
                                            loadImage(imagens[0], ivImagemConsulta1)
                                        }
                                        if (imagens.size >= 2) {
                                            loadImage(imagens[1], ivImagemConsulta2)
                                        }
                                        if (imagens.size >= 3) {
                                            loadImage(imagens[2], ivImagemConsulta3)
                                        }
                                    }
                                }
                            } else {
                                // O documento do chamado não existe
                            }
                        }
                    }
                } else {
                    // O documento do usuário logado não existe
                }
            }
            .addOnFailureListener { exception ->
                // Ocorreu um erro ao consultar o documento do usuário logado
            }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancela o registro do listener ao destruir a view
        listenerRegistration?.remove()
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(requireContext())
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    private fun updateView(paciente: String?) {
        if (paciente.isNullOrEmpty()) {
            tvNomeConsulta.visibility = View.GONE
            tvNomeConsul.visibility = View.GONE
            tvTelefoneConsulta.visibility = View.GONE
            tvTelefoneConsul.visibility = View.GONE
            ivImagemConsulta1.visibility = View.GONE
            ivImagemConsulta2.visibility = View.GONE
            ivImagemConsulta3.visibility = View.GONE
            tvEntreContat.visibility = View.GONE
            tvSemPaciente.visibility = View.VISIBLE
            btnReturnHome.visibility = View.VISIBLE
        } else {
            tvNomeConsulta.visibility = View.VISIBLE
            tvNomeConsul.visibility = View.VISIBLE
            tvTelefoneConsulta.visibility = View.VISIBLE
            tvTelefoneConsul.visibility = View.VISIBLE
            ivImagemConsulta1.visibility = View.VISIBLE
            ivImagemConsulta2.visibility = View.VISIBLE
            ivImagemConsulta3.visibility = View.VISIBLE
            tvEntreContat.visibility = View.VISIBLE
            tvSemPaciente.visibility = View.GONE
            btnReturnHome.visibility = View.GONE
        }
    }
}
