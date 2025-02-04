package org.sarden.core.domain.todo

import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.domain.todo.internal.*

trait TodoService:
  def deliverPendingNotifications(): UIO[Unit]
  def createTodo(todo: CreateTodo): UIO[Todo]
  def getActiveTodos(): UIO[Vector[Todo]]
  def deleteTodo(id: TodoId): UIO[Unit]

object TodoService:
  val live: URLayer[Database & IdGenerator, TodoService] = ZLayer.fromZIO {
    for
      db <- ZIO.service[Database]
      idGenerator <- ZIO.service[IdGenerator]
    yield LiveTodoService(LiveTodoRepo(idGenerator), db)
  }

class LiveTodoService(repo: TodoRepo, db: Database) extends TodoService:

  override def deliverPendingNotifications(): UIO[Unit] =
    ???

  override def createTodo(todo: CreateTodo): UIO[Todo] =
    db.transactionOrDie(repo.createTodo(todo))

  override def getActiveTodos(): UIO[Vector[Todo]] =
    db.transactionOrDie(repo.getActiveTodos())

  override def deleteTodo(id: TodoId): UIO[Unit] =
    db.transactionOrDie(repo.deleteTodo(id))
