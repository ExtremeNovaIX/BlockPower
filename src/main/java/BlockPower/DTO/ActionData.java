package BlockPower.DTO;

public class ActionData {
    private String actionType;

    public static final String MINECART_RUSH = "MINECART_RUSH";

    public ActionData() {

    }

    public ActionData(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
