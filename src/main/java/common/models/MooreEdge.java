package common.models;

import java.util.Objects;

public class MooreEdge {
    public MooreNode from;
    public MooreNode to;
    public String x;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MooreEdge)) return false;
        MooreEdge mooreEdge = (MooreEdge) o;
        return Objects.equals(from, mooreEdge.from) &&
            Objects.equals(to, mooreEdge.to) &&
            Objects.equals(x, mooreEdge.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, x);
    }
}
