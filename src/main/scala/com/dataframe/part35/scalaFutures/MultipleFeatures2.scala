package com.dataframe.part35.scalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, future}



object Cloud {
  def runAlgorithm(i: Int): Future[Int] = future {
    val result = i + 10
    println(s"returning result from cloud: $result")
    result
  }
}


object MultipleFeatures2 {


  def main(args: Array[String]): Unit = {



    println("starting futures")
    val result1 = Cloud.runAlgorithm(10)
    val result2 = Cloud.runAlgorithm(20)
    val result3 = Cloud.runAlgorithm(30)

    println("before for-comprehension")
    val result = for {
      r1 <- result1
      r2 <- result2
      r3 <- result3
    } yield (r1 + r2 + r3)

    println("before onSuccess")
    result onSuccess {
      case result => println(s"total = $result")
    }

    println("before sleep at the end")
    //sleep(2000)  // important: keep the jvm alive

  }

}
