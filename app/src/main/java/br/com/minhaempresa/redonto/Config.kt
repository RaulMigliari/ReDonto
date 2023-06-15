package br.com.minhaempresa.redonto

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Config.newInstance] factory method to
 * create an instance of this fragment.
 */
class Config : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var logoutButton: Button

    val auth = FirebaseAuth.getInstance()
    private lateinit var switchCompat: SwitchCompat
    private lateinit var clientRef: DocumentReference

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
        val view = inflater.inflate(R.layout.fragment_config, container, false)

        logoutButton = view.findViewById(R.id.btnLogout)

        switchCompat = view.findViewById(R.id.swAtivarChamados)

        val db = Firebase.firestore
        clientRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        clientRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // tratamento de erro
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val statusString = snapshot.getString("status")
                val status = statusString ?: "offline"

                if (status == "online") {
                    // cliente online
                    switchCompat.isChecked = true
                    switchCompat.isEnabled = true
                } else if (status == "offline") {
                    // cliente offline
                    switchCompat.isChecked = false
                    switchCompat.isEnabled = true
                } else if (status == "emConsulta") {
                    // cliente em consulta
                    switchCompat.isChecked = false
                    switchCompat.isEnabled = false
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            val statusString = when {
                isChecked && switchCompat.isEnabled -> "online"
                !isChecked && switchCompat.isEnabled -> "offline"
                else -> "emConsulta"
            }

            db.collection("Usuarios").document(auth.currentUser?.uid.toString())
                .update("status", statusString)
                .addOnSuccessListener {
                    Log.d(TAG, "Status updated successfully")
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error updating status", it)
                }
        }

        logoutButton.setOnClickListener {
            // Desloga o usuário atual
            FirebaseAuth.getInstance().signOut()

            // Volta para a tela de login ou para outra tela de sua escolha
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
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
         * @return A new instance of fragment Config.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Config().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}