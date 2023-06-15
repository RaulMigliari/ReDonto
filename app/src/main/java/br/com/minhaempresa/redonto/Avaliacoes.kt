package br.com.minhaempresa.redonto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Avaliacoes : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_avaliacoes, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAvaliacoes)

        // Configurando o RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        val adapter = AvaliacaoAdapter(emptyList())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // Obtendo as avaliações do usuário logado e atualizando o RecyclerView
        getAvaliacoesDoUsuarioLogado(adapter)

        return view
    }

    private fun getAvaliacoesDoUsuarioLogado(adapter: AvaliacaoAdapter) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()

        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("Usuarios")
                .document(userId)
                .collection("avaliacoes")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val avaliacoes = mutableListOf<Map<String, Any>>()
                    for (documentSnapshot in querySnapshot.documents) {
                        val comentario = documentSnapshot.getString("comentario")
                        val nota = documentSnapshot.getDouble("nota")

                        if (comentario != null && nota != null) {
                            val avaliacaoData = mapOf(
                                "comentario" to comentario,
                                "nota" to nota
                            )
                            avaliacoes.add(avaliacaoData)
                        }
                    }
                    adapter.setAvaliacoes(avaliacoes)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Tratar falha na obtenção dos dados do Firestore
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Avaliacoes().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

