package org.sarden.core.domain.todo

import zio.*

import org.sarden.core.domain.todo.internal.TodoRepo

trait TodoService:
  def deliverPendingNotifications(): UIO[Unit]
  def createTodo(todo: CreateTodo): UIO[Todo]
  def getActiveTodos(): UIO[Vector[Todo]]
  def deleteTodo(id: TodoId): UIO[Unit]

class LiveTodoService(repo: TodoRepo) extends TodoService:

  override def deliverPendingNotifications(): UIO[Unit] =
    ???

  override def createTodo(todo: CreateTodo): UIO[Todo] =
    repo.createTodo(todo)

  override def getActiveTodos(): UIO[Vector[Todo]] =
    repo.getActiveTodos()

  override def deleteTodo(id: TodoId): UIO[Unit] =
    repo.deleteTodo(id)
