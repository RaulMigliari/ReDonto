package br.com.minhaempresa.redonto

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Enderecos.newInstance] factory method to
 * create an instance of this fragment.
 */
class Enderecos : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var tvRuaValEnd1: TextView
    private lateinit var tvBairroValEnd1: TextView
    private lateinit var tvCepValEnd1: TextView
    private lateinit var tvNumeroValEnd1: TextView
    private lateinit var tvComplementoValEnd1: TextView

    private lateinit var tvRuaValEnd2: TextView
    private lateinit var tvBairroValEnd2: TextView
    private lateinit var tvCepValEnd2: TextView
    private lateinit var tvNumeroValEnd2: TextView
    private lateinit var tvComplementoValEnd2: TextView

    private lateinit var tvRuaValEnd3: TextView
    private lateinit var tvBairroValEnd3: TextView
    private lateinit var tvCepValEnd3: TextView
    private lateinit var tvNumeroValEnd3: TextView
    private lateinit var tvComplementoValEnd3: TextView

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_enderecos, container, false)

        tvRuaValEnd1 = view.findViewById(R.id.tvRuaValEnd1)
        tvBairroValEnd1 = view.findViewById(R.id.tvBairroValEnd1)
        tvCepValEnd1 = view.findViewById(R.id.tvCepValEnd1)
        tvNumeroValEnd1 = view.findViewById(R.id.tvNumeroValEnd1)
        tvComplementoValEnd1 = view.findViewById(R.id.tvComplementoValEnd1)

        tvRuaValEnd2 = view.findViewById(R.id.tvRuaValEnd2)
        tvBairroValEnd2 = view.findViewById(R.id.tvBairroValEnd2)
        tvCepValEnd2 = view.findViewById(R.id.tvCepValEnd2)
        tvNumeroValEnd2 = view.findViewById(R.id.tvNumeroValEnd2)
        tvComplementoValEnd2 = view.findViewById(R.id.tvComplementoValEnd2)

        tvRuaValEnd3 = view.findViewById(R.id.tvRuaValEnd3)
        tvBairroValEnd3 = view.findViewById(R.id.tvBairroValEnd3)
        tvCepValEnd3 = view.findViewById(R.id.tvCepValEnd3)
        tvNumeroValEnd3 = view.findViewById(R.id.tvNumeroValEnd3)
        tvComplementoValEnd3 = view.findViewById(R.id.tvComplementoValEnd3)

        // Recupera dados do Firestore
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val perfilRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        perfilRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.data
                // Exibe os dados na tela de perfil
                tvRuaValEnd1.text = data?.get("rua1") as? String ?: ""
                tvBairroValEnd1.text = data?.get("bairro1") as? String ?: ""
                tvCepValEnd1.text = data?.get("cep1") as? String ?: ""
                tvNumeroValEnd1.text = data?.get("numero1") as? String ?: ""
                tvComplementoValEnd1.text = data?.get("complemento1") as? String ?: ""

                tvRuaValEnd2.text = data?.get("rua2") as? String ?: ""
                tvBairroValEnd2.text = data?.get("bairro2") as? String ?: ""
                tvCepValEnd2.text = data?.get("cep2") as? String ?: ""
                tvNumeroValEnd2.text = data?.get("numero2") as? String ?: ""
                tvComplementoValEnd2.text = data?.get("complemento2") as? String ?: ""

                tvRuaValEnd3.text = data?.get("rua3") as? String ?: ""
                tvBairroValEnd3.text = data?.get("bairro3") as? String ?: ""
                tvCepValEnd3.text = data?.get("cep3") as? String ?: ""
                tvNumeroValEnd3.text = data?.get("numero3") as? String ?: ""
                tvComplementoValEnd3.text = data?.get("complemento3") as? String ?: ""
            } else {
                Log.d(ContentValues.TAG, "Não há dados no documento!")
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Erro ao recuperar dados do Firestore:", exception)
        }


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Enderecos.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Enderecos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}