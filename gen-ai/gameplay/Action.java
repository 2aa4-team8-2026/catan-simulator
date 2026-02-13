package genaicodebase.gameplay;

public class Action {
    public static Action lastAction;
    public ActionType actionType;
    public int targetId;

    public Action() {
        this.actionType = ActionType.PASS;
        this.targetId = (int) (System.currentTimeMillis() % 10);
        lastAction = this;
    }

    public Action(ActionType actionType, int targetId) {
        this.actionType = actionType;
        this.targetId = targetId;
        lastAction = this;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public void resetToDefault() {
        this.actionType = ActionType.PASS;
        this.targetId = -1;
    }
}
