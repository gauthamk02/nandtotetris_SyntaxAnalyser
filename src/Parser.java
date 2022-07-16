import java.util.ArrayList;

public class Parser {

    static String[] subroutinedec = { "constructor", "function", "method" };
    static String[] classvardec = { "static", "field" };
    static String[] vartype = { "int", "String", "char" };
    static String statement_end = "<symbol> } </symbol>";
    static String statement_begin = "<symbol> { </symbol>";
    
    private ArrayList<String> input;
    private int indent = 0;
    // private int currLine = 0;

    Parser(ArrayList<String> inputfile) {
        input = inputfile;
    }

    ArrayList<String> parse() {
        ArrayList<String> output = new ArrayList<>();
        for(int i = 0; i < input.size(); i++) {
            String line = input.get(i);
            if(line.contains("class")) {
                output.add("\t".repeat(indent) + "<class>");
                output.addAll(compileClass(getScope(input, i)));
                output.add("\t".repeat(indent) + "</class>");
            }
        }
        return output;
    }

    ArrayList<String> compileClass(ArrayList<String> tokens) {
        indent += 1;
        ArrayList<String> classOut = new ArrayList<>();
        if(!getTokenClass(tokens.get(0)).equals("keyword") && !getTokenValue(tokens.get(0)).equals("class")) {
            System.out.println("Error: Expected keyword class");
            return null;
        }
        classOut.add("\t".repeat(indent) + tokens.get(0));
        classOut.add("\t".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(1)) + " </identifier>");
        if(!getTokenClass(tokens.get(2)).equals("symbol") && !getTokenValue(tokens.get(2)).equals("{")) {
            System.out.println("Error: Expected symbol {");
            return null;
        }
        classOut.add("\t".repeat(indent) + tokens.get(2));
        // currLine += 3;
        for(int currLine = 3; currLine < tokens.size(); currLine++) {
            if(getTokenClass(tokens.get(currLine)).equals("keyword") && getTokenValue(tokens.get(currLine)).equals("constructor")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            }
            else if(getTokenClass(tokens.get(currLine)).equals("keyword") && getTokenValue(tokens.get(currLine)).equals("function")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            }
            else if(getTokenClass(tokens.get(currLine)).equals("keyword") && getTokenValue(tokens.get(currLine)).equals("method")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            }
            else if(getTokenClass(tokens.get(currLine)).equals("keyword") && getTokenValue(tokens.get(currLine)).equals("var")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            }
            else if(getTokenClass(tokens.get(currLine)).equals("symbol") && getTokenValue(tokens.get(currLine)).equals("}")) {
                classOut.add("\t".repeat(indent) + tokens.get(currLine));
                break;
            }
            else {
                System.out.println("Error: Expected keyword constructor, function, or method");
                return null;
            }
        }

