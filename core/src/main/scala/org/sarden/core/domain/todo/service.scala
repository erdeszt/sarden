package org.sarden.core.domain.todo

import org.sarden.core.domain.todo.internal.TodoRepo

trait TodoService:
  def deliverPendingNotifications(): Unit
  def createTodo(todo: CreateTodo): Todo
  def getActiveTodos(): List[Todo]
  def deleteTodo(id: TodoId): Unit

class LiveTodoService(repo: TodoRepo) extends TodoService:

  override def deliverPendingNotifications(): Unit =
    ???

  override def createTodo(todo: CreateTodo): Todo =
    repo.createTodo(todo)

  override def getActiveTodos(): List[Todo] =
    repo.getActiveTodos()

  override def deleteTodo(id: TodoId): Unit =
    repo.deleteTodo(id)
