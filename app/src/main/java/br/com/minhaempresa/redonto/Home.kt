package br.com.minhaempresa.redonto

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference

class Home : Fragment() {
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
    private var status: String = ""
    private var chamadosStatus: String = ""
    private lateinit var progressbar: ProgressBar
    private lateinit var estamosProcurando: TextView


    private var chamadosListener: ListenerRegistration? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        currentUser = auth.currentUser!!

        irConfigButton = view.findViewById(R.id.btnIrConfigChamadosEspera)
        irChamadosButton = view.findViewById(R.id.btnIrConsultaChamadoEspera)
        offlineTextView = view.findViewById(R.id.tvReceberChamadosEspera)
        estaEmConsultaTextView = view.findViewById(R.id.tvEstaEmConsultaChamadoEspera)
        recyclerView = view.findViewById(R.id.rvChamadosEspera)
        progressbar = view.findViewById(R.id.pbEsperando)
        estamosProcurando = view.findViewById(R.id.tvEspereChamados)

        recyclerView.layoutManager = LinearLayoutManager(context)
        listChamados = mutableListOf()
        userRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                status = snapshot.getString("status") ?: ""
                val chamadosList = listChamados.toList()
                updateView(status, chamadosList)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                status = snapshot.getString("status") ?: ""
                val chamadosList = listChamados.toList()
                updateView(status, chamadosList)
            }
        }
        userRef.collection("Chamados")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Verificar o status dos chamados
                    val chamado = snapshot.documents.firstOrNull()
                    chamadosStatus = chamado?.getString("status") ?: ""
                    val chamadosList = listChamados.toList()
                    updateView(status, chamadosList)
                } else {
                    chamadosStatus = ""
                    val chamadosList = listChamados.toList()
                    updateView(status, chamadosList)
                    Log.d(TAG, "No chamados found")
                }
            }

        chamadoAdapter = ChamadoAdapter(listChamados) { chamado ->
            onChamadoClick(chamado)
        }
        recyclerView.adapter = chamadoAdapter

        setupChamadosListener()
        getChamadosDoDentista { chamados ->
            listChamados.clear()
            listChamados.addAll(chamados)
            chamadoAdapter.notifyDataSetChanged()

            // Update the view after fetching the chamados
            updateView(status, chamados)
        }

        irConfigButton.setOnClickListener {
            val fragmentConfig = Config()
            requireFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragmentConfig)
                .addToBackStack(null)
                .commit()
        }

        irChamadosButton.setOnClickListener {
            val fragmentChamados = Consulta()
            requireFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragmentChamados)
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    private fun onChamadoClick(chamado: ListChamados) {
        val detalhesFragment = Detalhes.newInstance(chamado)
        requireFragmentManager().beginTransaction()
            .replace(R.id.frame_layout, detalhesFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getChamadosDoDentista(callback: (List<ListChamados>) -> Unit) {
        val chamadosList = mutableListOf<ListChamados>()

        val userId = currentUser.uid
        val userReference = db.document("/Usuarios/$userId")

        db.collection("Chamados")
            .whereEqualTo("status", "espera")
            .get()
            .addOnSuccessListener { allChamadosSnapshot ->
                val chamadosCount = allChamadosSnapshot.size()
                var chamadosProcessed = 0

                allChamadosSnapshot.documents.forEach { chamadoDoc ->
                    val chamadoId = chamadoDoc.id
                    val nome = chamadoDoc.getString("nome")
                    val imagens = chamadoDoc.get("imagens") as? List<String>

                    if (chamadoId != null && nome != null && imagens != null) {
                        val interesse = chamadoDoc.get("interesse") as? List<DocumentReference>
                        val naoInteresse =
                            chamadoDoc.get("naoInteresse") as? List<DocumentReference>

                        val hasInterest = interesse?.any { it == userReference } ?: false
                        val hasNotInterest = naoInteresse?.any { it == userReference } ?: false

                        if (!hasInterest && !hasNotInterest) {
                            chamadosList.add(ListChamados(chamadoId, nome, imagens))
                        }

                        chamadosProcessed++

                        if (chamadosProcessed == chamadosCount) {
                            callback(chamadosList)
                            val chamadosListFinal = listChamados.toList()
                            updateView(status, chamadosListFinal)
                        }
                    }
                }

            }
    }


    private fun setupChamadosListener() {
        val userId = currentUser.uid
        val userReference = db.document("/Usuarios/$userId")

        chamadosListener = db.collection("Chamados")
            .whereEqualTo("status", "espera")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.d(TAG, "Erro ao ouvir chamados: $exception")
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val chamadosList = mutableListOf<ListChamados>()

                    querySnapshot.documents.forEach { chamadoDoc ->
                        val chamadoId = chamadoDoc.id
                        val nome = chamadoDoc.getString("nome")
                        val imagens = chamadoDoc.get("imagens") as? List<String>

                        if (chamadoId != null && nome != null && imagens != null) {
                            val interesse = chamadoDoc.get("interesse") as? List<DocumentReference>
                            val naoInteresse = chamadoDoc.get("naoInteresse") as? List<DocumentReference>

                            val hasInterest = interesse?.any { it == userReference } ?: false
                            val hasNotInterest = naoInteresse?.any { it == userReference } ?: false

                            if (!hasInterest && !hasNotInterest) {
                                chamadosList.add(ListChamados(chamadoId, nome, imagens))
                            }
                        }
                    }

                    listChamados.clear()
                    listChamados.addAll(chamadosList)
                    chamadoAdapter.notifyDataSetChanged()
                }
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
                progressbar.visibility = if (chamadosList.isEmpty()) View.VISIBLE else View.GONE
                estamosProcurando.visibility = if (chamadosList.isEmpty()) View.VISIBLE else View.GONE
            }
            "offline" -> {
                recyclerView.visibility = View.GONE
                offlineTextView.visibility = View.VISIBLE
                irConfigButton.visibility = View.VISIBLE
                irChamadosButton.visibility = View.GONE
                estaEmConsultaTextView.visibility = View.GONE
                progressbar.visibility = View.GONE
                estamosProcurando.visibility = View.GONE
            }
            "emConsulta" -> {
                recyclerView.visibility = View.GONE
                offlineTextView.visibility = View.GONE
                irConfigButton.visibility = View.GONE
                irChamadosButton.visibility = View.VISIBLE
                estaEmConsultaTextView.visibility = View.VISIBLE
                progressbar.visibility = View.GONE
                estamosProcurando.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chamadosListener?.remove()
    }

    companion object {
        fun newInstance(): Home {
            return Home()
        }
    }
}

