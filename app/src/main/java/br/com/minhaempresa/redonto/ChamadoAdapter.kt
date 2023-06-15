package br.com.minhaempresa.redonto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChamadoAdapter(
    private val listChamados: List<ListChamados>,
    private val onItemClick: (chamado: ListChamados) -> Unit
) : RecyclerView.Adapter<ChamadoAdapter.ChamadoViewHolder>() {

    inner class ChamadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeTextView: TextView = itemView.findViewById(R.id.tvNomeCham)
        private val imagemImageView1: ImageView = itemView.findViewById(R.id.ivChamados1)
        private val imagemImageView2: ImageView = itemView.findViewById(R.id.ivChamados2)
        private val imagemImageView3: ImageView = itemView.findViewById(R.id.ivChamados3)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chamado = listChamados[position]
                    onItemClick(chamado)
                }
            }

            imagemImageView1.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chamado = listChamados[position]
                    onItemClick(chamado)
                }
            }

            imagemImageView2.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chamado = listChamados[position]
                    onItemClick(chamado)
                }
            }

            imagemImageView3.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chamado = listChamados[position]
                    onItemClick(chamado)
                }
            }
        }

        fun bind(chamado: ListChamados) {
            nomeTextView.text = chamado.nome

            val imagens = listOf(imagemImageView1, imagemImageView2, imagemImageView3)

            for (i in imagens.indices) {
                val imagemImageView = imagens[i]

                if (i < chamado.imagens.size) {
                    val imageUrl = chamado.imagens[i]
                    Glide.with(itemView)
                        .load(imageUrl)
                        .into(imagemImageView)
                } else {
                    imagemImageView.setImageDrawable(null)
                }
            }

            if (chamado.imagens.size > 1) {
                val swipeIndicator = ContextCompat.getDrawable(itemView.context, R.drawable.indicator_swipe)
                imagens.forEach { imagemImageView ->
                    imagemImageView.overlay.clear()
                    swipeIndicator?.let {
                        imagemImageView.overlay.add(it)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChamadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_chamados,
            parent,
            false
        )
        return ChamadoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChamadoViewHolder, position: Int) {
        val currentItem = listChamados[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = listChamados.size
}
