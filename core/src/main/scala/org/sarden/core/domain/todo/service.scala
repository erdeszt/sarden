package org.sarden.core.domain.todo

import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.domain.todo.internal.*
import org.sarden.core.tx.*

trait TodoService:
  def deliverPendingNotifications(): UIO[Unit]
  def createTodo(todo: CreateTodo): UIO[Todo]
  def getActiveTodos(): UIO[Vector[Todo]]
  def deleteTodo(id: TodoId): UIO[Unit]

object TodoService:
  val live: URLayer[Tx.Runner & IdGenerator, TodoService] = ZLayer.fromZIO {
    for
      tx <- ZIO.service[Tx.Runner]
      idGenerator <- ZIO.service[IdGenerator]
    yield LiveTodoService(LiveTodoRepo(idGenerator), tx)
  }

class LiveTodoService(repo: TodoRepo, tx: Tx.Runner) extends TodoService:

  override def deliverPendingNotifications(): UIO[Unit] =
    ???

  override def createTodo(todo: CreateTodo): UIO[Todo] =
    tx.runOrDie(repo.createTodo(todo))

  override def getActiveTodos(): UIO[Vector[Todo]] =
    tx.runOrDie(repo.getActiveTodos())

  override def deleteTodo(id: TodoId): UIO[Unit] =
    tx.runOrDie(repo.deleteTodo(id))
