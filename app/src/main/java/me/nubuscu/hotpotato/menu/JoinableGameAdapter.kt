package me.nubuscu.hotpotato.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.nubuscu.hotpotato.model.JoinableGameModel
import me.nubuscu.hotpotato.R

class JoinableGameAdapter(
    private val games: List<JoinableGameModel>,
    private val clickListener: (JoinableGameModel) -> Unit
) :
    RecyclerView.Adapter<JoinableGameAdapter.JoinableGameViewHolder>() {

    class JoinableGameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.endpointName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinableGameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.joinable_game_view_holder, parent, false)
        val holder = JoinableGameViewHolder(view)
        view.setOnClickListener { clickListener(games[holder.adapterPosition]) }
        return holder
    }

    override fun getItemCount(): Int = games.size

    override fun onBindViewHolder(holder: JoinableGameViewHolder, i: Int) {
        holder.name.text = games[i].endpointName
    }

}
