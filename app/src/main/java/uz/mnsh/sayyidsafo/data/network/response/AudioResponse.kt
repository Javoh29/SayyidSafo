package uz.mnsh.sayyidsafo.data.network.response

import com.google.gson.annotations.SerializedName
import uz.mnsh.sayyidsafo.data.db.model.AudioModel


data class AudioResponse (
    @SerializedName("data")
    val data: List<AudioModel>
)