package common.converter;

import java.util.Objects;

class MooreState {
    private String y;
    String q;

    MooreState(String y, String q) {
        this.y = y;
        this.q = q;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MooreState)) return false;
        MooreState that = (MooreState) o;
        return Objects.equals(y, that.y) &&
            Objects.equals(q, that.q);
    }

    @Override
    public int hashCode() {
        return Objects.hash(y, q);
    }
}
