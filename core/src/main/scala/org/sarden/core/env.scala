package org.sarden.core

import izumi.reflect.macrortti.LightTypeTag
import izumi.reflect.{*, given}

trait Env[+R]:
  def get[A: Tag](using Contains[R, A]): A

trait Contains[-R, A]

trait LowPriorityContains:
  given containsSelf[A: Tag]: Contains[A, A] =
    new Contains[A, A] {}

object Contains extends LowPriorityContains:
  given containsRec[A, R, R0](using
      contains: Contains[R, A]
  ): Contains[R0 & R, A] =
    new Contains[R0 & R, A] {}

case class EnvBuilder[+R](map: Map[LightTypeTag, Any]):
  def add[A](instance: A)(using tag: Tag[A]): EnvBuilder[A & R] =
    EnvBuilder(
      map + (tag.tag -> instance)
    )

  def build: Env[R] =
    new Env[R] {
      override def get[A](using tag: Tag[A], _ev: Contains[R, A]): A =
        map(tag.tag).asInstanceOf[A]
    }

object EnvBuilder:
  def empty: EnvBuilder[Any] = EnvBuilder(Map.empty)
