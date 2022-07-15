package com.mambobryan.data.tables.user

import com.mambobryan.utils.Exclude
import com.mambobryan.utils.toDateLong
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object UsersTable : UUIDTable() {

    val userCreatedAt = datetime("user_created_at")
    val userUpdatedAt = datetime("user_updated_at").nullable()
    val userSetupAt = datetime("user_setup_at").nullable()
    val userEmail = varchar("user_email", 50).uniqueIndex()
    val userHash = varchar("user_hash", 128)
    val userName = varchar("user_name", 50).nullable()
    val userImage = varchar("user_image", 50).nullable()
    val userBio = text("user_bio").nullable()
    val userDateOfBirth = date("user_dob").nullable()
    val userGender = integer("user_gender").nullable()
    val userToken = text("user_token").nullable()

}

data class User(
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val setupAt: LocalDateTime?,
    val id: UUID?,
    val email: String,
    @Exclude val hash: String,
    val username: String?,
    val imageUrl: String?,
    val bio: String?,
    val dateOfBirth: LocalDate?,
    val gender: Int?
)

internal fun ResultRow?.toUser(): User? {
    if (this == null) return null
    return User(
        createdAt = this[UsersTable.userCreatedAt],
        updatedAt = this[UsersTable.userUpdatedAt],
        setupAt = this[UsersTable.userSetupAt],
        id = this[UsersTable.id].value,
        email = this[UsersTable.userEmail],
        hash = this[UsersTable.userHash],
        username = this[UsersTable.userName],
        imageUrl = this[UsersTable.userImage],
        bio = this[UsersTable.userBio],
        dateOfBirth = this[UsersTable.userDateOfBirth],
        gender = this[UsersTable.userGender]
    )
}

internal fun Query?.toUserList(): List<User?> {
    if (this == null) return listOf()
    return this.map { it.toUser() }
}