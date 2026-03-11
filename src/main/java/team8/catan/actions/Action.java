package team8.catan.actions;

import java.util.Objects;

public class Action {
    private final ActionType actionType;
    private final ActionTarget target;

    public Action(ActionType actionType, int targetId) {
        this(actionType, targetId == ActionTarget.NO_TARGET_ID
            ? ActionTarget.none()
            : ActionTarget.of(actionType.getTargetKind(), targetId));
    }

    public Action(ActionType actionType, ActionTarget target) {
        this.actionType = Objects.requireNonNull(actionType, "actionType");
        this.target = Objects.requireNonNull(target, "target");
    }

    public ActionType getActionType() {
        return actionType;
    }

    public ActionTarget getTarget() {
        return target;
    }

    public int getTargetId() {
        return target.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Action)) {
            return false;
        }
        Action action = (Action) o;
        return actionType == action.actionType && target.equals(action.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionType, target);
    }
}
