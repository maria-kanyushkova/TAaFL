package common.models;

import java.util.Objects;

public class FullCheckMealyEdge extends MealyEdge {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealyEdge)) return false;
        MealyEdge mealyEdge = (MealyEdge) o;
        return Objects.equals(from, mealyEdge.from) &&
            Objects.equals(to, mealyEdge.to) &&
            Objects.equals(x, mealyEdge.x) &&
            Objects.equals(y, mealyEdge.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, x, y);
    }
}
