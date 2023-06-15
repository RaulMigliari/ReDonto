package br.com.minhaempresa.redonto

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChamadosEspera : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var irConfigButton: Button
    private lateinit var irChamadosButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var offlineTextView: TextView
    private lateinit var estaEmConsultaTextView: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var userRef: DocumentReference
    private lateinit var chamadoAdapter: ChamadoAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var listChamados: MutableList<ListChamados>
    private lateinit var tvAceiteChamadoEspera: TextView
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private var chamadosStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chamados_espera, container, false)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        tvAceiteChamadoEspera = view.findViewById(R.id.tvAceiteChamadoEspera)
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        currentUser = auth.currentUser!!

        irConfigButton = view.findViewById(R.id.btnIrConfigChamadosEspera)
        irConfigButton.setOnClickListener {
            val fragmentConfig = Config()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentConfig)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        irChamadosButton = view.findViewById(R.id.btnIrConsultaChamadoEspera)
        irChamadosButton.setOnClickListener {
            val fragmentChamados = Consulta()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentChamados)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        offlineTextView = view.findViewById(R.id.tvReceberChamadosEspera)
        estaEmConsultaTextView = view.findViewById(R.id.tvEstaEmConsultaChamadoEspera)

        userRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                chamadosStatus = snapshot.getString("status") ?: ""
                val chamadosList = listChamados.toList()
                updateView(chamadosStatus, chamadosList)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        recyclerView = view.findViewById(R.id.rvChamadosEspera)
        recyclerView.layoutManager = LinearLayoutManager(context)
        listChamados = mutableListOf()
        chamadoAdapter = ChamadoAdapter(listChamados) { chamado ->
            onChamadoClick(chamado)
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            carregarChamadosDoDentista(userId)
        }

        recyclerView.adapter = chamadoAdapter

        return view
    }

    private fun carregarChamadosDoDentista(userId: String) {
        getChamadosDoDentista(userId) { chamados ->
            listChamados.clear()
            listChamados.addAll(chamados)
            chamadoAdapter.notifyDataSetChanged()

            updateView(chamadosStatus, chamados)
        }
    }

    private fun onChamadoClick(chamado: ListChamados) {
        val detalhes2Fragment = Detalhes2.newInstance(chamado)
        val transaction = requireFragmentManager().beginTransaction()
        transaction.replace(R.id.frame_layout, detalhes2Fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getChamadosDoDentista(userId: String, callback: (List<ListChamados>) -> Unit) {
        val userReference = db.collection("Usuarios").document(userId)

        userReference.get().addOnSuccessListener { userSnapshot ->
            val emergencias = userSnapshot.get("emergencias") as? List<String>

            if (emergencias != null && emergencias.isNotEmpty()) {
                val chamadosCollection = db.collection("Chamados")
                    .whereIn(FieldPath.documentId(), emergencias)
                    .whereEqualTo("status", "espera")

                chamadosCollection.addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        callback(emptyList()) // Caso ocorra algum erro, retorna uma lista vazia
                        return@addSnapshotListener
                    }

                    val chamadosList = mutableListOf<ListChamados>()

                    for (dc in querySnapshot?.documentChanges ?: emptyList()) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val chamadoId = dc.document.id
                            val nome = dc.document.getString("nome")
                            val imagens = dc.document.get("imagens") as? List<String>

                            if (chamadoId != null && nome != null && imagens != null) {
                                chamadosList.add(ListChamados(chamadoId, nome, imagens))
                            }
                        }
                    }

                    callback(chamadosList)
                }
            } else {
                callback(emptyList()) // Se a lista de emergências estiver vazia, retorna uma lista vazia
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Erro ao obter dados do usuário: $exception")
            callback(emptyList()) // Caso ocorra algum erro, retorna uma lista vazia
        }
    }

    private fun updateView(status: String, chamadosList: List<ListChamados>) {
        when (status) {
            "online" -> {
                recyclerView.visibility = View.VISIBLE
                offlineTextView.visibility = View.GONE
                irConfigButton.visibility = View.GONE
                irChamadosButton.visibility = View.GONE
                estaEmConsultaTextView.visibility = View.GONE
                tvAceiteChamadoEspera.visibility = if (chamadosList.isEmpty()) View.VISIBLE else View.GONE
            }
            "offline" -> {
                recyclerView.visibility = View.GONE
                offlineTextView.visibility = View.VISIBLE
                irConfigButton.visibility = View.VISIBLE
                irChamadosButton.visibility = View.GONE
                estaEmConsultaTextView.visibility = View.GONE
                tvAceiteChamadoEspera.visibility = View.GONE
            }
            "emConsulta" -> {
                recyclerView.visibility = View.GONE
                offlineTextView.visibility = View.GONE
                irConfigButton.visibility = View.GONE
                irChamadosButton.visibility = View.VISIBLE
                estaEmConsultaTextView.visibility = View.VISIBLE
                tvAceiteChamadoEspera.visibility = View.GONE
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChamadosEspera().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
