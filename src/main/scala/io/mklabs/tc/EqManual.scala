package io.mklabs.tc

object EqManual extends App {

  trait Eq[T] {
    def equals(val1: T, val2: T): Boolean
  }

  implicit object IntEq extends Eq[Int] {
    override def equals(val1: Int, val2: Int): Boolean = val1 == val2
  }

  implicit object StringEq extends Eq[String] {
    override def equals(val1: String, val2: String): Boolean = val1.equals(val2)
  }

  object Comparison {
    def compare[T](val1: T, val2: T)(implicit comparator: Eq[T]): Boolean = comparator.equals(val1, val2)
  }

  val aNumber: Int = 4
  val anotherNumber: Int = 5

  val result1 = Comparison.compare(aNumber, anotherNumber)
  println(s"$aNumber == $anotherNumber is $result1")

//  Comparison.compare(1, "1")

  object MyComparisons {
    implicit class MyIntComparison[T](val1: T)(implicit eq: Eq[T]) {
      def `===`(val2: T): Boolean = eq.equals(val1, val2)
    }
  }


  import MyComparisons._
  println(s"$aNumber === $anotherNumber is ${aNumber === anotherNumber}")

}
