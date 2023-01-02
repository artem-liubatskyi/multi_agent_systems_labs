package models

trait SpeleologistAction

case object GrabGold extends SpeleologistAction
case object Shoot extends SpeleologistAction
case object Move extends SpeleologistAction
case object TurnLeft extends SpeleologistAction
case object TurnRight extends SpeleologistAction
