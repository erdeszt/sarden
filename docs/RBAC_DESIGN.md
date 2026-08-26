```scala
sealed trait Role
sealed trait Basic extends Role
sealed trait Admin extends Basic

trait UserCtx[+T <: Role]

trait Tx

trait PlantId
trait OrderId

class LivePlantService() extends PlantService {

    def orderPlant(name: String, amount: Int)(using UserCtx[Basic], Tx): OrderId = ???

    def createPlant(name: String)(using UserCtx[Admin], Tx): PlantId = ???

}

def test() = {
    given tx: Tx = new Tx {}
    val s = LivePlantService()

    {
    given role: UserCtx[Role] = new UserCtx {}

        s.orderPlant("carrot", 2) // Error
        s.createPlant("carrot")   // Error
    }

    {
    given role: UserCtx[Basic] = new UserCtx {}

        s.orderPlant("carrot", 2)
        s.createPlant("carrot")   // Error
    }

    {
    given role: UserCtx[Admin] = new UserCtx {}

        s.orderPlant("carrot", 2)
        s.createPlant("carrot")
    }

}
```