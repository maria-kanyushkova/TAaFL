package common.models;

import java.util.Objects;

public class MooreNode implements Comparable<MooreNode> {
    public String q;
    public String y;

    public MooreNode(MooreNode node) {
        this.q = node.q;
        this.y = node.y;
    }

    public MooreNode() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MooreNode)) return false;
        MooreNode mooreNode = (MooreNode) o;
        return Objects.equals(q, mooreNode.q) &&
            Objects.equals(y, mooreNode.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, y);
    }

    @Override
    public int compareTo(MooreNode o) {
        return q.compareTo(o.q);
    }
}
