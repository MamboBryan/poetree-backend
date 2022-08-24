package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.TopicRequest
import com.mambobryan.data.tables.topic.*
import com.mambobryan.data.tables.topic.toTopic
import com.mambobryan.data.tables.topic.toTopicList
import com.mambobryan.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

class TopicsRepository {

    suspend fun create(request: TopicRequest): ServerResponse<out Any?> {

        if (request.name.isNullOrBlank()) return defaultBadRequestResponse(message = "invalid name")
        if (request.color.isNullOrBlank()) return defaultBadRequestResponse(message = "invalid color")
        if (!request.color.isValidHexColor()) return defaultBadRequestResponse(message = "invalid color")

        return try {

            val condition =
                Op.build { TopicsTable.name like "%${request.name}%" or (TopicsTable.color like "%${request.color}%") }
            val exists = query { TopicsTable.select(condition).empty().not() }

            if (exists) return defaultBadRequestResponse(message = "topic name or color already exists")

            var statement: InsertStatement<Number>? = null

            query {
                statement = TopicsTable.insert {
                    it[TopicsTable.createdAt] = LocalDateTime.now()
                    it[TopicsTable.name] = request.name
                    it[TopicsTable.color] = request.color
                }
            }

            val data = statement?.resultedValues?.get(0).toTopic().toTopicDto()

            return defaultCreatedResponse(message = "topic created successfully", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun get(topicId: Int) = query {

        return@query try {
            val result =
                TopicsTable.select { TopicsTable.id eq topicId }.firstOrNull() ?: return@query defaultNotFoundResponse(
                    message = "Topic not found"
                )

            val data = result.toTopic().toTopicDto()
            defaultOkResponse(message = "topic", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getTopics(page: Int) = query {

        val (limit, offset) = getLimitAndOffset(page)

        return@query try {

            val list =
                TopicsTable.selectAll().limit(n = limit, offset = offset).sortedByDescending { TopicsTable.updatedAt }
                    .map { it.toTopic().toTopicDto() }

            val data = getPagedData(page = page, result = list)

            defaultOkResponse(message = "topics", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun update(topicId: Int, request: TopicRequest) = query {

        return@query try {

            if (request.name.isNullOrBlank() and request.color.isNullOrBlank()) return@query defaultBadRequestResponse("topic name and color cannot be blank")

            val topicExists = TopicsTable.select(TopicsTable.id eq topicId).empty().not()
            if (!topicExists) return@query defaultNotFoundResponse(message = "topic not found")

            val existsCondition =
                Op.build { TopicsTable.name like "%${request.name}%" or (TopicsTable.color like "%${request.color}%") }
            val exists = TopicsTable.select(existsCondition).empty().not()

            if (exists) return@query defaultBadRequestResponse(message = "topic name or color already exists")

            val condition = Op.build { TopicsTable.id eq topicId }

            TopicsTable.update({ condition }) {
                it[TopicsTable.updatedAt] = LocalDateTime.now()
                if (!request.name.isNullOrBlank()) it[TopicsTable.name] = request.name
                if (!request.color.isNullOrBlank() && request.color.isValidHexColor()) it[TopicsTable.color] =
                    request.color
            }

            val topic = TopicsTable.select(TopicsTable.id eq topicId).map { it.toTopic().toTopicDto() }.singleOrNull()
            defaultOkResponse(message = "topic updated successfully", data = topic)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun delete(topicId: Int) = query {
        return@query try {

            val topicExists = TopicsTable.select(TopicsTable.id eq topicId).empty().not()
            if (!topicExists) return@query defaultNotFoundResponse(message = "topic not found")

            val result = TopicsTable.deleteWhere { TopicsTable.id eq topicId }
            defaultOkResponse(message = "topic deleted successfully", data = result != 0)
        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }
    }

}