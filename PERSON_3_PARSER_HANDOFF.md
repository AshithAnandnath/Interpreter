# Person 3 Parser Handoff

## Owner

Person 3 is responsible for the syntactic analyzer, also called the parser.

The parser takes the token list produced by Person 2's lexer and converts it into
the AST statement list created from Person 1's AST classes. Person 4's
interpreter will then execute that AST.

## Files Created

### Parser.java

This file contains the recursive-descent parser.

Required public API:

```java
public Parser(List<Token> tokens)
public List<Stmt> parse()
```

These signatures must not be changed because the markdown instructions require
this exact pipeline:

```java
Lexer.scanTokens() -> Parser(List<Token>) -> Parser.parse() -> Interpreter.interpret(List<Stmt>)
```

### ParseException.java

This file contains the custom parser exception.

It formats syntax errors with line numbers, for example:

```text
[Line 4] Error at ';': Expected expression.
```

This follows the project rule that errors must use custom exceptions and include
the exact line number.

## What The Parser Supports

The parser currently supports:

- Variable declarations
- Print statements
- Expression statements
- Block statements
- If/else statements
- While statements
- For statements
- Assignment expressions
- Arithmetic expressions
- Comparison expressions
- Equality expressions
- Unary expressions
- Grouped expressions
- Number literals
- String literals
- Boolean literals
- Null literal
- Variable references

Example supported program:

```java
let x = 10;
print x;

if (x > 5) {
    print "large";
} else {
    print "small";
}

while (x < 20) {
    x = x + 1;
}

for (let i = 0; i < 5; i = i + 1) {
    print i;
}
```

## Token Requirements For Person 2

Person 2 must make sure the lexer creates `Token` objects that contain at least:

```java
TokenType type;
String lexeme;
Object literal;
int line;
```

The parser expects these `TokenType` enum values:

```java
LET,
PRINT,
IF,
ELSE,
WHILE,
FOR,

IDENTIFIER,
NUMBER,
STRING,
TRUE,
FALSE,
NULL,

LEFT_PAREN,
RIGHT_PAREN,
LEFT_BRACE,
RIGHT_BRACE,
SEMICOLON,

EQUAL,
EQUAL_EQUAL,
BANG,
BANG_EQUAL,

GREATER,
GREATER_EQUAL,
LESS,
LESS_EQUAL,

PLUS,
MINUS,
STAR,
SLASH,

EOF
```

### Important For Loop Token Requirement

Because the parser now supports `for` loops safely by converting them into
existing `while` loop AST nodes, Person 2 must add this token type:

```java
FOR
```

The lexer must scan the keyword:

```java
for
```

as:

```java
TokenType.FOR
```

Without this token, `for` loops will not reach the parser correctly.

If Person 2 uses different enum names, the parser will not compile until the
names are aligned.

## AST Requirements For Person 1

Person 1 must provide base AST classes named:

```java
Expr
Stmt
```

The parser expects these expression classes:

```java
AssignExpr
BinaryExpr
GroupingExpr
LiteralExpr
UnaryExpr
VariableExpr
```

The parser expects these statement classes:

```java
BlockStmt
ExpressionStmt
IfStmt
PrintStmt
VarDeclareStmt
WhileStmt
```

There is intentionally no `ForStmt` requirement.

The parser supports `for` loops by converting them into existing AST classes:

```java
for (let i = 0; i < 5; i = i + 1) {
    print i;
}
```

is parsed as if it were:

```java
{
    let i = 0;
    while (i < 5) {
        print i;
        i = i + 1;
    }
}
```

This means Person 1 does not need to create a new `ForStmt` AST class, and
Person 4 does not need to add a special visitor method for `for` loops. Person 4
only needs existing `BlockStmt`, `WhileStmt`, and `ExpressionStmt` support.

The constructors should match the parser usage:

