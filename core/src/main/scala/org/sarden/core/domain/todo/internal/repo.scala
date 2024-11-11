package org.sarden.core.domain.todo.internal

import org.sarden.core.domain.todo.*

private[todo] trait TodoRepo:
  def getActiveTodos(): Vector[Todo]
