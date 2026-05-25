public class ParseException extends RuntimeException {
    public final Token token;

    public ParseException(Token token, String message) {
        super(formatMessage(token, message));
        this.token = token;
    }

    private static String formatMessage(Token token, String message) {
        if (token.type == TokenType.EOF) {
            return "[Line " + token.line + "] Error at end: " + message;
        }

        return "[Line " + token.line + "] Error at '" + token.lexeme + "': " + message;
    }
}