```java
new AssignExpr(Token name, Expr value)
new BinaryExpr(Expr left, Token operator, Expr right)
new GroupingExpr(Expr expression)
new LiteralExpr(Object value)
new UnaryExpr(Token operator, Expr right)
new VariableExpr(Token name)

new BlockStmt(List<Stmt> statements)
new ExpressionStmt(Expr expression)
new IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch)
new PrintStmt(Expr expression)
new VarDeclareStmt(Token name, Expr initializer)
new WhileStmt(Expr condition, Stmt body)
```

Important: `VariableExpr` must expose the variable token as `name`, because
assignment parsing needs this field:

```java
Token name = ((VariableExpr) expr).name;
```

For example:

```java
class VariableExpr extends Expr {
    final Token name;

    VariableExpr(Token name) {
        this.name = name;
    }

    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariableExpr(this);
    }
}
```

## Requirements For Person 4

Person 4's interpreter should receive the parser output like this:

```java
Lexer lexer = new Lexer(source);
List<Token> tokens = lexer.scanTokens();

Parser parser = new Parser(tokens);
List<Stmt> statements = parser.parse();

Interpreter interpreter = new Interpreter();
interpreter.interpret(statements);
```

The interpreter must use the Visitor pattern for AST evaluation. It should not
use large `switch` statements or `instanceof` checks to evaluate AST nodes.

The interpreter must also use Person 1's `Environment` class for variables and
nested scopes.

## Parser Flow

The parser starts from:

```java
parse()
```

Then it repeatedly parses declarations:

```java
declaration()
```

Declarations currently include:

```java
let variable = expression;
```

Other input is parsed as a statement.

Statement parsing supports:

```java
print expression;
if (condition) statement else statement
while (condition) statement
for (initializer; condition; increment) statement
{ block statements }
expression;
```

Expression parsing follows this precedence order:

```text
assignment
equality
comparison
term
factor
unary
primary
```

This means multiplication and division correctly bind tighter than addition and
subtraction.

Example:

```java
let result = 2 + 3 * 4;
```

The parser reads this as:

```text
2 + (3 * 4)
```

not:

```text
(2 + 3) * 4
```

## Error Handling

Syntax errors throw `ParseException`.

Examples:

```java
let = 10;
```

Produces an error like:

```text
[Line 1] Error at '=': Expected variable name.
```

Another example:

```java
print 10
```

Produces an error like:

```text
[Line 1] Error at end: Expected ';' after value.
```

The parser does not print errors directly. Whoever calls the parser should catch
`ParseException` at the top level and decide how to show the message.

Example:

```java
try {
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();
    interpreter.interpret(statements);
} catch (ParseException error) {
    System.err.println(error.getMessage());
}
```

## Integration Checklist

Before combining everyone's work, check these items:

- Person 2's `Token` class has `type`, `lexeme`, `literal`, and `line`.
- Person 2's `TokenType` enum contains every value listed above.
- Person 2 scans the keyword `for` as `TokenType.FOR`.
- Person 2 always adds an `EOF` token at the end of `scanTokens()`.
- Person 1's AST class names match the parser.
- Person 1's AST constructors match the parser.
- Person 1 does not need a `ForStmt` class.
- Person 1's AST nodes implement the Visitor pattern.
- Person 4's interpreter accepts `List<Stmt>`.
- Person 4's interpreter uses `Environment` for variables.
- Person 4 does not need special `for` execution logic because the parser
  converts `for` loops into `while` loops.
- The main program calls the pipeline in this order:

```java
List<Token> tokens = lexer.scanTokens();
Parser parser = new Parser(tokens);
List<Stmt> statements = parser.parse();
interpreter.interpret(statements);
```

## Scope Boundary

This parser intentionally does not implement lexer logic, AST class definitions,
environment memory management, or interpreter execution.

Those are owned by the other teammates.

This file only completes Person 3's parser responsibility and provides the
contract needed for all four parts to work together.
