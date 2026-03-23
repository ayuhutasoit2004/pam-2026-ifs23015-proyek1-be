package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IBookRepository
import org.delcom.repositories.IBorrowingRepository

class BorrowingService(
    private val borrowingRepo: IBorrowingRepository,
    private val bookRepo: IBookRepository
) {
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val status = call.request.queryParameters["status"]
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        val (borrowings, total) = borrowingRepo.getAll(search, status, page, perPage)
        val totalPages = if (perPage > 0) Math.ceil(total.toDouble() / perPage).toInt() else 1

        call.respond(DataResponse(
            "success",
            "Berhasil mengambil daftar peminjaman",
            BorrowingListResponse(
                borrowings = borrowings,
                meta = BorrowingMeta(total, page, perPage, totalPages)
            )
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val borrowingId = call.parameters["id"]
            ?: throw AppException(400, "ID peminjaman tidak valid!")

        val borrowing = borrowingRepo.getById(borrowingId)
            ?: throw AppException(404, "Data peminjaman tidak ditemukan!")

        call.respond(DataResponse("success", "Berhasil mengambil data peminjaman", BorrowingResponse(borrowing)))
    }

    suspend fun post(call: ApplicationCall) {
        val request = call.receive<BorrowingRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("bookId", "ID buku tidak boleh kosong")
        validator.required("borrowerName", "Nama peminjam tidak boleh kosong")
        validator.validate()

        // Cek buku ada dan stok tersedia
        val book = bookRepo.getById(request.bookId)
            ?: throw AppException(404, "Buku tidak ditemukan!")

        if (book.stock <= 0) {
            throw AppException(400, "Stok buku habis, tidak dapat dipinjam!")
        }

        // Kurangi stok buku
        bookRepo.updateStock(request.bookId, -1)

        // Simpan peminjaman
        val borrowingId = borrowingRepo.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil mencatat peminjaman", BorrowingIdResponse(borrowingId)))
    }

    suspend fun put(call: ApplicationCall) {
        val borrowingId = call.parameters["id"]
            ?: throw AppException(400, "ID peminjaman tidak valid!")

        val oldBorrowing = borrowingRepo.getById(borrowingId)
            ?: throw AppException(404, "Data peminjaman tidak ditemukan!")

        val request = call.receive<BorrowingRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("borrowerName", "Nama peminjam tidak boleh kosong")
        validator.required("status", "Status tidak boleh kosong")
        validator.validate()

        // Business logic: jika status berubah menjadi "returned", tambah stok
        if (oldBorrowing.status == "borrowed" && request.status == "returned") {
            bookRepo.updateStock(oldBorrowing.bookId, +1)
        }

        val updatedBorrowing = oldBorrowing.copy(
            borrowerName = request.borrowerName,
            status = request.status,
            returnDate = if (request.status == "returned") Clock.System.now() else oldBorrowing.returnDate,
            updatedAt = Clock.System.now()
        )

        val isUpdated = borrowingRepo.update(borrowingId, updatedBorrowing)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui data peminjaman!")

        call.respond(DataResponse("success", "Berhasil mengubah data peminjaman", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val borrowingId = call.parameters["id"]
            ?: throw AppException(400, "ID peminjaman tidak valid!")

        val oldBorrowing = borrowingRepo.getById(borrowingId)
            ?: throw AppException(404, "Data peminjaman tidak ditemukan!")

        // Jika masih dipinjam, kembalikan stok dulu
        if (oldBorrowing.status == "borrowed") {
            bookRepo.updateStock(oldBorrowing.bookId, +1)
        }

        val isDeleted = borrowingRepo.delete(borrowingId)
        if (!isDeleted) throw AppException(400, "Gagal menghapus data peminjaman!")

        call.respond(DataResponse("success", "Berhasil menghapus data peminjaman", null))
    }

    // Summary untuk Home Screen
    suspend fun getSummary(call: ApplicationCall) {
        val (allBorrowings, totalBorrowing) = borrowingRepo.getAll("", null, 1, 1)
        val (borrowed, totalBorrowed) = borrowingRepo.getAll("", "borrowed", 1, 1)
        val (returned, totalReturned) = borrowingRepo.getAll("", "returned", 1, 1)

        call.respond(DataResponse(
            "success",
            "Berhasil mengambil summary peminjaman",
            mapOf(
                "totalBorrowing" to totalBorrowing,
                "totalBorrowed" to totalBorrowed,
                "totalReturned" to totalReturned,
            )
        ))
    }
}
