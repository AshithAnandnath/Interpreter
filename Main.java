import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    // 1. Create ONE instance of your newly fixed Interpreter
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: java Main [script.txt]");
            System.exit(64);
        } else if (args.length == 1) {
            // Run a file if passed via command line
            runFile(args[0]);
        } else {
            // Start the Live Interactive Prompt!
            runPrompt();
        }
    }

    // ---------------------------------------------------------
    // The Live Prompt (REPL)
    // ---------------------------------------------------------
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        System.out.println("=================================================");
        System.out.println("  WELCOME TO OUR CUSTOM LANGUAGE INTERPRETER!");
        System.out.println("  Type your code below (Press Ctrl+C to exit)");
        System.out.println("=================================================");

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break; 
            
            run(line);
            
            // Reset error flags so the prompt doesn't lock up after a typo
            hadError = false; 
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    // ---------------------------------------------------------
    // The Core Pipeline (Connecting all 4 Teammates)
    // ---------------------------------------------------------
    private static void run(String source) {
        try {
            // 1. PERSON 2: Lexical Analysis
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens(); 
            
            // 2. PERSON 3: Syntactic Analysis
            Parser parser = new Parser(tokens); 
            List<Stmt> statements = parser.parse(); 

            if (hadError) return;

            // 3. PERSON 1 & 4: Runtime Execution
            interpreter.interpret(statements);
            
        } catch (Exception e) {
            // If the Lexer or Parser throw an exception, print it safely
            System.err.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Standard Error Handling
    // ---------------------------------------------------------
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}