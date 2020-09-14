package uz.mnsh.sayyidsafo.data.db.unitchosen

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UnitAudioModel(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "location")
    val location: String,
    @ColumnInfo(name = "topic_id")
    val topic_id: Int,
    @ColumnInfo(name = "size")
    val size: String,
    @ColumnInfo(name = "rn")
    val rn: Int,
    @ColumnInfo(name = "duration")
    val duration: String
): Parcelable {
    fun getFileName(): String {
        return "$name.mp3"
    }
}