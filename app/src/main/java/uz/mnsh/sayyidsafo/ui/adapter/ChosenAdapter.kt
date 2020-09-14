package uz.mnsh.sayyidsafo.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.gson.Gson
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.fragment.ChosenAction
import kotlin.collections.HashMap

class ChosenAdapter(
    private val listenActions: ChosenAction
) :
    RecyclerView.Adapter<ChosenAdapter.ChosenViewHolder>() {

    private var idList: HashMap<Int, Int> = HashMap()
    var isPlay: Int = -1

    class ChosenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: AppCompatTextView = view.findViewById(R.id.tv_name)
        val tvDuration: AppCompatTextView = view.findViewById(R.id.tv_time)
        val tvSize: AppCompatTextView = view.findViewById(R.id.tv_size)
        val progressBar: ProgressBar = view.findViewById(R.id.progress)
        val download: AppCompatImageView = view.findViewById(R.id.img_download)
        val chosen: AppCompatImageView = view.findViewById(R.id.img_chosen)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.relative_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChosenViewHolder {
        return ChosenViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audios_container, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listenActions.listChosen.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChosenViewHolder, position: Int) {
        holder.tvTitle.text = listenActions.listChosen[position].name
        holder.tvSize.text = String.format("%.2f", listenActions.listChosen[position].size.toLong() / 1000.0) + "Мб"
        holder.tvDuration.text = getFormattedTime(listenActions.listChosen[position].duration.toLong())

        if (listenActions.listAudios.contains(listenActions.listChosen[position].getFileName())) {
            if (isPlay == position){
                holder.download.setImageResource(R.drawable.stop)
            }else{
                holder.download.setImageResource(R.drawable.play)
            }
        } else {
            holder.download.setImageResource(R.drawable.download)
        }

        holder.relativeLayout.setOnClickListener {
            if (listenActions.listAudios.contains(listenActions.listChosen[position].getFileName())){
                listenActions.itemClick(SongModel(
                    name = listenActions.listChosen[position].name,
                    songPath = App.DIR_PATH + listenActions.listChosen[position].topic_id + "/" + listenActions.listChosen[position].getFileName(),
                    topicID = listenActions.listChosen[position].topic_id.toString()
                ))
            }else startDownload(position, holder)
        }

        holder.chosen.setOnClickListener {
            listenActions.deleteChosen(listenActions.listChosen[position])
            listenActions.listChosen.removeAt(position)
        }

        holder.download.setOnClickListener {
            if (listenActions.listAudios.contains(listenActions.listChosen[position].getFileName())){
                listenActions.itemClick(SongModel(
                    name = listenActions.listChosen[position].name,
                    songPath = App.DIR_PATH + listenActions.listChosen[position].topic_id + "/" + listenActions.listChosen[position].getFileName(),
                    topicID = listenActions.listChosen[position].topic_id.toString()
                ))
            }else startDownload(position, holder)
        }
    }

    private fun startDownload(index: Int, holder: ChosenViewHolder) {
        if (idList[index] != null && PRDownloader.getStatus(idList[index]!!) == Status.RUNNING) {
            PRDownloader.cancel(idList[index]!!)
            holder.progressBar.visibility = View.GONE
            notifyItemChanged(index)
        } else {
            holder.progressBar.visibility = View.VISIBLE
            holder.download.setImageResource(R.drawable.cancel)
            idList[index] = PRDownloader.download(
                "http://5.182.26.44:8080/storage/" + listenActions.listChosen[index].location,
                App.DIR_PATH + "${listenActions.listChosen[index].topic_id}/",
                listenActions.listChosen[index].getFileName()
            ).build()
                .setOnProgressListener {
                    holder.progressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        listenActions.listAudios.add(listenActions.listChosen[index].getFileName())
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
}