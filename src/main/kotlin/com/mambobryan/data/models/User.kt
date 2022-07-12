package com.mambobryan.data.models

import com.mambobryan.utils.Exclude
import com.mambobryan.utils.toDateLong
import com.mambobryan.utils.toDateAndTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class User(
    val createdAt: String?,
    val updatedAt: String?,
    val setupAt: String?,
    val id: Int,
    val email: String,
    @Exclude val hash: String,
    val username: String?,
    val imageUrl: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: Int?
)

object Users : Table() {

    val userCreatedAt = long("user_created_at")
    val userUpdatedAt = long("user_updated_at").nullable()
    val userSetupAt = long("user_setup_at").nullable()
    val userId = integer("user_id").autoIncrement().uniqueIndex()
    val userEmail = varchar("user_email", 50).uniqueIndex()
    val userHash = varchar("user_hash", 128)
    val userName = varchar("user_name", 50).nullable()
    val userImage = varchar("user_image", 50).nullable()
    val userBio = text("user_bio").nullable()
    val userDateOfBirth = long("user_dob").nullable()
    val userGender = integer("user_gender").nullable()
    val userToken = text("user_token").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(userEmail)

}

internal fun ResultRow?.toUser(): User? {
    if (this == null) return null
    return User(
        createdAt = this[Users.userCreatedAt].toDateAndTime(),
        updatedAt = this[Users.userUpdatedAt].toDateAndTime(),
        setupAt  = this[Users.userSetupAt].toDateAndTime(),
        id = this[Users.userId],
        email = this[Users.userEmail],
        hash = this[Users.userHash],
        username = this[Users.userName],
        imageUrl = this[Users.userImage],
        bio = this[Users.userBio],
        dateOfBirth = this[Users.userDateOfBirth].toDateLong(),
        gender = this[Users.userGender]
    )
}
