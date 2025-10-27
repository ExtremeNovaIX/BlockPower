package BlockPower.Skills;

public class SkillExecutionResult {
    private String message;
    private boolean isSuccess;

    public SkillExecutionResult(String message, boolean isSuccess) {
        this.message = message;
        this.isSuccess = isSuccess;
    }

    public static SkillExecutionResult success(String message) {
        return new SkillExecutionResult(message, true);
    }

    public static SkillExecutionResult success() {
        return new SkillExecutionResult("", true);
    }

    public static SkillExecutionResult fail(String message) {
        return new SkillExecutionResult(message, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
