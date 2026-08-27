package gls.domain.user

import scala.collection.concurrent.TrieMap

import gls.*

private[user] trait UserRepo {
  def create(user: User): Unit
  def getByEmail(email: Email): Option[User]
  def getById(id: UserId): Option[User]
}

private[user] class InMemoryUserRepo extends UserRepo {

  private val repo = GenericInMemoryRepo[User, UserId](_.id)

  override def create(user: User): Unit = {
    repo.store(user)
  }

  override def getByEmail(email: Email): Option[User] = {
    repo.query(_.email == email).headOption
  }

  override def getById(id: UserId): Option[User] = {
    repo.getById(id)
  }

}

// TODO: Move
class GenericInMemoryRepo[Entity, Id](getId: Entity => Id) {
  private val store = TrieMap.empty[Id, Entity]

  def store(entity: Entity): Unit = {
    val _ = store.put(getId(entity), entity)
    ()
  }

  def getById(id: Id): Option[Entity] = {
    store.get(id)
  }

  def query(matcher: Entity => Boolean): Vector[Entity] = {
    store.filter { (_, entity) => matcher(entity) }.values.toVector
  }

}
