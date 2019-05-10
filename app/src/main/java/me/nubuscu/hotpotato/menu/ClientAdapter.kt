package me.nubuscu.hotpotato.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.model.ClientDetailsModel

/**
 * Adapter to display a list of connected Nearby clients
 */
class ClientAdapter(private val clients: List<ClientDetailsModel>, private val clickListener: () -> Unit) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {
    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.clientName)
        //TODO kick button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.client_in_lobby_view_holder, parent, false)
        val holder = ClientViewHolder(view)
        view.setOnClickListener { clickListener() }
        return holder
    }

    override fun getItemCount(): Int = clients.size

    override fun onBindViewHolder(holder: ClientViewHolder, i: Int) {
        holder.clientName.text = clients[i].name
    }
}