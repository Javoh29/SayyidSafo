package uz.mnsh.sayyidsafo.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.adapter.ChosenAdapter
import uz.mnsh.sayyidsafo.ui.base.ScopedFragment
import uz.mnsh.sayyidsafo.utils.ListenActions
import java.io.File

class ChosenFragment : ScopedFragment(R.layout.fragment_chosen), KodeinAware, ListenActions {

    override val kodein by closestKodein()
    private val viewModelFactory: ChosenViewModelFactory by instance<ChosenViewModelFactory>()
    private lateinit var viewModel: ChosenViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: AppCompatEditText
    private var listAudiosFile: ArrayList<String> = ArrayList()
    private var adapter: ChosenAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_chosen)
        recyclerView.layoutManager = LinearLayoutManager(context)
        editText = view.findViewById(R.id.edit_search)

        viewModel = viewModelFactory.create(ChosenViewModel::class.java)
        loadData()
    }

    private fun loadData() = launch {
        listAudiosFile.clear()
        File(App.DIR_PATH).walkTopDown().forEach { file ->
            if (file.name.endsWith(".mp3")){
                listAudiosFile.add(file.name)
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
        adapter = ChosenAdapter(list, this)
        recyclerView.adapter = adapter

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                adapter!!.searchAudio(editText.text.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
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