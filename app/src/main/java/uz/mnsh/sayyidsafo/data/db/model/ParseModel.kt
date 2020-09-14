package uz.mnsh.sayyidsafo.data.db.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ParseModel(
    @SerializedName("text")
    val text: String
)