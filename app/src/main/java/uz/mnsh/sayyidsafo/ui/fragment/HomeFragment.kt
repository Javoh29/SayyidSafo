package uz.mnsh.sayyidsafo.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.db.model.ParseModel
import uz.mnsh.sayyidsafo.data.model.LessonsModel
import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import uz.mnsh.sayyidsafo.ui.activity.MainActivity
import uz.mnsh.sayyidsafo.ui.adapter.LessonsAdapter
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class HomeFragment : Fragment(R.layout.fragment_home), KodeinAware {

    override val kodein by closestKodein()
    private val unitProvider: UnitProvider by  instance()

    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bindUI()
    }

    private fun bindUI(){
        val listModel = ArrayList<LessonsModel>()
        listModel.add(LessonsModel(image = R.drawable.img_dars1, name = R.string.text_dars1, sum = unitProvider.getSavedSum(2)))
        listModel.add(LessonsModel(image = R.drawable.img_dars2, name = R.string.text_dars2, sum = unitProvider.getSavedSum(3)))
        listModel.add(LessonsModel(image = R.drawable.img_dars3, name = R.string.text_dars3, sum = unitProvider.getSavedSum(4)))
        listModel.add(LessonsModel(image = R.drawable.img_dars4, name = R.string.text_dars4, sum = unitProvider.getSavedSum(5)))
        listModel.add(LessonsModel(image = R.drawable.img_dars5, name = R.string.text_dars5, sum = unitProvider.getSavedSum(6)))
        listModel.add(LessonsModel(image = R.drawable.img_dars6, name = R.string.text_dars6, sum = unitProvider.getSavedSum(7)))
        listModel.add(LessonsModel(image = R.drawable.img_dars7, name = R.string.text_dars7, sum = unitProvider.getSavedSum(8)))
        listModel.add(LessonsModel(image = R.drawable.img_dars8, name = R.string.text_dars8, sum = unitProvider.getSavedSum(9)))
        recyclerView.adapter = LessonsAdapter(listModel)

        img_menu.setOnClickListener {
            (activity as MainActivity).drawerLayout!!.openDrawer(GravityCompat.START)
        }

        var parseText = ""
        try {
            val input: InputStream? = requireContext().assets?.open("parse.json")
            val size: Int? = input?.available()
            val buffer = ByteArray(size!!)
            input.read(buffer)
            input.close()
            parseText = String(buffer, charset("UTF-8"))
        }catch (e: IOException){
        }

        val parseList: List<ParseModel> = Gson().fromJson(parseText, object : TypeToken<List<ParseModel>>() {}.type)

        tvParse.text = parseList[Random.nextInt(0, parseList.size)].text
    }
}