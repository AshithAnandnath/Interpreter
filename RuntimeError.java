public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super("[Line " + token.line + "] Error: " + message);
        this.token = token;
    }
}
