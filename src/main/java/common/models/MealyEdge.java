package common.models;

import java.util.Objects;

public class MealyEdge {
    public MealyNode from;
    public MealyNode to;
    public String x;
    public String y;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealyEdge)) return false;
        MealyEdge mealyEdge = (MealyEdge) o;
        return Objects.equals(to, mealyEdge.to) &&
            Objects.equals(y, mealyEdge.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, y);
    }
}
