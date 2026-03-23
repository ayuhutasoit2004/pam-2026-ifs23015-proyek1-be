package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BorrowingTable : UUIDTable("borrowings") {
    val bookId = uuid("book_id").references(BookTable.id)
    val borrowerName = varchar("borrower_name", 100)
    val borrowDate = timestamp("borrow_date")
    val returnDate = timestamp("return_date").nullable()
    val status = varchar("status", 20).default("borrowed") // borrowed, returned
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
