package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookTable : UUIDTable("books") {
    val title = varchar("title", 200)
    val author = varchar("author", 100)
    val genre = varchar("genre", 50)
    val stock = integer("stock").default(0)
    val cover = text("cover").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
