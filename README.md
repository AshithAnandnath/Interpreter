# Custom Java Interpreter

This project is a custom programming language interpreter built completely from
scratch in Java. It is a simple tree-walking interpreter designed to show how a
programming language works internally, from reading source code to producing
runtime output.

The project does not use external parser generators such as ANTLR or JavaCC.
All major parts are written manually using Java Standard Edition.

## Core Idea

The core idea is to build a small interpreted language by following the same
major stages used in real language runtimes:

```text
Source Code
-> Lexer
-> Tokens
-> Parser
-> AST
-> Interpreter
-> Output
```

The lexer breaks raw text into tokens. The parser checks syntax and builds an
Abstract Syntax Tree. The interpreter walks that tree and executes the program.

## What We Built

The interpreter currently supports:

- Variable declarations using `let`
- Variable assignment
- `print` statements
- `if` / `else` conditions
- `while` loops
- `for` loops
- Block statements using `{ }`
- Nested scopes
- Numbers
- Strings
- Booleans: `true` and `false`
- `null`
- Arithmetic operators: `+`, `-`, `*`, `/`
- Comparison operators: `>`, `>=`, `<`, `<=`
- Equality operators: `==`, `!=`
- Unary operators: `!`, `-`
- Single-line comments using `//`
- Lexer, parser, and runtime errors with line numbers
- Interactive terminal REPL
- Script-file execution

## Team Structure

The work was divided into four major responsibilities:

- Person 1: AST classes and environment memory handling
- Person 2: Lexer and token generation
- Person 3: Parser and syntax analysis
- Person 4: Interpreter and execution

Each part connects through a strict pipeline:

```java
Lexer lexer = new Lexer(source);
List<Token> tokens = lexer.scanTokens();

Parser parser = new Parser(tokens);
List<Stmt> statements = parser.parse();

Interpreter interpreter = new Interpreter();
interpreter.interpret(statements);
```

## File Overview

| File | Purpose |
| --- | --- |
| `Main.java` | Entry point. Runs a file or starts the interactive prompt. |
| `Lexer.java` | Converts source code into tokens. |
| `Token.java` | Stores token type, lexeme, literal value, and line number. |
| `TokenType.java` | Defines all token categories. |
| `Parser.java` | Converts tokens into AST statements and expressions. |
| `Expr.java` | Defines expression AST nodes and visitor methods. |
| `Stmt.java` | Defines statement AST nodes and visitor methods. |
| `Interpreter.java` | Walks the AST and executes the program. |
| `Environment.java` | Stores variables and supports nested scopes. |
| `LexerException.java` | Custom lexer error. |
| `ParseException.java` | Custom parser error. |
| `RuntimeError.java` | Custom runtime error. |
| `PERSON_3_PARSER_HANDOFF.md` | Parser integration notes for teammates. |
| `Interpreter_Project_Demo_Guide.pdf` | Shareable demo explanation document. |

## How It Works

Example program:

```java
let x = 2 + 3 * 4;
print x;
```

First, the lexer converts the source into tokens:

```text
LET IDENTIFIER EQUAL NUMBER PLUS NUMBER STAR NUMBER SEMICOLON PRINT IDENTIFIER SEMICOLON EOF
```

Then the parser builds an AST. It understands operator precedence, so this:

```java
2 + 3 * 4
```

is parsed as:

```text
2 + (3 * 4)
```

Finally, the interpreter evaluates the AST and prints:

```text
14
```

## For Loop Design

`for` loops are supported without adding a separate `ForStmt` AST class. The
parser converts a `for` loop into an equivalent block and `while` loop.

This:

```java
for (let i = 0; i < 3; i = i + 1) {
    print i;
}
```

is internally handled like:

```java
{
    let i = 0;
    while (i < 3) {
        print i;
        i = i + 1;
    }
}
```

This keeps the interpreter simpler because it only needs to execute existing
`BlockStmt`, `WhileStmt`, and `ExpressionStmt` nodes.

## How To Run

Open PowerShell in the project folder:

```powershell
cd "C:\Users\ankit\OneDrive\Documents\Ashith Anandnath\python\Interpreter\Interpreter"
```

Compile the Java files:

```powershell
javac *.java
```

Start the interactive prompt:

```powershell
java Main
```

You can now type code directly into the terminal.

## Example Terminal Test

After running `java Main`, paste these lines:

```java
print "===== DEMO START =====";
let x = 2 + 3 * 4;
print x;
if (x > 10) { print "x is greater than 10"; } else { print "x is small"; }
let total = 0;
for (let i = 1; i <= 5; i = i + 1) { total = total + i; }
print total;
let value = 100;
{ let value = 200; print value; }
print value;
print true;
print null;
print "===== DEMO COMPLETE =====";
```

Expected important output:

```text
===== DEMO START =====
14
x is greater than 10
15
200
100
true
nil
===== DEMO COMPLETE =====
```

## Running A Script File

Create a file, for example:

```text
test.script
```

Put language code inside it:

```java
let total = 0;
for (let i = 1; i <= 10; i = i + 1) {
    total = total + i;
}
print total;
```

Run it with:

```powershell
java Main test.script
```

Expected output:

```text
55
```

## Error Handling

The interpreter uses custom errors with line numbers.

Lexer error example:

```java
print @;
```

Parser error example:

```java
let = 10;
```

Runtime error example:

```java
print missingVariable;
```

These errors help identify exactly where the program failed.

## Limitations

This is a simple educational interpreter, not a full production language.

Current limitations:

- No user-defined functions
- No arrays
- No classes
- No `break` or `continue`
- No input function
- No advanced standard library

The architecture is modular, so these features can be added later by extending
the lexer, parser, AST, and interpreter.

## Project Summary

This project demonstrates the complete basic workflow of an interpreter:

- Reading source code
- Tokenizing input
- Parsing grammar
- Building an AST
- Managing variable memory and scope
- Executing code through a tree-walking interpreter

It gives a practical understanding of how programming languages process and run
code internally.
