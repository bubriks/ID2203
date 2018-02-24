package se.kth.id2203.components.EventuallyPerfectFailureDetector

import se.sics.kompics.network._
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.timer.{ScheduleTimeout, Timer}
import se.sics.kompics.Start
import se.kth.id2203.components.EventuallyPerfectFailureDetector.EPFDComponents._
import se.kth.id2203.networking.{NetMessage, NetAddress}


//Define EPFD Implementation
class EPFD(epfdInit: Init[EPFD]) extends ComponentDefinition {

//  def this() {
//    this(Init[EPFD](Set[NetAddress]()))
//  }

  //EPFD subscriptions
  val timer = requires[Timer]
  val pLink = requires[Network]
  val epfd = provides[EventuallyPerfectFailureDetector]

  // EPDF component state and initialization

  //configuration parameters
  val self = cfg.getValue[NetAddress]("id2203.address")
  val topology = epfdInit match { case Init(nodes: Set[NetAddress] @unchecked) => nodes }
  val delta = cfg.getValue[Long]("components.EventuallyPerfectFailureDetector.delay")

  //mutable state
  var period = cfg.getValue[Long]("components.EventuallyPerfectFailureDetector.delay")
  var alive = Set(cfg.getValue[List[Address]]("components.EventuallyPerfectFailureDetector.topology"): _*)
  var suspected = Set[Address]()
  var seqnum = 0
  var delay = delta
  def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

  //EPFD event handlers
  ctrl uponEvent {
    case _: Start => handle {

      /* WRITE YOUR CODE HERE  */
      startTimer(period)


    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      if (!alive.intersect(suspected).isEmpty) {

        /* WRITE YOUR CODE HERE  */
        period = period + delta

      }

      seqnum = seqnum + 1

      for (p <- topology) {
        if (!alive.contains(p) && !suspected.contains(p)) {

          /* WRITE YOUR CODE HERE  */
          suspected = suspected + p
          trigger(Suspect(p) -> epfd) //Triggering broadcast to the failure detector!

        } else if (alive.contains(p) && suspected.contains(p)) {
          suspected = suspected - p
          trigger(Restore(p) -> epfd)
        }
        trigger(NetMessage(self, p, HeartbeatRequest(seqnum)) -> pLink);
      }
      alive = Set[Address]()
      startTimer(period)
    }
  }

  pLink uponEvent {
    case NetMessage(src, HeartbeatRequest(seq)) => handle {

      /* WRITE YOUR CODE HERE  */

      trigger(NetMessage( src, HeartbeatReply(seq)) -> pLink);

    }
    case NetMessage(src, HeartbeatReply(seq)) => handle {

      /* WRITE YOUR CODE HERE  */
      if(seq == seqnum || suspected.contains(src.src)) {
        alive = alive + src.src;
      }

    }
  }
};
