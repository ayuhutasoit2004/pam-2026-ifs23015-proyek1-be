package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IBookRepository
import java.io.File
import java.util.UUID

class BookService(
    private val bookRepo: IBookRepository
) {
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val genre = call.request.queryParameters["genre"]
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        val (books, total) = bookRepo.getAll(search, genre, page, perPage)
        val totalPages = if (perPage > 0) Math.ceil(total.toDouble() / perPage).toInt() else 1

        call.respond(DataResponse(
            "success",
            "Berhasil mengambil daftar buku",
            BookListResponse(
                books = books,
                meta = BookMeta(total, page, perPage, totalPages)
            )
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val bookId = call.parameters["id"]
            ?: throw AppException(400, "ID buku tidak valid!")

        val book = bookRepo.getById(bookId)
            ?: throw AppException(404, "Buku tidak ditemukan!")

        call.respond(DataResponse("success", "Berhasil mengambil data buku", BookResponse(book)))
    }

    suspend fun post(call: ApplicationCall) {
        val request = call.receive<BookRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul buku tidak boleh kosong")
        validator.required("author", "Penulis tidak boleh kosong")
        validator.required("genre", "Genre tidak boleh kosong")
        validator.validate()

        val bookId = bookRepo.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil menambahkan buku", BookIdResponse(bookId)))
    }

    suspend fun put(call: ApplicationCall) {
        val bookId = call.parameters["id"]
            ?: throw AppException(400, "ID buku tidak valid!")

        val oldBook = bookRepo.getById(bookId)
            ?: throw AppException(404, "Buku tidak ditemukan!")

        val request = call.receive<BookRequest>()
        request.cover = oldBook.cover // pertahankan cover lama

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul buku tidak boleh kosong")
        validator.required("author", "Penulis tidak boleh kosong")
        validator.required("genre", "Genre tidak boleh kosong")
        validator.validate()

        val isUpdated = bookRepo.update(bookId, request.toEntity())
        if (!isUpdated) throw AppException(400, "Gagal memperbarui buku!")

        call.respond(DataResponse("success", "Berhasil mengubah data buku", null))
    }

    suspend fun putCover(call: ApplicationCall) {
        val bookId = call.parameters["id"]
            ?: throw AppException(400, "ID buku tidak valid!")

        val oldBook = bookRepo.getById(bookId)
            ?: throw AppException(404, "Buku tidak ditemukan!")

        var newCoverPath: String? = null

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/books/$fileName"

                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        newCoverPath = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (newCoverPath == null) throw AppException(400, "Cover buku tidak tersedia!")

        val updateBook = oldBook.copy(cover = newCoverPath)
        val isUpdated = bookRepo.update(bookId, updateBook)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui cover buku!")

        // Hapus cover lama
        if (oldBook.cover != null) {
            val oldFile = File(oldBook.cover!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse("success", "Berhasil mengubah cover buku", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val bookId = call.parameters["id"]
            ?: throw AppException(400, "ID buku tidak valid!")

        val oldBook = bookRepo.getById(bookId)
            ?: throw AppException(404, "Buku tidak ditemukan!")

        val isDeleted = bookRepo.delete(bookId)
        if (!isDeleted) throw AppException(400, "Gagal menghapus buku!")

        // Hapus cover jika ada
        if (oldBook.cover != null) {
            val oldFile = File(oldBook.cover!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse("success", "Berhasil menghapus buku", null))
    }

    suspend fun getCover(call: ApplicationCall) {
        val bookId = call.parameters["id"]
            ?: throw AppException(400, "ID buku tidak valid!")

        val book = bookRepo.getById(bookId)
            ?: return call.respond(HttpStatusCode.NotFound)

        if (book.cover == null) throw AppException(404, "Buku belum memiliki cover")

        val file = File(book.cover!!)
        if (!file.exists()) throw AppException(404, "Cover buku tidak tersedia")

        call.respondFile(file)
    }
}
