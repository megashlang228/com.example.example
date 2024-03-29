package com.example.database.model

import com.example.database.DatabaseFactory.dbQuery
import com.example.model.Category
import com.example.model.Comment
import com.example.model.CommentPreview
import com.example.model.UserInfo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object PostsComments: Table("posts_comments") {
    val id = long("id").autoIncrement()
    val postId = long("post_id")
    val userId = long("user_id")
    val text = text("text")
    val date = timestamp("date")

    suspend fun createComment(comment: Comment): Comment? = dbQuery{
        val inserted = PostsComments.insert {
            it[postId] = comment.postId
            it[userId] = comment.userId
            it[text] = comment.text
            it[date] = Instant.ofEpochSecond(comment.date)
        }
        inserted.resultedValues?.singleOrNull()?.let { resultRowToComment(it) }
    }

    suspend fun getCommentsByPostId(id: Long): List<CommentPreview> = dbQuery {
        return@dbQuery Join(
            PostsComments, Posts,
            onColumn = postId, otherColumn = Posts.id,
            joinType = JoinType.INNER,
            additionalConstraint = { postId eq id})
            .join(Users, onColumn = userId, otherColumn = Users.id, joinType = JoinType.INNER,
                additionalConstraint = { userId eq Users.id})
            .join(
                Categories, JoinType.INNER,
                onColumn = Users.categoryId, otherColumn = Categories.id)
            .selectAll().orderBy(date to SortOrder.ASC).mapNotNull { resultRowToCommentPreview(it) }
    }

    private fun resultRowToComment(row: ResultRow) = Comment(
        id = row[id],
        userId = row[userId],
        postId = row[postId],
        date = row[date].toEpochMilli(),
        text = row[text]
        )

    private fun resultRowToCommentPreview(row: ResultRow) = CommentPreview(
        id = row[id],
        postId = row[postId],
        date = row[date].toEpochMilli(),
        text = row[text],
        user = UserInfo(
            id = row[userId],
            name = row[Users.name],
            surname = row[Users.surname],
            icon = row[Users.icon],
            doctorStatus = row[Users.doctorStatus],
            dateOfBirthday = row[Users.dateOfBirthday].toEpochMilli(),
            category = Category(
                id = row[Categories.id],
                name = row[Categories.name]
            )
        )
    )
}