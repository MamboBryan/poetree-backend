package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.TopicRequest
import com.mambobryan.data.tables.topic.TopicsTable
import com.mambobryan.data.tables.topic.toTopic
import com.mambobryan.data.tables.topic.toTopicList
import com.mambobryan.utils.defaultBadRequestResponse
import com.mambobryan.utils.defaultOkResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime

class TopicsRepository {

    suspend fun create(request: TopicRequest): ServerResponse<Any?> {

        if (request.name.isNullOrBlank()) return defaultBadRequestResponse("invalid name")
        if (request.color.isNullOrBlank()) return defaultBadRequestResponse("invalid color")
        if (request.color.length < 7 || request.color.first().toString() != "#") return defaultBadRequestResponse(
            "invalid color"
        )

        var statement: InsertStatement<Number>? = null

        query {
            statement = TopicsTable.insert {
                it[TopicsTable.createdAt] = LocalDateTime.now()
                it[TopicsTable.name] = request.name
                it[TopicsTable.color] = request.color
            }
        }

        return defaultOkResponse(
            message = "topic created", data = statement?.resultedValues?.get(0).toTopic()
        )

    }

    suspend fun get(topicId: Int) = query {

        val topic = TopicsTable.select { TopicsTable.id eq topicId }.toTopicList().firstOrNull()
        defaultOkResponse(message = "topics", data = topic)

    }

    suspend fun getTopics() = query {
        val list = TopicsTable.selectAll().sortedByDescending { TopicsTable.updatedAt }.map { it.toTopic() }
        defaultOkResponse(message = "topics", data = list)
    }

    suspend fun update(topicId: Int, request: TopicRequest) = query {

        if (request.name.isNullOrBlank() and request.color.isNullOrBlank()) return@query defaultBadRequestResponse("Invalid name or color")

        val condition = Op.build { TopicsTable.id eq topicId }

        TopicsTable.update({ condition }) {
            it[TopicsTable.updatedAt] = LocalDateTime.now()
            if (!request.name.isNullOrBlank()) it[TopicsTable.name] = request.name
            if (!request.color.isNullOrBlank() && request.color.first().toString() != "#") it[TopicsTable.color] =
                request.color
        }

        val topic = TopicsTable.select(TopicsTable.id eq topicId).map { it.toTopic() }.singleOrNull()
        defaultOkResponse(message = "topic updated", data = topic)
    }

    suspend fun delete(topicId: Int) = query {
        val result = TopicsTable.deleteWhere { TopicsTable.id eq topicId }
        defaultOkResponse(message = "topics", data = result != 0)
    }

}