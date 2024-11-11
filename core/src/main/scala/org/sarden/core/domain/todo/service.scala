package org.sarden.core.domain.todo

trait TodoService:
  def deliverPendingNotifications(): Unit
  def createTodo(todo: CreateTodo): TodoId
  def listTodos(): Vector[Todo]

class LiveTodoService() extends TodoService:

  override def deliverPendingNotifications(): Unit =
    ???

  override def createTodo(todo: CreateTodo): TodoId =
    ???

  override def listTodos(): Vector[Todo] =
    ???
