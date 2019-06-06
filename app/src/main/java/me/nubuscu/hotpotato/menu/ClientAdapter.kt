package me.nubuscu.hotpotato.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.model.ClientDetailsModel

/**
 * Adapter to display a list of connected Nearby clients
 */
class ClientAdapter(
    private val clients: List<ClientDetailsModel>,
    private val clickListener: (client: ClientDetailsModel) -> Unit
) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {
    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.clientName)
        val kickButton: Button = view.findViewById(R.id.kickButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.client_in_lobby_view_holder, parent, false)
        return ClientViewHolder(view)
    }

    override fun getItemCount(): Int = clients.size

    override fun onBindViewHolder(holder: ClientViewHolder, i: Int) {
        holder.clientName.text = clients[i].name
        holder.kickButton.setOnClickListener { clickListener(clients[i]) }

    }
}