package common.models;

import java.util.Objects;

public class MealyNode implements Comparable<MealyNode> {
    public String q;

    public MealyNode() {
    }

    public MealyNode(MealyNode node) {
        this.q = node.q;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealyNode)) return false;
        MealyNode mealyNode = (MealyNode) o;
        return Objects.equals(q, mealyNode.q);
    }

    @Override
    public int hashCode() {
        return Objects.hash(q);
    }

    @Override
    public int compareTo(MealyNode o) {
        return q.compareTo(o.q);
    }
}