        indent -= 1;
        return classOut;
    }

    ArrayList<String> compileSubroutine(ArrayList<String> tokens) {
        ArrayList<String> subroutineOut = new ArrayList<>();
        // if(getTokenClass(tokens.get(0)).equals("keyword") || !getTokenValue(tokens.get(0)).equals("constructor") || getTokenValue(tokens.get(0)).equals("function") || getTokenValue(tokens.get(0)).equals("method")) {
        //     System.out.println("Error: Expected keyword constructor, function, or method");
        //     return null;
        // }

        subroutineOut.add("\t".repeat(indent) + "<subroutineDec>");
        indent += 1;
        // subroutineOut.addAll(tokens.subList(0, 4));
        subroutineOut.add("\t".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        subroutineOut.add("\t".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(1)) + " </keyword>");
        subroutineOut.add("\t".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(2)) + " </identifier>");
        
        subroutineOut.add("\t".repeat(indent) + "<symbol> ( </symbol>");
        subroutineOut.add("\t".repeat(indent) + "<parameterList>");
        indent += 1;
        //TODO: Add parameter list
        indent -= 1;
        subroutineOut.add("\t".repeat(indent) + "</parameterList>");
        subroutineOut.add("\t".repeat(indent) + "<symmbol> ) </symbol>");
        
        subroutineOut.add("\t".repeat(indent) + "<subroutineBody>");
        indent += 1;
        subroutineOut.add("\t".repeat(indent) + "<symbol> { </symbol>");

        int currLine = 0;
        while(!getTokenValue(tokens.get(currLine)).equals("{")) {
            currLine++;
        }
        currLine++;

        while(currLine < tokens.size()) {
            if(getTokenClass(tokens.get(currLine)).equals("keyword") && getTokenValue(tokens.get(currLine)).equals("var")) {
                ArrayList<String> varDecTokens = getStatement(tokens, currLine);
                currLine += varDecTokens.size();
                subroutineOut.addAll(compileVarDec(varDecTokens));
            }
            else {
                break;
            }
        }

        //TODO: Add compile statements
        subroutineOut.add("\t".repeat(indent) + "<symbol> } </symbol>");
        indent -= 1;
        subroutineOut.add("\t".repeat(indent) + "</subroutineBody>");
        indent -=1;
        subroutineOut.add("\t".repeat(indent) + "</subroutineDec>");
        
        return subroutineOut;       
    }

    ArrayList<String> compileVarDec(ArrayList<String> tokens) {
        ArrayList<String> varDecOut = new ArrayList<>();
        varDecOut.add("\t".repeat(indent) + "<varDec>");
        indent += 1;
        varDecOut.add("\t".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        String varType = getTokenValue(tokens.get(1));
        if(varType.equals("int") || varType.equals("char") || varType.equals("boolean")) {
            varDecOut.add("\t".repeat(indent) + "<keyword> " + varType + " </keyword>");
        }
        else {
            varDecOut.add("\t".repeat(indent) + "<identifier> " + varType + " </identifier>");
        }
        int tokenNo = 2;
        while(!getTokenValue(tokens.get(tokenNo)).equals(";")) {
            varDecOut.add("\t".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(tokenNo)) + " </identifier>");
            tokenNo += 1;
            if(!getTokenValue(tokens.get(tokenNo)).equals(";")) {
                varDecOut.add("\t".repeat(indent) + "<symbol> , </symbol>");
                tokenNo += 1;
            }
        }
        varDecOut.add("\t".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        varDecOut.add("\t".repeat(indent) + "</varDec>");
        return varDecOut;
    }

    /*
     * Helper functions
    */

    void applyIndent(ArrayList<String> tokens) {
        for(int i = 0; i < tokens.size(); i++) {
            tokens.set(i, "\t".repeat(indent) + tokens.get(i));
        }
        indent--;
    }

    void addWithIndent(ArrayList<String> tokens, String token) {
        tokens.add("\t".repeat(indent) + "\t".repeat(indent) + token);
    }

    void addAllWithIndent(ArrayList<String> tokens, ArrayList<String> newTokens) {
        for(int i = 0; i < newTokens.size(); i++) {
            tokens.add("\t".repeat(indent) + "\t".repeat(indent) + newTokens.get(i));
        }
    }
    
    String getTokenClass(String token) {
        // System.out.println("Class " + token.substring(token.indexOf("<") + 1, token.indexOf(">")).replace("/", "").trim());
        return token.substring(token.indexOf("<") + 1, token.indexOf(">")).replace("/", "").trim();
    }

    String getTokenValue(String token) {
        // System.out.println("Val " +  token.substring(token.indexOf(">") + 1, token.indexOf("</")).trim());
        return token.substring(token.indexOf(">") + 1, token.indexOf("</")).trim();
    }

    ArrayList<String> getStatement(ArrayList<String> tokens, int currLine) {
        ArrayList<String> statement = new ArrayList<>();
        while(!getTokenValue(tokens.get(currLine)).equals(";")) {
            statement.add(tokens.get(currLine));
            currLine++;
        }
        statement.add(tokens.get(currLine));
        return statement;
    }
    ArrayList<String> getScope(ArrayList<String> code ,int startLine) {
        ArrayList<String> scope = new ArrayList<>();
        int count = 1;
        for (int i = startLine; i < code.size(); i++) {
            scope.add("\t".repeat(indent) + code.get(i));

            String token = code.get(i);
            if (getTokenClass(token).equals("keyword") && getTokenValue(token).equals("{")) {
                count++;
            } else if (getTokenClass(token).equals("keyword") && getTokenValue(token).equals("}")) {
                count--;
            }
            if (count == 0) {
                break;
            }
        }
        return scope;
    }
}