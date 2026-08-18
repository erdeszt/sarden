package gls

import com.github.f4b6a3.ulid.{Ulid, UlidCreator}

trait IdGenerator[Id] {
  
  def generate(): Id

}

class UlidWrapperIdGenerator[Id](wrap: Ulid => Id) extends IdGenerator[Id] {

  override def generate(): Id = {
    wrap(UlidCreator.getUlid())
  }
  
}

object UlidIdGenerator extends UlidWrapperIdGenerator(identity)
