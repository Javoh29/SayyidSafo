package uz.mnsh.sayyidsafo.ui.fragment

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chosen.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.adapter.ChosenAdapter
import uz.mnsh.sayyidsafo.ui.adapter.SavedAdapter
import uz.mnsh.sayyidsafo.ui.base.ScopedFragment
import uz.mnsh.sayyidsafo.utils.ListenActions
import java.io.File

class ChosenFragment : ScopedFragment(R.layout.fragment_chosen), KodeinAware, ListenActions {

    override val kodein by closestKodein()
    private val viewModelFactory: ChosenViewModelFactory by instance<ChosenViewModelFactory>()
    private lateinit var viewModel: ChosenViewModel
    private var isChange: Boolean = true

    private var listAudiosFile: ArrayList<String> = ArrayList()
    private var adapter: ChosenAdapter? = null
    private val listModel: ArrayList<UnitAudioModel> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerChosen.layoutManager = LinearLayoutManager(context)

        viewModel = viewModelFactory.create(ChosenViewModel::class.java)
        loadData()
    }

    private fun loadData() = launch {
        listAudiosFile.clear()
        val metaRetriever = MediaMetadataRetriever()
        File(App.DIR_PATH).walkTopDown().forEach { file ->
            if (file.name.endsWith(".mp3")){
                listAudiosFile.add(file.name)
                metaRetriever.setDataSource(file.path)
                listModel.add(UnitAudioModel(id = 0, name = file.name, duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong(), location = "", size = file.length(), topic_id = "1"))
            }
        }

        viewModel.getChosen().value.await().observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it.isNotEmpty()){
                bindUI(it)
            }
        })
    }

    private fun bindUI(list: List<UnitAudioModel>){
        adapter = ChosenAdapter(ArrayList(list), this)
        if (isChange) {
            tvChosen.setBackgroundResource(R.drawable.text_view_border)
            tvSaved.setBackgroundResource(R.drawable.text_view_bg)
            recyclerChosen.adapter = adapter
        }else {
            tvChosen.setBackgroundResource(R.drawable.text_view_bg)
            tvSaved.setBackgroundResource(R.drawable.text_view_border)
            recyclerChosen.adapter = SavedAdapter(listModel, this)
        }

        tvChosen.setOnClickListener {
            isChange = true
            bindUI(list)
        }

        tvSaved.setOnClickListener {
            isChange = false
            bindUI(list)
        }
    }

    override val listAudios: ArrayList<String>
        get() = listAudiosFile

    override fun addChosen(unitAudioModel: UnitAudioModel) {
    }

    override fun deleteChosen(id: Int) {
        viewModel.deleteChosen(id)
    }

    override fun playPause() {
        MainActivity.playerAdapter!!.resumeOrPause()
    }

    override fun isPause(): Boolean {
        return MainActivity.playerAdapter!!.isPlaying()
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.playerAdapter != null){
            MainActivity.playerAdapter!!.getCurrentTitle().observeForever {
                if (it == null) return@observeForever
                if (adapter != null && it != ""){
                    adapter!!.listModel.forEachIndexed { index, model ->
                        if (model.name == it){
                            adapter?.isPlaying = index
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }else{
                    Handler().postDelayed(Runnable {
                        if (adapter != null && it != ""){
                            adapter!!.listModel.forEachIndexed { index, model ->
                                if (model.name == it){
                                    if (MainActivity.playerAdapter!!.isPlaying()){
                                        adapter?.isPlaying = index
                                        adapter?.notifyDataSetChanged()
                                    }else{
                                        adapter?.isPlaying = index
                                    }
                                }
                            }
                        }
                    }, 300)
                }
            }
        }
    }

}