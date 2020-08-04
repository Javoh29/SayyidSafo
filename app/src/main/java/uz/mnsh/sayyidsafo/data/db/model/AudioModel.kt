package uz.mnsh.sayyidsafo.data.db.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "audios_table")
data class AudioModel(
    @PrimaryKey(autoGenerate = true)
    var idTable: Int = 0,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("topic_id")
    val topic_id: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("duration")
    val duration: Long
)