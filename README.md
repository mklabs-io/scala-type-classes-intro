
# yet another post about type classes in Scala

While recently having a chat with a colleague about type classes in scala, I realized I failed at properly explaining the concept. So here I am, giving it (yet) another stab.

In this post we will:
- understand the intuition behind type classes (TCs), to better reason when to use them;
- provide two concrete examples with code in scala;
- conclude with a very brief look into `cats`, a widely used scala library that heavily relies on TCs;

This readme contains the contents of the blog post published in [dev.to platform](https://dev.to/mklabs).

## Intuition

Type classes provide a way to model for different behaviors according to the type of the object being handled. So, at it's core, a type class is (unlike the name might at first sight suggest) actually just an interface - a `trait` in scala - that defines the behavior with a given method signature that accepts or returns generic type.
We then define concrete implementations for that interface for a given specific type. These we call the type class instances.
Usually, as a last step, one exposes this functionality with a given api, say, for example a static method.


## Example 1

Let's say we work in a company that does ratings, and, to simplify matters, issues 5 star ratings. Currently the company started with doing evaluations of restaurants. However, we want to be able to extend those in the future to other aspects in life, such as hotels, cities, concerts, cars, gadgets, etc, you name it.
Modelling our business domain, we'd quickly understand that a car is a completely distinct model than a hotel, so using classic OOP polymorphism by finding a common denominator interface that all of the other classes extend/inherit is a weak solution.
Instead, what we really want is just to define a general interface for rating any of these types - think <i>generics</i>. In more rigorous terms this is called <i>parametric polymorphism</i>, which refers to the ability to define a single code template that can work with any type `T` parameter.

```scala
  // this is our Type Class
  trait Reviewer[T] {
    def rate(obj: T): Int
  }

```
Our TC has a single method that accepts a generic type, and returns 1-5 star rating which we oversimplified with an Integer (`Int`) return type.

Let's add two (rather silly but hopefully illustrative) models:

```scala
  case class Restaurant(name: String, foodQuality: Int, environment: Int, location: Int)

  case class Dish(name: String, sweetness: Int, saltiness: Int, bitterness: Int, sourness: Int, umami: Int)

```

Now comes the second part, where we deviate from parametric polymorphism - where the concrete implementation is completely agnostic to the Type `T` being handled - and add specific TC instances that define different behaviors/strategies depending on the object being handled. For rating a restaurant one could implement a simple algorithm as follows:

```scala

  object RestaurantReviewer extends Reviewer[Restaurant] {
    override def rate(obj: Restaurant): Int =
      scala.math
        .round(0.6F * obj.foodQuality + 0.3F * obj.environment + 0.1F * obj.location)
  }

``` 
and, on the other hand, for reviewing a dish the following strategy:

```scala

  object RestaurantReviewer extends Reviewer[Restaurant] {
    override def rate(obj: Restaurant): Int =
      scala.math
        .round(0.6F * obj.foodQuality + 0.3F * obj.environment + 0.1F * obj.location)
  }

```

TC are also sometimes referred to as <i>ad hoc polymorphism</i>, which opposes parametric polymorphism because one purposely defines a concrete distinct implementation for every given type. I found Jason McClellan does a great job explaining the [different types of polymorphism and in concrete for type classes, so a highly recommend you to have a look at it](https://dev.to/jmcclell/inheritance-vs-generics-vs-typeclasses-in-scala-20op).


The usual third and last step is to expose the overall rating functionality, for example, as follows:

```scala

  object Evaluator {
    def rate[T](obj: T)(evaluator: Reviewer[T]): Int = evaluator.rate(obj)
  }

```

Alright, this is already enough for us to dry-run in a demo app:

```scala

object DemoTC extends App {

  trait Reviewer[T] {
    def rate(obj: T): Int
  }

  case class Restaurant(name: String, foodQuality: Int, environment: Int, location: Int)
  case class Dish(name: String, sweetness: Int, saltiness: Int, bitterness: Int, sourness: Int, umami: Int)

  object ReviewStrategies {

    object RestaurantReviewer extends Reviewer[Restaurant] {
      override def rate(obj: Restaurant): Int =
        scala.math
          .round(0.6F * obj.foodQuality + 0.3F * obj.environment + 0.1F * obj.location)
    }

    object DishReviewer extends Reviewer[Dish] {
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
    def rate[T](obj: T)(evaluator: Reviewer[T]): Int = evaluator.rate(obj)
  }

  val restaurant1 = Restaurant(name = "Cheesegaddon", foodQuality = 5, environment = 3, location = 2)
  
  // rate using `RestaurantReviewer` strategy:  
  val restaurant1Rating = Evaluator.rate(restaurant1)(ReviewStrategies.RestaurantReviewer)
  
  // will print: Restaurant Cheesegaddon final rate is: 4 stars
  println(s"Restaurant ${restaurant1.name} final rate is: ${restaurant1Rating} stars")


}

```


Note that so far we're not leveraging any of scala's powerful features. As a matter a fact, we can implement the exact same demo code in java (yes java nerds, bear with me, as mentioned in the inline code comments, the above code is not the finest, but it illustrates my point):

```java
public final class DemoTCJava {

    // this is our Type Class
    interface Reviewer<T> {
        int rate(T obj);
    }

    // Note 1: Avoiding using newer Java Records to make this code easily runable on any jdk version 
    // Note 2: made usually private internal vars public as to avoid extra verbosity of implementing getters;
    private static final class Restaurant {
        public final String name;
        public final int foodQuality;
        public final int environment;
        public final int location;

        public Restaurant(final String name, final int foodQuality, final int environment, final int location) {
            this.name = name;
            this.foodQuality = foodQuality;
            this.environment = environment;
            this.location = location;
        }
    }

    private static final class Dish {
        public final String name;
        public final int sweetness;
        public final int saltiness;
        public final int bitterness;
        public final int sourness;
        public final int umami;

        public Dish(final String name, final int sweetness, final int saltiness, final int bitterness, final int sourness, final int umami) {
            this.name = name;
            this.sweetness = sweetness;
            this.saltiness = saltiness;
            this.bitterness = bitterness;
            this.sourness = sourness;
            this.umami = umami;
        }
    }

    private static final class RestaurantReviewer implements Reviewer<Restaurant> {
        @Override
        public int rate(Restaurant obj) {
            return Math.round(0.6F * obj.foodQuality + 0.3F * obj.environment + 0.1F * obj.location);
        }
    }

    private static final class DishReviewer implements Reviewer<Dish> {
        @Override
        public int rate(Dish obj) {
            return Math.round(
                    0.1F * obj.sweetness
                            + 0.2F * obj.saltiness
                            + 0.1F * obj.bitterness
                            + 0.1F * obj.sourness
                            + 0.5F * obj.umami
            );
        }
    }

    private static final class Evaluator<T> {
        public int rate(T obj, Reviewer<T> evaluator) {
            return evaluator.rate(obj);
        }
    }

    public static void main(String[] args) {
        final Restaurant restaurant1 = new Restaurant("Cheesegaddon", 5, 3, 2);
        final RestaurantReviewer reviewer = new RestaurantReviewer();
        final Evaluator<Restaurant> evaluator = new Evaluator<>();
        final int restaurant1Rating = evaluator.rate(restaurant1, reviewer);

        System.out.println(
                String.format("Restaurant %s final rate is: %d stars",
                        restaurant1.name,
                        restaurant1Rating
                )
        );
    }

}

```

This is an example where scala's implicits really shine. If we made our API - the Evaluator static class - accept the evaluator as an implicit argument:

```scala

  object Evaluator {
    def rate[T](obj: T)(implicit evaluator: Reviewer[T]): Int = evaluator.rate(obj)
  }

```

.. and changed our `ReviewStrategies` to implicitly define TC instances:
```scala

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

```

.. we could then simply import the intended review strategies based on scope:

```scala

  import ReviewStrategies._
  val restaurant1Rating = Evaluator.rate(restaurant1)

```  

And this is one of my favorite things about TCs, namely the flexibility that they provide. Maybe tomorrow a new food critic comes, that has uses a completely different algorithm for computing the rate. Absolutely no change would be required
in our API - we would simply need to import the new different set of strategies.


## Example 2

Our second example is meant to illustrate an additional nice thing about TCs: the compile time safety.

Let's say we want to prevent the following to happen:

```scala
  
  val aComparison = 2 == "a String"
  // will print: Comparison is false
  println(s"Comparison is $aComparison")

```
Your IDE will hopefully highlight the fact that we are comparing an int with a string, but compilation will succeed and the expression will evaluate to false.

We can use the same strategy as before. Let's define an `Eq` TC with two concrete implementations, respectively for `Int` and `String`:

```scala

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

  // will print: 4 == 5 is false
  println(s"$aNumber == $anotherNumber is $result1")

```
With this approach we are certain that if we try:
```scala
Comparison.compare(1, "one")
```
... compilation will fail, as intended.

We could improve our `Comparison` implementation just a little further leveraging scala extension methods:

```scala
  object MyComparisons {
    implicit class MyIntComparison[T](val1: T)(implicit eq: Eq[T]) {
      def `===`(val2: T): Boolean = eq.equals(val1, val2)
    }
  }

``` 
If we import `MyComparisons` object, we will immediately start having access to our custom `===` method for all `Int` and `String` type objects, allowing the following:

```scala
  import MyComparisons._
  // will print: 4 === 5 is false
  println(s"$aNumber === $anotherNumber is ${aNumber === anotherNumber}")

```
In case you're wondering how extension methods work, essentially the compiler is smart enough to underneath the hood to rap our `Int` instance stored in variable `aNumber` into a new instance of:
`new MyIntComparison(aNumber)`
.. and call the method `===` towards the other `Int` instance stored in variable `anotherNumber`:
`new MyIntComparison(aNumber).=== (anotherNumber)`

Pretty cool, right?

If you're thinking to implement this by yourself, think again. Scala's Swiss-army-knife library `cats` provides exactly this same functionality.

Start by importing it in sbt:
```sbt
val catsVersion = "2.1.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion
)

```
And use it:

```scala

  import cats.Eq
  import cats.instances.int._
  import  cats.syntax.eq._

  // will print: 2 === 3 is false
  println(s"2 === 3 is ${2 === 3}")

```



## Conclusion

I'd like to wish that, if anything, this post manages to convey the intuition behind type classes.

Type classes are useful when we want to model different behaviors according to the specific type of an object. With our previous example, we tried to provide a case where it makes little sense to use polymorphism to model distinct concepts, namely restaurants, hotels, cars, cities, etc. In other words, instead of having a common interface that all of the previous classes inherit, we prefer to rather use generics.
Our type class is nothing but an interface that defines the signature for a given behavior/method leveraging generics.
This leaves the concrete implementation open for accommodate different strategies according to the concrete types, or even different strategies for the same time. This idea is probably what makes type classes so powerful: the flexibility to extend them.

Our second type class example attempted to illustrate one last perk: type safety at compile time. It did so with a simplified example of the `cats` core library for type safety equality comparison between objects. If you're not familiar with cats, [go ahead and give it go](https://typelevel.org/cats/).

## References
- [parametric polymorphism](https://en.wikipedia.org/wiki/Parametric_polymorphism)
- [ad hoc polymorphism](https://en.wikipedia.org/wiki/Parametric_polymorphism)
- Jason McClellan on the [different types of polymorphism and where type classes fit](https://dev.to/jmcclell/inheritance-vs-generics-vs-typeclasses-in-scala-20op)
- [cats core library](https://typelevel.org/cats/)    
