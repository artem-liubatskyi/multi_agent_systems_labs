package agents

import agents.Environment.{ActionResponse, EnvironmentRequest, EnvironmentResponse, PerformAction, Request}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.{Labyrinth, LabyrinthCellDescriptor, SpeleologistAction}

class Environment {
  private val Labyrinth = new Labyrinth
  def behavior: Behavior[Request] = Behaviors.receive((_, message) => {
    message match {
      case EnvironmentRequest(sender) =>
        sender ! EnvironmentResponse(Labyrinth.getCurrentCellState())
        Behaviors.same

      case PerformAction(action, sender) =>
        performAction(action)
        sender ! ActionResponse("ACCEPTED")
        Behaviors.same
    }
  })

  def performAction(action: SpeleologistAction): Unit = {
    Labyrinth.performAction(action)
  }
}

object Environment {
  sealed trait Request
  sealed trait Response

  case class EnvironmentRequest(sender: ActorRef[Response]) extends Request
  case class EnvironmentResponse(state: LabyrinthCellDescriptor) extends Response
  case class PerformAction(action: SpeleologistAction, sender: ActorRef[Response]) extends Request
  case class ActionResponse(response: String) extends Response
}

