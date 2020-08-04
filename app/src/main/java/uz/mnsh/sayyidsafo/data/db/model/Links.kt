package uz.mnsh.sayyidsafo.data.db.model

data class Links (
    val self: Link?,
    val next: Link?,
    val last: Link?
)
data class Link(
    val href: String
)
