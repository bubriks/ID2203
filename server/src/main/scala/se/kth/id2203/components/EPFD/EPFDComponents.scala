package se.kth.id2203.components.EPFD


import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port
import se.sics.kompics.timer.{ScheduleTimeout, Timeout}

object EPFDComponents {

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)

  case class Suspect(process: NetAddress) extends KompicsEvent

  case class Restore(process: NetAddress) extends KompicsEvent

  case class HeartbeatReply(seq: Int) extends KompicsEvent

  case class HeartbeatRequest(seq: Int) extends KompicsEvent

  class EventuallyPerfectFailureDetector extends Port {
    indication[Suspect]
    indication[Restore]
    //request[OverlayUpdate]
  }


}