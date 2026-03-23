package org.delcom.repositories

import org.delcom.entities.Borrowing

interface IBorrowingRepository {
    suspend fun getAll(
        search: String,
        status: String?,
        page: Int,
        perPage: Int
    ): Pair<List<Borrowing>, Long>

    suspend fun getById(borrowingId: String): Borrowing?
    suspend fun create(borrowing: Borrowing): String
    suspend fun update(borrowingId: String, newBorrowing: Borrowing): Boolean
    suspend fun delete(borrowingId: String): Boolean
}
