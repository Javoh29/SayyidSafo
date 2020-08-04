package uz.mnsh.sayyidsafo.utils

import android.graphics.Path
import androidx.annotation.Px

abstract class MaskPath {
    abstract fun rebuildPath(@Px containerWidthPx: Int, @Px containerHeightPx: Int): Path
    abstract fun getPath(): Path
}