package uz.mnsh.sayyidsafo.ui.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Status
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.utils.ListenActions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AudiosAdapter(
    listModel: List<UnitAudioModel>,
    private val chosenModel: List<UnitAudioModel>,
    private val listenActions: ListenActions
) :
    RecyclerView.Adapter<AudiosAdapter.AudiosViewHolder>() {

    var listModel: ArrayList<UnitAudioModel> = ArrayList(listModel)
    private val listModelReserv: List<UnitAudioModel> = listModel
    private var idList: HashMap<Int, Int> = HashMap()
    var isPlay: Int = -1

    class AudiosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: AppCompatTextView = view.findViewById(R.id.tv_name)
        val tvDuration: AppCompatTextView = view.findViewById(R.id.tv_time)
        val tvSize: AppCompatTextView = view.findViewById(R.id.tv_size)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val download: AppCompatImageView = view.findViewById(R.id.img_download)
        val chosen: AppCompatImageView = view.findViewById(R.id.img_chosen)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.relative_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudiosViewHolder {
        return AudiosViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audios_container, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listModel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AudiosViewHolder, position: Int) {
        holder.tvTitle.text = listModel[position].name
        holder.tvSize.text = String.format("%.2f", listModel[position].size.toLong() / 1000.0) + "Мб"
        holder.tvDuration.text = getFormattedTime(listModel[position].duration.toLong())
        holder.progressBar.visibility = View.GONE

        if (listenActions.listAudios.contains(listModel[position].getFileName())) {
            if (isPlay == position) {
                holder.download.setImageResource(R.drawable.stop)
            } else {
                holder.download.setImageResource(R.drawable.play)
            }
        } else {
            holder.download.setImageResource(R.drawable.download)
        }

        if (chosenModel.contains(listModel[position])) {
            holder.chosen.setImageResource(R.drawable.ic_chosen_on)
        } else {
            holder.chosen.setImageResource(R.drawable.ic_chosen)
        }

        holder.relativeLayout.setOnClickListener {
            if (listenActions.listAudios.contains(listModel[position].getFileName())){
                listenActions.itemPlay(SongModel(
                    name = listModel[position].name,
                    songPath = App.DIR_PATH + listModel[position].topic_id + "/" + listModel[position].getFileName(),
                    topicID = listModel[position].topic_id.toString()
                ))
            }else startDownload(position, holder)
        }

        holder.chosen.setOnClickListener {
            if (chosenModel.contains(listModel[position])) {
                holder.chosen.setImageResource(R.drawable.ic_chosen)
                listenActions.deleteChosen(listModel[position].id)
            } else {
                holder.chosen.setImageResource(R.drawable.ic_chosen_on)
                listenActions.addChosen(listModel[position])
            }
        }

        holder.download.setOnClickListener {
            if (listenActions.listAudios.contains(listModel[position].getFileName())){
                listenActions.itemPlay(SongModel(
                    name = listModel[position].name,
                    songPath = App.DIR_PATH + listModel[position].topic_id + "/" + listModel[position].getFileName(),
                    topicID = listModel[position].topic_id.toString()
                ))
            }else startDownload(position, holder)
        }
    }

    private fun startDownload(index: Int, holder: AudiosViewHolder) {
        if (idList[index] != null && PRDownloader.getStatus(idList[index]!!) == Status.RUNNING) {
            PRDownloader.cancel(idList[index]!!)
            holder.progressBar.visibility = View.GONE
            notifyItemChanged(index)
        } else {
            holder.progressBar.visibility = View.VISIBLE
            holder.download.setImageResource(R.drawable.cancel)
            idList[index] = PRDownloader.download(
                "http://5.182.26.44:8080/storage/" + listModel[index].location,
                App.DIR_PATH + "${listModel[index].topic_id}/",
                listModel[index].getFileName()
            ).build()
                .setOnProgressListener {
                    holder.progressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        listenActions.listAudios.add(listModel[index].getFileName())
                        idList.remove(index)
                        notifyItemChanged(index)
                    }

                    override fun onError(error: com.downloader.Error?) {
                        Log.d("BAG", error?.responseCode.toString())
                        notifyItemChanged(index)
                    }
                })
        }
    }

    private fun getFormattedTime(seconds: Long): String {
        val minutes = seconds / 60
        return String.format("%d:%02d", minutes, seconds % 60)
    }

    fun searchAudio(text: String) {
        listModel.clear()
        if (text.isEmpty()) {
            listModel = ArrayList(listModelReserv)
        } else {
            listModelReserv.forEach { it ->
                if (it.name.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                    listModel.add(it)
                }
            }
        }
        notifyDataSetChanged()
    }
}