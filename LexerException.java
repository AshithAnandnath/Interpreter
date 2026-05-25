public class LexerException extends RuntimeException {
    private final int line;

    public LexerException(int line, String message) {
        super("[Line " + line + "] Error: " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
