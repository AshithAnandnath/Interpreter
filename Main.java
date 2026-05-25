import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   HANDWRITTEN JAVA LEXER DEMONSTRATION         ");
        System.out.println("=================================================\n");

        // 1. Valid program case
        String validSource = 
            "// This is a sample program in our custom language\n" +
            "let x = 45.67;\n" +
            "let y = 123;\n" +
            "let message = \"hello world\";\n" +
            "let isWorking = true;\n" +
            "\n" +
            "if (x >= y) {\n" +
            "    print \"x is greater or equal\";\n" +
            "} else {\n" +
            "    print message;\n" +
            "}\n" +
            "\n" +
            "while (isWorking != false) {\n" +
            "    let value = null;\n" +
            "    print value;\n" +
            "    isWorking = false;\n" +
            "}\n";

        System.out.println("--- Valid Source Input Code ---");
        System.out.println(validSource);
        System.out.println("--- Scanned Tokens Output ---");
        
        try {
            Lexer lexer = new Lexer(validSource);
            List<Token> tokens = lexer.scanTokens();
            
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (LexerException e) {
            System.err.println("Error occurred during scanning: " + e.getMessage());
        }

        System.out.println("\n-------------------------------------------------\n");

        // 2. Error Case 1: Unexpected Character
        String invalidCharSource = 
            "let a = 10;\n" +
            "let b = @;\n"; // illegal character '@'
        
        System.out.println("--- Error Case 1: Unexpected Character Source ---");
        System.out.println(invalidCharSource);
        
        try {
            Lexer lexer = new Lexer(invalidCharSource);
            lexer.scanTokens();
            System.out.println("SUCCESS: (Unexpectedly succeeded!)");
        } catch (LexerException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION:");
            System.out.println(e.getMessage());
        }

        System.out.println("\n-------------------------------------------------\n");

        // 3. Error Case 2: Unterminated String
        String unterminatedStringSource = 
            "let name = \"john doe;\n" + // no closing quote
            "let age = 30;\n";
        
        System.out.println("--- Error Case 2: Unterminated String Source ---");
        System.out.println(unterminatedStringSource);
        
        try {
            Lexer lexer = new Lexer(unterminatedStringSource);
            lexer.scanTokens();
            System.out.println("SUCCESS: (Unexpectedly succeeded!)");
        } catch (LexerException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION:");
            System.out.println(e.getMessage());
        }
        
        System.out.println("\n=================================================");
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[Runtime Error] " + error.getMessage());
    }
}
