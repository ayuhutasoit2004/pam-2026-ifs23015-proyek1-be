package org.delcom.dao

import org.delcom.tables.BorrowingTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class BorrowingDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, BorrowingDAO>(BorrowingTable)

    var bookId by BorrowingTable.bookId
    var borrowerName by BorrowingTable.borrowerName
    var borrowDate by BorrowingTable.borrowDate
    var returnDate by BorrowingTable.returnDate
    var status by BorrowingTable.status
    var createdAt by BorrowingTable.createdAt
    var updatedAt by BorrowingTable.updatedAt
}
