import agents.{Environment, Navigator, Speleologist}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Main {
  def Main(args: Array[String]): Unit = {
    val environment = new Environment
    val navigator = new Navigator
    val speleologist = new Speleologist


    ActorSystem(Behaviors.setup[Any](context => {
      val envRef = context.spawn(environment.behavior, "environment")
      val navRef = context.spawn(navigator.actionRequestBehavior, "navigator")
      val envBehaviorRef = context.spawn(speleologist.environmentBehavior, "environment-behavior")
      val navBehaviorRef = context.spawn(speleologist.navigatorBehavior, "navigator-behavior")

      speleologist.setup(navRef, envRef, navBehaviorRef, envBehaviorRef)

      Behaviors.same
    }), "system")
  }
}
