package uz.mnsh.sayyidsafo.data.network.response

import uz.mnsh.sayyidsafo.data.db.model.AudioModel
import uz.mnsh.sayyidsafo.data.db.model.Links
import uz.mnsh.sayyidsafo.data.db.model.MetaData


data class AudioResponse (
    val items: List<AudioModel>,
    val _links: Links,
    val _meta: MetaData
)