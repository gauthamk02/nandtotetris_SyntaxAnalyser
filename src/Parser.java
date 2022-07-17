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
        for (int i = 0; i < input.size(); i++) {
            String line = input.get(i);
            if (line.contains("class")) {
                output.add("  ".repeat(indent) + "<class>");
                output.addAll(compileClass(getScope(input, i)));
                output.add("  ".repeat(indent) + "</class>");
            }
        }
        return output;
    }

    ArrayList<String> compileClass(ArrayList<String> tokens) {
        indent += 1;
        ArrayList<String> classOut = new ArrayList<>();
        if (!getTokenClass(tokens.get(0)).equals("keyword") && !getTokenValue(tokens.get(0)).equals("class")) {
            System.out.println("Error: Expected keyword class");
            return null;
        }
        classOut.add("  ".repeat(indent) + tokens.get(0));
        classOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(1)) + " </identifier>");
        if (!getTokenClass(tokens.get(2)).equals("symbol") && !getTokenValue(tokens.get(2)).equals("{")) {
            System.out.println("Error: Expected symbol {");
            return null;
        }
        classOut.add("  ".repeat(indent) + tokens.get(2));
        // currLine += 3;
        for (int currLine = 3; currLine < tokens.size(); currLine++) {
            if (getTokenClass(tokens.get(currLine)).equals("keyword")
                    && getTokenValue(tokens.get(currLine)).equals("constructor")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            } else if (getTokenClass(tokens.get(currLine)).equals("keyword")
                    && getTokenValue(tokens.get(currLine)).equals("function")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            } else if (getTokenClass(tokens.get(currLine)).equals("keyword")
                    && getTokenValue(tokens.get(currLine)).equals("method")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            } else if (getTokenClass(tokens.get(currLine)).equals("keyword")
                    && getTokenValue(tokens.get(currLine)).equals("var")) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size();
                classOut.addAll(compileSubroutine(scope));
            } else if (getTokenClass(tokens.get(currLine)).equals("symbol")
                    && getTokenValue(tokens.get(currLine)).equals("}")) {
                classOut.add("  ".repeat(indent) + tokens.get(currLine));
                break;
            } else {
                System.out.println("Error: Expected keyword constructor, function, or method");
                return null;
            }
        }

        classOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        indent -= 1;
        return classOut;
    }

    ArrayList<String> compileSubroutine(ArrayList<String> tokens) {
        ArrayList<String> subroutineOut = new ArrayList<>();
        // if(getTokenClass(tokens.get(0)).equals("keyword") ||
        // !getTokenValue(tokens.get(0)).equals("constructor") ||
        // getTokenValue(tokens.get(0)).equals("function") ||
        // getTokenValue(tokens.get(0)).equals("method")) {
        // System.out.println("Error: Expected keyword constructor, function, or
        // method");
        // return null;
        // }

        subroutineOut.add("  ".repeat(indent) + "<subroutineDec>");
        indent += 1;
        // subroutineOut.addAll(tokens.subList(0, 4));
        subroutineOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        subroutineOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(1)) + " </keyword>");
        subroutineOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(2)) + " </identifier>");

        subroutineOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
        subroutineOut.add("  ".repeat(indent) + "<parameterList>");
        indent += 1;
        // TODO: Add parameter list
        indent -= 1;
        subroutineOut.add("  ".repeat(indent) + "</parameterList>");
        subroutineOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");

        subroutineOut.addAll(compileSubroutineBody(tokens));
        indent -= 1;
        subroutineOut.add("  ".repeat(indent) + "</subroutineDec>");

        return subroutineOut;
    }

    ArrayList<String> compileSubroutineBody(ArrayList<String> tokens) {
        ArrayList<String> subroutineBodyOut = new ArrayList<>();
        subroutineBodyOut.add("  ".repeat(indent) + "<subroutineBody>");
        indent += 1;
        subroutineBodyOut.add("  ".repeat(indent) + "<symbol> { </symbol>");

        int currLine = 0;
        while (!getTokenValue(tokens.get(currLine)).equals("{")) {
            currLine++;
        }
        currLine++;

        while (currLine < tokens.size()) {
            if (getTokenClass(tokens.get(currLine)).equals("keyword")
                    && getTokenValue(tokens.get(currLine)).equals("var")) {
                ArrayList<String> varDecTokens = getStatement(tokens, currLine);
                currLine += varDecTokens.size();
                subroutineBodyOut.addAll(compileVarDec(varDecTokens));
            } else {
                break;
            }
        }

        // TODO: Add compile statements
        subroutineBodyOut.addAll(compileStatements(new ArrayList(tokens.subList(currLine, tokens.size()))));
        subroutineBodyOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        indent -= 1;
        subroutineBodyOut.add("  ".repeat(indent) + "</subroutineBody>");
        return subroutineBodyOut;
    }

    ArrayList<String> compileVarDec(ArrayList<String> tokens) {
        ArrayList<String> varDecOut = new ArrayList<>();
        varDecOut.add("  ".repeat(indent) + "<varDec>");
        indent += 1;
        varDecOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        String varType = getTokenValue(tokens.get(1));
        if (varType.equals("int") || varType.equals("char") || varType.equals("boolean")) {
            varDecOut.add("  ".repeat(indent) + "<keyword> " + varType + " </keyword>");
        } else {
            varDecOut.add("  ".repeat(indent) + "<identifier> " + varType + " </identifier>");
        }
        int tokenNo = 2;
        while (!getTokenValue(tokens.get(tokenNo)).equals(";")) {
            varDecOut
                    .add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(tokenNo)) + " </identifier>");
            tokenNo += 1;
            if (!getTokenValue(tokens.get(tokenNo)).equals(";")) {
                varDecOut.add("  ".repeat(indent) + "<symbol> , </symbol>");
                tokenNo += 1;
            }
        }
        varDecOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        varDecOut.add("  ".repeat(indent) + "</varDec>");
        return varDecOut;
    }

    ArrayList<String> compileLet(ArrayList<String> tokens) {
        ArrayList<String> letOut = new ArrayList<>();
        letOut.add("  ".repeat(indent) + "<letStatement>");
        indent += 1;
        letOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        letOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(1)) + " </identifier>");
        int tokenNo = 2;
        if (getTokenValue(tokens.get(tokenNo)).equals("[")) {
            letOut.add("  ".repeat(indent) + "<symbol> [ </symbol>");
            tokenNo += 1;
            letOut.add("  ".repeat(indent) + "<expression>");
            indent += 1;
            letOut.addAll(compileExpression(tokens, tokenNo));
            indent -= 1;
            letOut.add("  ".repeat(indent) + "</expression>");
            tokenNo += 1;
            letOut.add("  ".repeat(indent) + "<symbol> ] </symbol>");
        }
        letOut.add("  ".repeat(indent) + "<symbol> = </symbol>");
        tokenNo += 1;
        letOut.addAll(compileExpression(tokens, tokenNo));
        letOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        letOut.add("  ".repeat(indent) + "</letStatement>");
        return letOut;
    }

    ArrayList<String> compileStatements(ArrayList<String> tokens) {
        ArrayList<String> statementsOut = new ArrayList<>();
        statementsOut.add("  ".repeat(indent) + "<statements>");
        indent += 1;
        int currLine = 0;
        while (currLine < tokens.size()) {
            String tokenKeyword = getTokenClass(tokens.get(currLine));
            String tokenVal = getTokenValue(tokens.get(currLine));

            if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("let")) {
                ArrayList<String> letTokens = getStatement(tokens, currLine);
                currLine += letTokens.size();
                statementsOut.addAll(compileLet(letTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("if")) {
                ArrayList<String> ifTokens = getScope(tokens, currLine);
                currLine += ifTokens.size();
                // statementsOut.addAll(compileIf(ifTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("while")) {
                ArrayList<String> whileTokens = getScope(tokens, currLine);
                currLine += whileTokens.size();
                // statementsOut.addAll(compileWhile(whileTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("do")) {
                ArrayList<String> doTokens = getStatement(tokens, currLine);
                currLine += doTokens.size();
                // statementsOut.addAll(compileDo(doTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("return")) {
                ArrayList<String> returnTokens = getStatement(tokens, currLine);
                currLine += returnTokens.size();
                // statementsOut.addAll(compileReturn(returnTokens));
            } else {
                currLine += 1;
            }
        }
        indent -= 1;
        statementsOut.add("  ".repeat(indent) + "</statements>");
        return statementsOut;
    }

    ArrayList<String> compileExpression(ArrayList<String> tokens, int startToken) {
        ArrayList<String> expressionOut = new ArrayList<>();
        ArrayList<String> expressionTokens = new ArrayList<>(tokens.subList(startToken, tokens.size()));
        expressionOut.add("  ".repeat(indent) + "<expression>");
        indent += 1;
        expressionOut.addAll(compileTerm(expressionTokens));
        int currToken = 0;
        while (currToken < expressionTokens.size()) {
            if (getTokenClass(expressionTokens.get(currToken)).equals("symbol")
                    && getTokenValue(expressionTokens.get(currToken)).equals("+")) {
                expressionOut.add("  ".repeat(indent) + "<symbol> + </symbol>");
                currToken += 1;
                expressionOut.addAll(compileTerm(expressionTokens));
            } else if (getTokenClass(expressionTokens.get(currToken)).equals("symbol")
                    && getTokenValue(expressionTokens.get(currToken)).equals("-")) {
                expressionOut.add("  ".repeat(indent) + "<symbol> - </symbol>");
                currToken += 1;
                expressionOut.addAll(compileTerm(expressionTokens));
            } else {
                break;
            }
        }
        indent -= 1;
        expressionOut.add("  ".repeat(indent) + "</expression>");
        return expressionOut;
    }

    ArrayList<String> compileExpressionList(ArrayList<String> expressions) {
        ArrayList<String> expresListOut = new ArrayList<>();
        expresListOut.add("  ".repeat(indent) + "<expressionList>");
        indent += 1;
        int currExpression = 0;
        while (currExpression < expressions.size()) {
            if (getTokenClass(expressions.get(currExpression)).equals("symbol")
                    && getTokenValue(expressions.get(currExpression)).equals(",")) {
                expresListOut.add("  ".repeat(indent) + "<symbol> , </symbol>");
                currExpression += 1;
            } else {
                expresListOut.addAll(compileExpression(expressions, currExpression));
                break;
            }
        }
        indent -= 1;
        expresListOut.add("  ".repeat(indent) + "</expressionList>");
        return expresListOut;
    }

    ArrayList<String> compileTerm(ArrayList<String> tokens) {
        ArrayList<String> termOut = new ArrayList<>();
        termOut.add("  ".repeat(indent) + "<term>");
        indent += 1;
        if (getTokenClass(tokens.get(0)).equals("integerConstant")) {
            termOut.add(
                    "  ".repeat(indent) + "<integerConstant> " + getTokenValue(tokens.get(0)) + " </integerConstant>");
        } else if (getTokenClass(tokens.get(0)).equals("stringConstant")) {
            termOut.add(
                    "  ".repeat(indent) + "<stringConstant> " + getTokenValue(tokens.get(0)) + " </stringConstant>");
        } else if (getTokenClass(tokens.get(0)).equals("keyword") && getTokenValue(tokens.get(0)).equals("true")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        } else if (getTokenClass(tokens.get(0)).equals("keyword") && getTokenValue(tokens.get(0)).equals("false")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        } else if (getTokenClass(tokens.get(0)).equals("keyword") && getTokenValue(tokens.get(0)).equals("null")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        } else if (getTokenClass(tokens.get(0)).equals("identifier")) {

            if (tokens.size() > 1) {
                if (getTokenValue(tokens.get(1)).equals("[")) {
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(0)) + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> [ </symbol>");
                    termOut.addAll(compileExpression(tokens, 2));
                    termOut.add("  ".repeat(indent) + "<symbol> ] </symbol>");
                } else if (getTokenValue(tokens.get(1)).equals("(")) {
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(0)) + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
                    // termOut.addAll(compileExpressionList(tokens, 2));
                    termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
                } else if (getTokenValue(tokens.get(1)).equals(".")) {
                    ArrayList<String> expressionList = new ArrayList<>();
                    for (int i = 4; i < tokens.size(); i++) {
                        if (getTokenValue(tokens.get(i)).equals(")")) {
                            break;
                        }
                        expressionList.add(tokens.get(i));
                    }
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(0)) + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> . </symbol>");
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(2)) + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
                    termOut.addAll(compileExpressionList(expressionList));
                    termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
                }
                else {
                    System.out.println("Error: " + getTokenValue(tokens.get(0)) + " is not a valid term");
                    return null;
                }
            } else {
                termOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(0)) + " </identifier>");
            }
        } else if (getTokenClass(tokens.get(0)).equals("symbol") && getTokenValue(tokens.get(0)).equals("(")) {
            termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
            // termOut.addAll(compileExpression(tokens.subList(1, tokens.size())));
            termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
        }
        indent -= 1;
        termOut.add("  ".repeat(indent) + "</term>");
        return termOut;
    }

    /*
     * Helper functions
     */

    void applyIndent(ArrayList<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            tokens.set(i, "  ".repeat(indent) + tokens.get(i));
        }
        indent--;
    }

    void addWithIndent(ArrayList<String> tokens, String token) {
        tokens.add("  ".repeat(indent) + "  ".repeat(indent) + token);
    }

    void addAllWithIndent(ArrayList<String> tokens, ArrayList<String> newTokens) {
        for (int i = 0; i < newTokens.size(); i++) {
            tokens.add("  ".repeat(indent) + "  ".repeat(indent) + newTokens.get(i));
        }
    }

    String getTokenClass(String token) {
        return token.substring(token.indexOf("<") + 1, token.indexOf(">")).replace("/", "").trim();
    }

    String getTokenValue(String token) {
        return token.substring(token.indexOf(">") + 1, token.indexOf("</")).trim();
    }

    ArrayList<String> getStatement(ArrayList<String> tokens, int currLine) {
        ArrayList<String> statement = new ArrayList<>();
        while (!getTokenValue(tokens.get(currLine)).equals(";")) {
            statement.add(tokens.get(currLine));
            currLine++;
        }
        statement.add(tokens.get(currLine));
        return statement;
    }

    ArrayList<String> getScope(ArrayList<String> code, int startLine) {
        ArrayList<String> scope = new ArrayList<>();
        int count = 0;
        int i = startLine;
        boolean flag = false;

        for (; i < code.size(); i++) {
            scope.add("  ".repeat(indent) + code.get(i));

            String token = code.get(i);
            if (getTokenClass(token).equals("symbol") && getTokenValue(token).equals("{")) {
                // System.out.println(token);
                count++;
                flag = true;
            } else if (getTokenClass(token).equals("symbol") && getTokenValue(token).equals("}")) {
                // System.out.println(token);
                count--;
            }
            if (count == 0 && flag) {
                break;
            }
        }
        return scope;
    }
}