package uz.mnsh.sayyidsafo.data.db.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "audios_table")
data class AudioModel(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("topic_id")
    val topic_id: Int,
    @SerializedName("size")
    val size: String,
    @SerializedName("rn")
    val rn: Int,
    @SerializedName("duration")
    val duration: String
)