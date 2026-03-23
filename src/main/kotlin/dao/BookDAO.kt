package org.delcom.dao

import org.delcom.tables.BookTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class  BookDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, BookDAO>(BookTable)

    var title by BookTable.title
    var author by BookTable.author
    var genre by BookTable.genre
    var stock by BookTable.stock
    var cover by BookTable.cover
    var createdAt by BookTable.createdAt
    var updatedAt by BookTable.updatedAt
}
