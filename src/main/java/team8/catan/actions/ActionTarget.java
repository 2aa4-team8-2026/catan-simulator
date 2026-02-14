package team8.catan.actions;

import java.util.Objects;

public final class ActionTarget {
    public static final int NO_TARGET_ID = -1;

    private final TargetKind kind;
    private final int id;

    private ActionTarget(TargetKind kind, int id) {
        this.kind = kind;
        this.id = id;
    }

    public static ActionTarget none() {
        return new ActionTarget(TargetKind.NONE, NO_TARGET_ID);
    }

    public static ActionTarget of(TargetKind kind, int id) {
        return new ActionTarget(kind, id);
    }

    public TargetKind getKind() {
        return kind;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionTarget)) {
            return false;
        }
        ActionTarget that = (ActionTarget) o;
        return id == that.id && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id);
    }
}
