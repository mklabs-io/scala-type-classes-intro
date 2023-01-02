package io.mklabs.tc;

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
