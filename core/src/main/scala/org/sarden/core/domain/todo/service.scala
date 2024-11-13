package org.sarden.core.domain.todo

import org.sarden.core.domain.todo.internal.TodoRepo

trait TodoService:
  def deliverPendingNotifications(): Unit
  def createTodo(todo: CreateTodo): Todo
  def getActiveTodos(): Vector[Todo]

class LiveTodoService(repo: TodoRepo) extends TodoService:

  override def deliverPendingNotifications(): Unit =
    ???

  override def createTodo(todo: CreateTodo): Todo =
    repo.createTodo(todo)

  override def getActiveTodos(): Vector[Todo] =
    repo.getActiveTodos()
