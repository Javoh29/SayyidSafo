package uz.mnsh.sayyidsafo.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import kotlinx.android.synthetic.main.fragment_listen.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.App
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.model.ChosenModel
import uz.mnsh.sayyidsafo.data.db.unitchosen.UnitAudioModel
import uz.mnsh.sayyidsafo.ui.activity.MainActivity.Companion.playerAdapter
import uz.mnsh.sayyidsafo.ui.adapter.AudiosAdapter
import uz.mnsh.sayyidsafo.ui.base.ScopedFragment
import uz.mnsh.sayyidsafo.utils.ListenActions
import java.io.File

class ListenFragment : ScopedFragment(R.layout.fragment_listen), KodeinAware, ListenActions {

    override val kodein by closestKodein()
    private val viewModelFactory: ListenViewModelFactory by instance<ListenViewModelFactory>()
    private lateinit var viewModel: ListenViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvLessonsSum: AppCompatTextView
    private lateinit var spinKitView: SpinKitView
    private lateinit var editText: AppCompatEditText
    private var listAudiosFile: ArrayList<String> = ArrayList()
    private var adapter: AudiosAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_lessons)
        recyclerView.layoutManager = LinearLayoutManager(context)
        tvLessonsSum = view.findViewById(R.id.tv_lesson_sum)
        spinKitView = view.findViewById(R.id.spin_kit)
        editText = view.findViewById(R.id.edit_search)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelFactory.create(ListenViewModel::class.java)
        loadData(arguments?.let {ListenFragmentArgs.fromBundle(it)}!!.index, arguments?.let {ListenFragmentArgs.fromBundle(it)}!!.sum)
    }

    private fun loadData(index: Int, sum: Int) = launch {
        listAudiosFile.clear()
        File(App.DIR_PATH + "${index}/").walkTopDown().forEach { file ->
            if (file.name.endsWith(".mp3")){
                listAudiosFile.add(file.name)
            }
        }

        viewModel.getAudios(index).value.await().observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it.size == sum){
                bindUI(it)
            }else return@Observer
        })
    }

    @SuppressLint("SetTextI18n")
    private fun bindUI(list: List<UnitAudioModel>) = launch{
        viewModel.getChosen().value.await().observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            adapter = AudiosAdapter(list, it,this@ListenFragment)
            recyclerView.adapter = adapter
            tvLessonsSum.text = "${getText(R.string.text_lesson_sum)} ${list.size}"
            recyclerView.visibility = View.VISIBLE
            spinKitView.visibility = View.GONE
            spinKitView.clearAnimation()
        })

        editText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                adapter!!.searchAudio(editText.text.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        img_back.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        when(list[0].topic_id.toInt()){
            1 ->{
                tv_lesson_title.setText(R.string.text_dars1)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_1)
            }
            2 ->{
                tv_lesson_title.setText(R.string.text_dars2)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_2)
            }
            3 ->{
                tv_lesson_title.setText(R.string.text_dars3)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_3)
            }
            4 ->{
                tv_lesson_title.setText(R.string.text_dars4)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_1)
            }
            5 ->{
                tv_lesson_title.setText(R.string.text_dars5)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_1)
            }
            6 ->{
                tv_lesson_title.setText(R.string.text_dars6)
                tv_lesson_subtitle.setText(R.string.text_dars_sub_1)
            }
        }
    }

    override val listAudios: ArrayList<String>
        get() = listAudiosFile

    override fun deleteChosen(id: Int) {
        viewModel.deleteChosen(id)
    }

    override fun playPause() {
        playerAdapter!!.resumeOrPause()
    }

    override fun isPause(): Boolean {
        return playerAdapter!!.isPlaying()
    }

    override fun addChosen(unitAudioModel: UnitAudioModel) {
        val model = ChosenModel(
            id = unitAudioModel.id,
            name = unitAudioModel.name,
            duration = unitAudioModel.duration,
            location = unitAudioModel.location,
            size = unitAudioModel.size,
            topic_id = unitAudioModel.topic_id
        )
        viewModel.setChosen(model)
    }

    override fun onResume() {
        super.onResume()
        if (playerAdapter != null){
            playerAdapter!!.getCurrentTitle().observeForever {
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
                                    if (playerAdapter!!.isPlaying()){
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