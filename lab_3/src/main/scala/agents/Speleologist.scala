package agents

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

class Speleologist {
  private var navRef: ActorRef[Navigator.ActionRequest] = _
  private var envRef: ActorRef[Environment.Request] = _

  private var environmentBehaviorRef: ActorRef[Environment.Response] = _
  private var navigatorBehaviorRef: ActorRef[Navigator.ActionResponse] = _

  def setup(navRef: ActorRef[Navigator.ActionRequest], envRef: ActorRef[Environment.Request], navigatorBehaviorRef:  ActorRef[Navigator.ActionResponse], environmentBehaviorRef: ActorRef[Environment.Response]): Unit = {
    this.navRef = navRef
    this.envRef = envRef

    this.environmentBehaviorRef = environmentBehaviorRef
    this.navigatorBehaviorRef = navigatorBehaviorRef

    envRef ! Environment.EnvironmentRequest(environmentBehaviorRef)
  }

  def environmentBehavior: Behavior[Environment.Response] = Behaviors.receive[Environment.Response]((_, message) => {
    message match {
      case Environment.EnvironmentResponse(state) =>
        navRef ! Navigator.ActionRequest(state, navigatorBehaviorRef)
        Behaviors.same

      case Environment.ActionResponse(_) =>
        envRef ! Environment.EnvironmentRequest(environmentBehaviorRef)
        Behaviors.same
    }
  })

  def navigatorBehavior: Behavior[Navigator.ActionResponse] = Behaviors.receive[Navigator.ActionResponse]((_, message) => {
    envRef ! Environment.PerformAction(message.action, environmentBehaviorRef)

    Behaviors.same
  })
}


