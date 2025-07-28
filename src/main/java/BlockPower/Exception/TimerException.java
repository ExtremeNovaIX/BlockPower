package BlockPower.Exception;

/**
 * 与 TimerManager 相关的操作中发生的特定异常。
 * <p>
 * 当 TimerManager 的API被不正确地调用时，应抛出此异常，
 * 例如：查询一个不存在的计时器，或传入不符合逻辑的参数。
 * <p>
 * 继承自 RuntimeException，表示这是一个非检查性异常，
 * 通常由编程错误引起，调用者应修复其调用代码，而不是捕获此异常。
 */
public class TimerException extends RuntimeException {

    /**
     * 构造一个新的 TimerException，并指定详细的错误信息。
     *
     * @param message 详细的错误信息，用于向开发者解释错误原因。
     */
    public TimerException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 TimerException，并指定详细的错误信息和异常原因。
     * <p>
     * 这个构造函数允许你包装另一个底层异常，形成异常链，
     * 这对于调试非常有用。
     *
     * @param message 详细的错误信息。
     * @param cause   异常的原因（通常是另一个被捕获的异常）。
     */
    public TimerException(String message, Throwable cause) {
        super(message, cause);
    }
}