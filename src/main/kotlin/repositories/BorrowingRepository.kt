package org.delcom.repositories

import org.delcom.dao.BookDAO
import org.delcom.dao.BorrowingDAO
import org.delcom.entities.Borrowing
import org.delcom.helpers.borrowingDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.BookTable
import org.delcom.tables.BorrowingTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class BorrowingRepository : IBorrowingRepository {

    override suspend fun getAll(
        search: String,
        status: String?,
        page: Int,
        perPage: Int
    ): Pair<List<Borrowing>, Long> = suspendTransaction {
        var condition: Op<Boolean> = Op.TRUE

        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            condition = condition and (BorrowingTable.borrowerName.lowerCase() like keyword)
        }

        if (!status.isNullOrBlank()) {
            condition = condition and (BorrowingTable.status eq status)
        }

        val total = BorrowingDAO.find(condition).count()
        val offset = ((page - 1) * perPage).toLong()

        val borrowings = BorrowingDAO
            .find(condition)
            .orderBy(BorrowingTable.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(perPage)
            .map { dao ->
                val book = BookDAO
                    .find { BookTable.id eq dao.bookId }
                    .limit(1)
                    .firstOrNull()
                borrowingDAOToModel(dao, book?.title ?: "", book?.author ?: "")
            }

        Pair(borrowings, total)
    }

    override suspend fun getById(borrowingId: String): Borrowing? = suspendTransaction {
        val dao = BorrowingDAO
            .find { BorrowingTable.id eq UUID.fromString(borrowingId) }
            .limit(1)
            .firstOrNull() ?: return@suspendTransaction null

        val book = BookDAO
            .find { BookTable.id eq dao.bookId }
            .limit(1)
            .firstOrNull()

        borrowingDAOToModel(dao, book?.title ?: "", book?.author ?: "")
    }

    override suspend fun create(borrowing: Borrowing): String = suspendTransaction {
        val borrowingDAO = BorrowingDAO.new {
            bookId = UUID.fromString(borrowing.bookId)
            borrowerName = borrowing.borrowerName
            borrowDate = borrowing.borrowDate
            returnDate = borrowing.returnDate
            status = borrowing.status
            createdAt = borrowing.createdAt
            updatedAt = borrowing.updatedAt
        }
        borrowingDAO.id.value.toString()
    }

    override suspend fun update(borrowingId: String, newBorrowing: Borrowing): Boolean = suspendTransaction {
        val dao = BorrowingDAO
            .find { BorrowingTable.id eq UUID.fromString(borrowingId) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.borrowerName = newBorrowing.borrowerName
            dao.status = newBorrowing.status
            dao.returnDate = newBorrowing.returnDate
            dao.updatedAt = newBorrowing.updatedAt
            true
        } else false
    }

    override suspend fun delete(borrowingId: String): Boolean = suspendTransaction {
        val rowsDeleted = BorrowingTable.deleteWhere {
            BorrowingTable.id eq UUID.fromString(borrowingId)
        }
        rowsDeleted >= 1
    }
}
