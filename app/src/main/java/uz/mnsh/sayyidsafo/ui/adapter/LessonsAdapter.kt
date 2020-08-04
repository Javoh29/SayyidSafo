package uz.mnsh.sayyidsafo.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.data.model.LessonsModel
import uz.mnsh.sayyidsafo.ui.fragment.HomeFragmentDirections

class LessonsAdapter(private val list: List<LessonsModel>): RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    class LessonsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val imageView: AppCompatImageView = view.findViewById(R.id.img_lesson)
        val tvName: AppCompatTextView = view.findViewById(R.id.tv_lesson_name)
        val tvSum: AppCompatTextView = view.findViewById(R.id.tv_lesson_sum)
        val mView = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsViewHolder {
        return LessonsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_lessons_container,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LessonsViewHolder, position: Int) {
        holder.imageView.setImageResource(list[position].image)
        holder.tvName.setText(list[position].name)
        holder.tvSum.text = list[position].sum.toString() + holder.mView.resources.getString(R.string.text_sum_lesson)

        holder.imageView.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeFragmentDirections.actionHomeFragmentToListenFragment().setIndex(position+1).setSum(list[position].sum))
        }
    }
}