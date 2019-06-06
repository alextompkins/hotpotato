package me.nubuscu.hotpotato.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.makeBitmap
import me.nubuscu.hotpotato.util.makeRoundDrawableFromBitmap

/**
 * Adapter to display a list of connected Nearby clients
 */
class ClientAdapter(
    private val clients: List<ClientDetailsModel>,
    private val clickListener: ((client: ClientDetailsModel) -> Unit)? = null
) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {
    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clientName: TextView = view.findViewById(R.id.clientName)
        val kickButton: Button = view.findViewById(R.id.kickButton)
        val profilePic: ImageView = view.findViewById(R.id.profilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.client_in_lobby_view_holder, parent, false)
        return ClientViewHolder(view)
    }

    override fun getItemCount(): Int = clients.size

    override fun onBindViewHolder(holder: ClientViewHolder, i: Int) {
        val client = clients[i]
        holder.clientName.text = client.name
        clickListener?.let { holder.kickButton.setOnClickListener { it(client) } }
        if (clickListener == null) {
            holder.kickButton.isVisible = false
        }
        client.profilePicture?.let {
            val drawable = makeRoundDrawableFromBitmap(DataHolder.instance.context.get()!!.resources, makeBitmap(it))
            holder.profilePic.setImageDrawable(drawable)
        }
    }
}