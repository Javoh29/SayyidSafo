package uz.mnsh.sayyidsafo.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.fragment.ChosenAction
import kotlin.collections.ArrayList

class SavedAdapter(
    private val listModel: ArrayList<UnitAudioModel>,
    private val chosenModel: List<UnitAudioModel>,
    private val listenActions: ChosenAction
) :
    RecyclerView.Adapter<SavedAdapter.SavedViewHolder>() {

    var isPlay: Int = -1

    class SavedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: AppCompatTextView = view.findViewById(R.id.tv_name)
        val tvDuration: AppCompatTextView = view.findViewById(R.id.tv_time)
        val tvSize: AppCompatTextView = view.findViewById(R.id.tv_size)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val download: AppCompatImageView = view.findViewById(R.id.img_download)
        val chosen: AppCompatImageView = view.findViewById(R.id.img_chosen)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.relative_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        return SavedViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audios_container, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listModel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        holder.tvTitle.text = listModel[position].name
        holder.tvSize.text = String.format("%.2f", listModel[position].size.toLong() / 1000.0) + "Мб"
        holder.tvDuration.text = getFormattedTime(listModel[position].duration.toLong())

        if (isPlay == position){
            holder.download.setImageResource(R.drawable.stop)
        }else{
            holder.download.setImageResource(R.drawable.play)
        }

        if (chosenModel.contains(listModel[position])){
            holder.chosen.setImageResource(R.drawable.ic_chosen_on)
        }else{
            holder.chosen.setImageResource(R.drawable.ic_chosen)
        }

        holder.relativeLayout.setOnClickListener {
            listenActions.itemClick(SongModel(
                name = listModel[position].name,
                songPath = App.DIR_PATH + listModel[position].topic_id + "/" + listModel[position].getFileName(),
                topicID = listModel[position].topic_id.toString()
            ))
        }

        holder.chosen.setOnClickListener {
            if (chosenModel.contains(listModel[position])){
                holder.chosen.setImageResource(R.drawable.ic_chosen)
                listenActions.deleteChosen(listModel[position])
            }else{
                holder.chosen.setImageResource(R.drawable.ic_chosen_on)
                listenActions.addChosen(listModel[position])
            }
        }

        holder.download.setOnClickListener {
            listenActions.itemClick(SongModel(
                name = listModel[position].name,
                songPath = App.DIR_PATH + listModel[position].topic_id + "/" + listModel[position].getFileName(),
                topicID = listModel[position].topic_id.toString()
            ))
        }
    }


    private fun getFormattedTime(seconds: Long): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }
}