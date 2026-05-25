import java.util.List;


public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }

    // -------------------------------------------------------------------------
    // Stmt.Visitor<Void> implementations
    // -------------------------------------------------------------------------

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarDeclareStmt(VarDeclareStmt stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Expr.Visitor<Object> implementations
    // -------------------------------------------------------------------------

    @Override
    public Object visitAssignExpr(AssignExpr expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(GroupingExpr expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                throw new RuntimeError(expr.operator,
                        "Unknown unary operator '" + expr.operator.lexeme + "'.");
        }
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object left  = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            // Arithmetic
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Division by zero.");
                return (double) left / (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                // Allow implicit number-to-string coercion for convenience
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");

            // Comparison
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            // Equality
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);

            default:
                throw new RuntimeError(expr.operator,
                        "Unknown binary operator '" + expr.operator.lexeme + "'.");
        }
    }

    @Override
    public Object visitVariableExpr(VariableExpr expr) {
        return environment.get(expr.name);
    }

    // -------------------------------------------------------------------------
    // Internal execution helpers
    // -------------------------------------------------------------------------

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment previous = this.environment;
        try {
            this.environment = blockEnvironment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // Restore the enclosing environment unconditionally, even on RuntimeError.
            this.environment = previous;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // -------------------------------------------------------------------------
    // Type helpers
    // -------------------------------------------------------------------------

    /**
     * Truthiness rules: null is false, boolean is its own value, everything else is true.
     */
    private boolean isTruthy(Object object) {
        if (object == null)          return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null)              return false;
        return a.equals(b);
    }

    /**
     * Validates that a single operand is a Double; throws RuntimeError otherwise.
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator,
                "Operand must be a number, got '" + typeName(operand) + "'.");
    }

    /**
     * Validates that both operands are Doubles; throws RuntimeError otherwise.
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        if (!(left instanceof Double))
            throw new RuntimeError(operator,
                    "Left operand must be a number, got '" + typeName(left) + "'.");
        throw new RuntimeError(operator,
                "Right operand must be a number, got '" + typeName(right) + "'.");
    }

    /**
     * Converts a runtime value to its user-facing string representation.
     * Strips the trailing ".0" from whole-number doubles (e.g. 3.0 -> "3").
     */
    private String stringify(Object object) {
        if (object == null)            return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    /**
     * Returns a human-readable type name for error messages.
     */
    private String typeName(Object object) {
        if (object == null)              return "nil";
        if (object instanceof Double)    return "number";
        if (object instanceof String)    return "string";
        if (object instanceof Boolean)   return "boolean";
        return object.getClass().getSimpleName();
    }
}