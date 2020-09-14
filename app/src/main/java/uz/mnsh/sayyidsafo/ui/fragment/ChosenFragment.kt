package uz.mnsh.sayyidsafo.ui.fragment

import android.graphics.Color
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
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.data.model.SongModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.adapter.ChosenAdapter
import uz.mnsh.sayyidsafo.ui.adapter.SavedAdapter
import uz.mnsh.sayyidsafo.ui.base.ScopedFragment
import java.io.File

class ChosenFragment : ScopedFragment(R.layout.fragment_chosen), KodeinAware, ChosenAction {

    override val kodein by closestKodein()
    private val viewModelFactory: ChosenViewModelFactory by instance<ChosenViewModelFactory>()
    private lateinit var viewModel: ChosenViewModel
    private var isChange: Boolean = true

    private var listAudiosFile: ArrayList<String> = ArrayList()
    private var adapter: ChosenAdapter? = null
    private var adapterSaved: SavedAdapter? = null
    private val listModel: ArrayList<UnitAudioModel> = ArrayList()
    private val listModelChosen: ArrayList<UnitAudioModel> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerChosen.layoutManager = LinearLayoutManager(context)

        viewModel = viewModelFactory.create(ChosenViewModel::class.java)
        loadData()
    }

    private fun loadData() = launch {
        listAudiosFile.clear()
        listModel.clear()
        File(App.DIR_PATH).walkTopDown().forEach { file ->
            if (file.name.endsWith(".mp3")){
                listAudiosFile.add(file.name)
                listModel.add(viewModel.getAudioForName(file.name.substring(0, file.name.length-4)))
            }
        }
        viewModel.getChosen().value.await().observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {
                bindUI(it)
            } else return@Observer
        })

    }

    private fun bindUI(list: List<UnitAudioModel>){
        listModelChosen.clear()
        listModelChosen.addAll(list)
        adapter = ChosenAdapter(this)
        adapterSaved = SavedAdapter(listModel, list, this)
        if (isChange) {
            tvChosen.setBackgroundResource(R.drawable.text_view_bg)
            tvChosen.setTextColor(Color.WHITE)
            tvSaved.setTextColor(resources.getColor(R.color.colorPrimary))
            tvSaved.setBackgroundResource(R.drawable.text_view_border)
            recyclerChosen.adapter = adapter
        }else {
            tvChosen.setBackgroundResource(R.drawable.text_view_border)
            tvChosen.setTextColor(resources.getColor(R.color.colorPrimary))
            tvSaved.setTextColor(Color.WHITE)
            tvSaved.setBackgroundResource(R.drawable.text_view_bg)
            recyclerChosen.adapter = adapterSaved
        }

        tvChosen.setOnClickListener {
            isChange = true
            bindUI(list)
        }

        tvSaved.setOnClickListener {
            isChange = false
            bindUI(list)
        }

        MainActivity.isSongPlay.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            var play = true
            if (isChange) {
                list.forEachIndexed { i, model ->
                    if (model.name == MainActivity.playerAdapter!!.getCurrentSong()?.name) {
                        if (it) {
                            adapter?.isPlay = i
                        } else adapter?.isPlay = -1
                        play = false
                    }
                }
                if (play) adapter?.isPlay = -1
                adapter?.notifyDataSetChanged()
            }else{
                listModel.forEachIndexed { i, model ->
                    if (model.name == MainActivity.playerAdapter!!.getCurrentSong()?.name) {
                        if (it) {
                            adapterSaved?.isPlay = i
                        } else adapterSaved?.isPlay = -1
                        play = false
                    }
                }
                if (play) adapterSaved?.isPlay = -1
                adapterSaved?.notifyDataSetChanged()
            }
        })
    }

    override val listAudios: ArrayList<String>
        get() = listAudiosFile

    override val listChosen: ArrayList<UnitAudioModel>
        get() = listModelChosen

    override fun addChosen(unitAudioModel: UnitAudioModel) {
        val model = ChosenModel(
            id = unitAudioModel.id,
            name = unitAudioModel.name,
            duration = unitAudioModel.duration,
            location = unitAudioModel.location,
            size = unitAudioModel.size,
            topic_id = unitAudioModel.topic_id,
            rn = unitAudioModel.rn
        )
        viewModel.setChosen(model)
    }

    override fun deleteChosen(model: UnitAudioModel) {
        viewModel.deleteChosen(model.id)
        listModelChosen.remove(model)
    }

    override fun itemClick(model: SongModel) {
        if (MainActivity.playerAdapter != null){
            if (MainActivity.playerAdapter!!.getCurrentSong()?.name == model.name){
                if (MainActivity.playerAdapter!!.getMediaPlayer() != null){
                    MainActivity.playerAdapter!!.resumeOrPause()
                }else{
                    MainActivity.playerAdapter!!.initMediaPlayer()
                }
            }else{
                MainActivity.playerAdapter!!.setCurrentSong(model, null)
                MainActivity.playerAdapter!!.initMediaPlayer()
            }
        }
    }

}

interface ChosenAction{
    val listAudios: ArrayList<String>

    val listChosen: ArrayList<UnitAudioModel>

    fun addChosen(unitAudioModel: UnitAudioModel)

    fun deleteChosen(model: UnitAudioModel)

    fun itemClick(model: SongModel)
}