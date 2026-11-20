package cn.stu.edu.sicnu.tay.lab07

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 音乐列表适配器
 */
class MusicAdapter(
    private val musicList: List<Music>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var currentPlayingIndex: Int = -1

    fun setCurrentPlayingIndex(index: Int) {
        val oldIndex = currentPlayingIndex
        currentPlayingIndex = index
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (index >= 0) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        holder.tvTitle.text = music.title
        holder.tvArtist.text = music.artist
        holder.tvDuration.text = music.formatDuration()

        // 高亮当前播放项
        if (position == currentPlayingIndex) {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.selected_item)
            )
        } else {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.transparent)
            )
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = musicList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
    }
}
