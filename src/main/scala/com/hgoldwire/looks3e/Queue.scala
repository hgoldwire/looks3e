package com.hgoldwire.looks3e

import java.util
import java.util.concurrent.LinkedBlockingQueue

import scala.collection.JavaConverters._

object Queue {
  def apply[A](as: Iterable[A]) = {
    val q = new Queue[A]
    q.add(as)
  }
}

class Queue[A] {
  val queue = new LinkedBlockingQueue[A]()

  def take(n: Int) = {
    val inFlight = new util.ArrayList[A](n)
    if (queue.drainTo(inFlight, n) == 0) {
      inFlight.add(queue.take())
    }
    inFlight.asScala
  }

  def add(a: A): Queue[A] = {
    queue.put(a)
    this
  }

  def add(as: Iterable[A]): Queue[A] = {
    as.foreach(add)
    this
  }
}