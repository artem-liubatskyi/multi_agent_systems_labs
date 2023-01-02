package agents

import agents.Navigator.ActionRequest
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.{LabyrinthCellDescriptor, Move, SpeleologistAction}

class Navigator {
  def actionRequestBehavior: Behavior[ActionRequest] = Behaviors.receive((_, message) => {
    message.sender ! Navigator.ActionResponse(defineAction(message.state))
    Behaviors.same
  })

  private def defineAction(state: LabyrinthCellDescriptor): SpeleologistAction = Move
}

object Navigator {
  case class ActionRequest(state: LabyrinthCellDescriptor, sender: ActorRef[ActionResponse])

  case class ActionResponse(action: SpeleologistAction)
}
