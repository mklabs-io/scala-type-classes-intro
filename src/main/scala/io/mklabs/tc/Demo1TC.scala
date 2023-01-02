package io.mklabs.tc

object Demo1TC extends App {

  trait Reviewer[T] {
    def rate(obj: T): Int
  }

  case class Restaurant(name: String, foodQuality: Int, environment: Int, location: Int)
  case class Dish(name: String, sweetness: Int, saltiness: Int, bitterness: Int, sourness: Int, umami: Int)

  object ReviewStrategies {

    implicit object RestaurantReviewer extends Reviewer[Restaurant] {
      override def rate(obj: Restaurant): Int =
        scala.math
          .round(0.6F * obj.foodQuality + 0.3F * obj.environment + 0.1F * obj.location)
    }

    implicit object DishReviewer extends Reviewer[Dish] {
      override def rate(obj: Dish): Int = scala.math.round(
        0.1F * obj.sweetness
          + 0.2F * obj.saltiness
          + 0.1F * obj.bitterness
          + 0.1F * obj.sourness
          + 0.5F * obj.umami
      )
    }

  }

  object Evaluator {
    def rate[T](obj: T)(implicit evaluator: Reviewer[T]): Int = evaluator.rate(obj)
  }

  val restaurant1 = Restaurant(name = "Cheesegaddon", foodQuality = 5, environment = 3, location = 2)

  import ReviewStrategies._
  val restaurant1Rating = Evaluator.rate(restaurant1)

  println(s"Restaurant ${restaurant1.name} final rate is: ${restaurant1Rating} stars")

}
