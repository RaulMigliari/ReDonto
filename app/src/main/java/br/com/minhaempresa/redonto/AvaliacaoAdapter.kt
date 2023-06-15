package br.com.minhaempresa.redonto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AvaliacaoAdapter(private var avaliacoes: List<Map<String, Any>>) : RecyclerView.Adapter<AvaliacaoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ratingBar: RatingBar = itemView.findViewById(R.id.rbNotaResul)
        val comentarioTextView: TextView = itemView.findViewById(R.id.tvComentarioResul)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_avaliacoes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avaliacao = avaliacoes[position]
        val nota = avaliacao["nota"] as? Double
        val comentario = avaliacao["comentario"] as? String

        if (nota != null) {
            holder.ratingBar.rating = nota.toFloat()
        }

        if (comentario != null) {
            holder.comentarioTextView.text = comentario
        }
    }

    override fun getItemCount(): Int {
        return avaliacoes.size
    }

    fun setAvaliacoes(avaliacoes: List<Map<String, Any>>) {
        this.avaliacoes = avaliacoes
        notifyDataSetChanged()
    }
}
