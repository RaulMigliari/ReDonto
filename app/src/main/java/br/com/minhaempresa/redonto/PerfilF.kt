package br.com.minhaempresa.redonto

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PerfilF : Fragment() {
    private lateinit var ivFotoPerfil: ImageView
    private lateinit var tvNomePerfil: TextView
    private lateinit var tvEmailPerfil: TextView
    private lateinit var tvCPFVal: TextView
    private lateinit var tvTelefoneVal: TextView
    private lateinit var tvSenhaFimPerf: TextView

    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil)
        tvNomePerfil = view.findViewById(R.id.tvTituloConfig)
        tvEmailPerfil = view.findViewById(R.id.tvEmailPerfil)
        tvTelefoneVal = view.findViewById(R.id.tvTelefoneVal)
        tvCPFVal = view.findViewById(R.id.tvCPFValPerfil)
        tvSenhaFimPerf = view.findViewById(R.id.tvSenhaFimPerf)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val perfilRef = db.collection("Usuarios").document(auth.currentUser?.uid.toString())

        perfilRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data
                tvNomePerfil.text = data?.get("nome") as String
                tvEmailPerfil.text = data?.get("email") as String
                tvTelefoneVal.text = data?.get("telefone") as String
                tvCPFVal.text = data?.get("cpf") as String
                tvSenhaFimPerf.text = data?.get("senhaFim") as String

                val fotoPerfilUrl = data?.get("fotoPerfil") as String?

                // Carrega e exibe a fotoPerfil
                if (fotoPerfilUrl != null) {
                    Glide.with(requireContext())
                        .load(fotoPerfilUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivFotoPerfil)
                }
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Erro ao recuperar dados do Firestore:", exception)
        }

        val botaoAval = view.findViewById<LinearLayout>(R.id.LinearAval)
        botaoAval.setOnClickListener {
            val fragmentAvaliacoes = Avaliacoes()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentAvaliacoes)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val botaoConfig = view.findViewById<LinearLayout>(R.id.LinearConfig)
        botaoConfig.setOnClickListener {
            val fragmentConfig = Config()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentConfig)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val botaoCurriculo = view.findViewById<LinearLayout>(R.id.LinearCurri)
        botaoCurriculo.setOnClickListener {
            val fragmentCurriculo = Curriculo()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentCurriculo)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val botaoEnderecos = view.findViewById<Button>(R.id.btnEnderecos)
        botaoEnderecos.setOnClickListener {
            val fragmentEnderecos = Enderecos()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.frame_layout, fragmentEnderecos)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}
