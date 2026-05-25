import java.util.ArrayList;
import java.util.List;

/*
 * Person 3 ownership: recursive-descent parser.
 *
 * Expected teammate-provided types:
 * - Token with fields: TokenType type, String lexeme, Object literal, int line
 * - TokenType enum values:
 *   LET, PRINT, IF, ELSE, WHILE, FOR,
 *   IDENTIFIER, NUMBER, STRING, TRUE, FALSE, NULL,
 *   LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
 *   SEMICOLON, EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL,
 *   GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
 *   PLUS, MINUS, STAR, SLASH, EOF
 * - AST classes:
 *   Expr, AssignExpr, BinaryExpr, GroupingExpr, LiteralExpr, UnaryExpr,
 *   VariableExpr
 *   Stmt, BlockStmt, ExpressionStmt, IfStmt, PrintStmt, VarDeclareStmt,
 *   WhileStmt
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        if (match(TokenType.LET)) {
            return varDeclaration();
        }

        return statement();
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new VarDeclareStmt(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new BlockStmt(block());
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new PrintStmt(value);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition.");

        Stmt body = statement();
        return new WhileStmt(condition, body);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.LET)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition.");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            List<Stmt> statements = new ArrayList<>();
            statements.add(body);
            statements.add(new ExpressionStmt(increment));
            body = new BlockStmt(statements);
        }

        if (condition == null) {
            condition = new LiteralExpr(true);
        }
        body = new WhileStmt(condition, body);

        if (initializer != null) {
            List<Stmt> statements = new ArrayList<>();
            statements.add(initializer);
            statements.add(body);
            body = new BlockStmt(statements);
        }

        return body;
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new ExpressionStmt(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof VariableExpr) {
                Token name = ((VariableExpr) expr).name;
                return new AssignExpr(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new UnaryExpr(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new LiteralExpr(false);
        }
        if (match(TokenType.TRUE)) {
            return new LiteralExpr(true);
        }
        if (match(TokenType.NULL)) {
            return new LiteralExpr(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new LiteralExpr(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableExpr(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new GroupingExpr(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseException error(Token token, String message) {
        return new ParseException(token, message);
    }
}
