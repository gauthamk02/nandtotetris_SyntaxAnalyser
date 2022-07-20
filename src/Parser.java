import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

    static String[] subroutinedec = { "constructor", "function", "method" };
    static String[] classvardec = { "static", "field" };
    static String[] vartype = { "int", "String", "char" };
    static String[] termSeparators = { "+", "-", "*", "/", "&", "|", "<", ">", "=", ";", ",", ")", "]", "&lt;", "&gt;",
            "&amp;", "&amp;&amp;" };
    static String[] closingSymbols = { ")", "}", "]", ";" };
    static String[] unaryOperators = { "-", "~" };
    private ArrayList<String> input;
    private int indent = 0;

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
        for (int currLine = 3; currLine < tokens.size(); currLine++) {
            String tokenClass = getTokenClass(tokens.get(currLine));
            String tokenVal = getTokenValue(tokens.get(currLine));

            if (tokenClass.equals("keyword")
                    && (tokenVal.equals("constructor")
                            || tokenVal.equals("function") || tokenVal.equals("method"))) {
                ArrayList<String> scope = getScope(tokens, currLine);
                currLine += scope.size() - 1;
                classOut.addAll(compileSubroutine(scope));
            } else if (tokenClass.equals("keyword")
                    && (tokenVal.equals("field") || tokenVal.equals("static"))) {
                ArrayList<String> scope = getStatement(tokens, currLine);
                currLine += scope.size() - 1;
                classOut.addAll(compileClassVarDec(scope));
            } else if (tokenClass.equals("symbol")
                    && tokenVal.equals("}")) {
                classOut.add("  ".repeat(indent) + tokens.get(currLine));
                break;
            } else {
                System.out.println("Error: Expected keyword constructor, function, or method");
                return null;
            }
        }

        indent -= 1;

        return classOut;
    }

    ArrayList<String> compileSubroutine(ArrayList<String> tokens) {
        ArrayList<String> subroutineOut = new ArrayList<>();
        // if(token0Class.equals("keyword") ||
        // !token0Val.equals("constructor") ||
        // token0Val.equals("function") ||
        // token0Val.equals("method")) {
        // System.out.println("Error: Expected keyword constructor, function, or
        // method");
        // return null;
        // }

        subroutineOut.add("  ".repeat(indent) + "<subroutineDec>");
        indent += 1;
        subroutineOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        if (getTokenValue(tokens.get(0)).equals("constructor")) {
            subroutineOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(1)) + " </identifier>");
        } else
            subroutineOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(1)) + " </keyword>");
        subroutineOut.add("  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(2)) + " </identifier>");

        subroutineOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
        ArrayList<String> parameterTokens = new ArrayList<>();
        for (int i = 4; !getTokenValue(tokens.get(i)).equals(")"); i++) {
            parameterTokens.add(tokens.get(i));
        }
        subroutineOut.addAll(compileParameterList(parameterTokens));
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

        subroutineBodyOut.addAll(compileStatements(new ArrayList(tokens.subList(currLine, tokens.size()))));
        subroutineBodyOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        indent -= 1;
        subroutineBodyOut.add("  ".repeat(indent) + "</subroutineBody>");
        return subroutineBodyOut;
    }

    ArrayList<String> compileParameterList(ArrayList<String> tokens) {
        ArrayList<String> parameterListOut = new ArrayList<>();
        parameterListOut.add("  ".repeat(indent) + "<parameterList>");
        for (int i = 0; i < tokens.size(); i++) {
            parameterListOut.add("  ".repeat(indent) + tokens.get(i));
        }
        parameterListOut.add("  ".repeat(indent) + "</parameterList>");
        return parameterListOut;
    }

    ArrayList<String> compileClassVarDec(ArrayList<String> tokens) {
        ArrayList<String> classVarDecOut = new ArrayList<>();
        String feildType = getTokenValue(tokens.get(1));
        classVarDecOut.add("  ".repeat(indent) + "<classVarDec>");
        indent += 1;
        classVarDecOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        if (feildType.equals("int") || feildType.equals("char") || feildType.equals("boolean")) {
            classVarDecOut.add("  ".repeat(indent) + "<keyword> " + feildType + " </keyword>");
        } else {
            classVarDecOut.add("  ".repeat(indent) + "<identifier> " + feildType + " </identifier>");
        }
        for (int i = 2; !getTokenValue(tokens.get(i)).equals(";"); i++) {
            classVarDecOut.add("  ".repeat(indent) + tokens.get(i));
        }
        classVarDecOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        classVarDecOut.add("  ".repeat(indent) + "</classVarDec>");
        return classVarDecOut;
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
        int tokenNo = 3;
        if (getTokenValue(tokens.get(2)).equals("[")) {
            letOut.add("  ".repeat(indent) + "<symbol> [ </symbol>");
            tokenNo += 1;
            indent += 1;
            ArrayList<String> expression = getExpression(tokens, 3);
            tokenNo += expression.size();
            letOut.addAll(compileExpression(expression, 0));
            indent -= 1;
            letOut.add("  ".repeat(indent) + "<symbol> ] </symbol>");
            letOut.add("  ".repeat(indent) + "<symbol> = </symbol>");
            ArrayList<String> expression2 = getExpression(tokens, tokenNo + 1);
            letOut.addAll(compileExpression(expression2, 0));
            letOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
            indent -= 1;
            letOut.add("  ".repeat(indent) + "</letStatement>");
        } else {
            letOut.add("  ".repeat(indent) + "<symbol> = </symbol>");
            ArrayList<String> expression = getExpression(tokens, tokenNo);
            letOut.addAll(compileExpression(expression, 0));
            letOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
            indent -= 1;
            letOut.add("  ".repeat(indent) + "</letStatement>");
        }
        return letOut;
    }

    ArrayList<String> compileDo(ArrayList<String> token) {
        int startInd = 0;
        while (!getTokenValue(token.get(startInd)).equals("(")) {
            startInd++;
        }
        startInd++;
        ArrayList<String> doOut = new ArrayList<>();
        doOut.add("  ".repeat(indent) + "<doStatement>");
        indent += 1;
        doOut.add("  ".repeat(indent) + "<keyword> do </keyword>");
        ArrayList<String> functionCall = new ArrayList<>(token.subList(1, token.size() - 1));
        doOut.addAll(compileTerm(functionCall, true));
        doOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        doOut.add("  ".repeat(indent) + "</doStatement>");
        return doOut;
    }

    ArrayList<String> compileIf(ArrayList<String> ifTokens, ArrayList<String> elseTokens) {
        ArrayList<String> ifOut = new ArrayList<>();
        ifOut.add("  ".repeat(indent) + "<ifStatement>");
        indent += 1;
        ifOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(ifTokens.get(0)) + " </keyword>");
        ifOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
        int tokenNo = 1;
        ArrayList<String> expression = getExpression(ifTokens, tokenNo + 1);
        tokenNo += expression.size();
        ifOut.addAll(compileExpression(expression, 0));
        ifOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
        ifOut.add("  ".repeat(indent) + "<symbol> { </symbol>");
        indent += 1;
        ArrayList<String> statements = new ArrayList(ifTokens.subList(tokenNo + 1, ifTokens.size() - 1));
        tokenNo += statements.size();
        ifOut.addAll(compileStatements(statements));
        indent -= 1;
        ifOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        if(elseTokens.size() > 0) {
            ifOut.add("  ".repeat(indent) + "<keyword> else </keyword>");
            ifOut.add("  ".repeat(indent) + "<symbol> { </symbol>");
            indent += 1;
            ArrayList<String> statements2 = new ArrayList(elseTokens.subList(1, elseTokens.size() - 1));
            ifOut.addAll(compileStatements(statements2));
            indent -= 1;
            ifOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        }
        ifOut.add("  ".repeat(indent) + "</ifStatement>");
        return ifOut;
    }

    ArrayList<String> compileWhile(ArrayList<String> tokens) {
        ArrayList<String> whileOut = new ArrayList<>();
        whileOut.add("  ".repeat(indent) + "<whileStatement>");
        indent += 1;
        whileOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        whileOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
        ArrayList<String> expressionTokens = getExpression(tokens, 2);
        whileOut.addAll(compileExpression(expressionTokens, 0));
        whileOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
        whileOut.add("  ".repeat(indent) + "<symbol> { </symbol>");
        whileOut.addAll(compileStatements(new ArrayList(tokens.subList(3, tokens.size()))));
        whileOut.add("  ".repeat(indent) + "<symbol> } </symbol>");
        indent -= 1;
        whileOut.add("  ".repeat(indent) + "</whileStatement>");
        return whileOut;
    }

    ArrayList<String> compileReturn(ArrayList<String> tokens) {
        ArrayList<String> returnOut = new ArrayList<>();
        returnOut.add("  ".repeat(indent) + "<returnStatement>");
        indent += 1;
        returnOut.add("  ".repeat(indent) + "<keyword> " + getTokenValue(tokens.get(0)) + " </keyword>");
        if (tokens.size() > 2) {
            ArrayList<String> expressionTokens = getExpression(tokens, 1);
            returnOut.addAll(compileExpression(expressionTokens, 0));
        }
        returnOut.add("  ".repeat(indent) + "<symbol> ; </symbol>");
        indent -= 1;
        returnOut.add("  ".repeat(indent) + "</returnStatement>");
        return returnOut;
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
                ArrayList<String> elseTokens = new ArrayList<>();
                currLine += ifTokens.size();
                if (currLine < tokens.size()) {
                    if (getTokenClass(tokens.get(currLine)).equals("keyword")
                            && getTokenValue(tokens.get(currLine)).equals("else")) {
                        elseTokens = getScope(tokens, currLine);
                        // ifTokens.addAll(elseTokens);
                        currLine += elseTokens.size();
                    }
                }
                statementsOut.addAll(compileIf(ifTokens, elseTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("while")) {
                ArrayList<String> whileTokens = getScope(tokens, currLine);
                currLine += whileTokens.size();
                statementsOut.addAll(compileWhile(whileTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("do")) {
                ArrayList<String> doTokens = getStatement(tokens, currLine);
                currLine += doTokens.size();
                statementsOut.addAll(compileDo(doTokens));
            } else if (tokenKeyword.equals("keyword")
                    && tokenVal.equals("return")) {
                ArrayList<String> returnTokens = getStatement(tokens, currLine);
                currLine += returnTokens.size();
                statementsOut.addAll(compileReturn(returnTokens));
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
        boolean isUnaryop = false;
        expressionOut.add("  ".repeat(indent) + "<expression>");
        indent += 1;
        ArrayList<String> termTokens = getTerm(expressionTokens, 0);
        int currToken = termTokens.size();
        if (getTokenClass(termTokens.get(0)).equals("symbol")
                && Arrays.asList(unaryOperators).contains(getTokenValue(termTokens.get(0)))) {
            indent += 1;
            isUnaryop = true;
            currToken += 1;
            expressionOut.add("  ".repeat(indent) + "<term>");
            expressionOut.add("  ".repeat(indent) + "<symbol> " + getTokenValue(termTokens.get(0)) + " </symbol>");
            termTokens.remove(0);
        }

        expressionOut.addAll(compileTerm(termTokens, false));
        while (currToken < expressionTokens.size()) {
            String tokenClass = getTokenClass(expressionTokens.get(currToken));
            String tokenVal = getTokenValue(expressionTokens.get(currToken));

            if (Arrays.asList(termSeparators).contains(tokenVal)
                    && !Arrays.asList(closingSymbols).contains(tokenVal)) {
                expressionOut.add("  ".repeat(indent) + "<symbol> " + tokenVal + " </symbol>");
                currToken += 1;
                continue;
            }

            if (!Arrays.asList(closingSymbols).contains(tokenVal)) {
                ArrayList<String> termTokens2 = getTerm(expressionTokens, currToken);
                currToken += termTokens2.size();
                expressionOut.addAll(compileTerm(termTokens2, false));
            } else if (isEndOfExpression(expressionTokens.get(currToken))) {
                break;
            } else {
                System.out.println("Error: invalid expression, urecognised symbol: " + expressionTokens.get(currToken));
                break;
            }
        }
        if (isUnaryop) {
            indent -= 1;
            expressionOut.add("  ".repeat(indent) + "</term>");
        }
        indent -= 1;
        expressionOut.add("  ".repeat(indent) + "</expression>");
        return expressionOut;
    }

    ArrayList<String> compileExpressionList(ArrayList<String> expressionList) {
        ArrayList<String> expresListOut = new ArrayList<>();
        expresListOut.add("  ".repeat(indent) + "<expressionList>");
        indent += 1;
        int currLine = 0;
        while (currLine < expressionList.size()) {
            if (getTokenClass(expressionList.get(currLine)).equals("symbol")
                    && getTokenValue(expressionList.get(currLine)).equals(",")) {
                expresListOut.add("  ".repeat(indent) + "<symbol> , </symbol>");
                currLine += 1;
            } else if (getTokenClass(expressionList.get(currLine)).equals("symbol")
                    && getTokenValue(expressionList.get(currLine)).equals("=")) {
                expresListOut.add("  ".repeat(indent) + "<symbol> = </symbol>");
                currLine += 1;
            } else {
                ArrayList<String> expressionTokens = getExpression(expressionList, currLine);
                expresListOut.addAll(compileExpression(expressionTokens, 0));
                currLine += expressionTokens.size();
            }
        }
        indent -= 1;
        expresListOut.add("  ".repeat(indent) + "</expressionList>");
        return expresListOut;
    }

    ArrayList<String> compileTerm(ArrayList<String> tokens, boolean avoidTermTag) {
        ArrayList<String> termOut = new ArrayList<>();
        String token0Class = getTokenClass(tokens.get(0));
        String token0Val = getTokenValue(tokens.get(0));

        if (!avoidTermTag)
            termOut.add("  ".repeat(indent) + "<term>");
        indent += 1;
        if (token0Class.equals("integerConstant")) {
            termOut.add(
                    "  ".repeat(indent) + "<integerConstant> " + token0Val + " </integerConstant>");
        } else if (token0Class.equals("stringConstant")) {
            termOut.add(
                    "  ".repeat(indent) + "<stringConstant> " + token0Val + " </stringConstant>");
        } else if (token0Class.equals("keyword") && token0Val.equals("true")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + token0Val + " </keyword>");
        } else if (token0Class.equals("keyword") && token0Val.equals("false")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + token0Val + " </keyword>");
        } else if (token0Class.equals("keyword") && token0Val.equals("null")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + token0Val + " </keyword>");
        } else if (token0Class.equals("keyword") && token0Val.equals("this")) {
            termOut.add("  ".repeat(indent) + "<keyword> " + token0Val + " </keyword>");
        } else if (token0Class.equals("identifier")) {

            if (tokens.size() > 1) {
                if (getTokenValue(tokens.get(1)).equals("[")) {
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + token0Val + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> [ </symbol>");
                    termOut.addAll(compileExpression(tokens, 2));
                    termOut.add("  ".repeat(indent) + "<symbol> ] </symbol>");
                } else if (getTokenValue(tokens.get(1)).equals("(")) {
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + token0Val + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
                    ArrayList<String> expressions = getExpression(tokens, 2);
                    termOut.addAll(compileExpressionList(expressions));
                    termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
                } else if (getTokenValue(tokens.get(1)).equals(".")) {
                    ArrayList<String> expressionList = new ArrayList<>();
                    int bracketCount = 0;
                    for (int i = 4; i < tokens.size(); i++) {
                        if (getTokenValue(tokens.get(i)).equals("(")) {
                            bracketCount += 1;
                        }
                        if (getTokenValue(tokens.get(i)).equals(")")) {
                            if (bracketCount == 0)
                                break;
                            bracketCount -= 1;
                        }
                        expressionList.add(tokens.get(i));
                    }
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + token0Val + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> . </symbol>");
                    termOut.add(
                            "  ".repeat(indent) + "<identifier> " + getTokenValue(tokens.get(2)) + " </identifier>");
                    termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
                    termOut.addAll(compileExpressionList(expressionList));
                    termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
                } else {
                    System.out.println("Error: " + tokens.get(1) + " is not a valid term");
                    return null;
                }
            } else {
                termOut.add("  ".repeat(indent) + "<identifier> " + token0Val + " </identifier>");
            }
        } else if (token0Class.equals("symbol") && token0Val.equals("(")) {
            termOut.add("  ".repeat(indent) + "<symbol> ( </symbol>");
            ArrayList<String> expressions = getExpression(tokens, 1);
            termOut.addAll(compileExpression(expressions, 0));
            termOut.add("  ".repeat(indent) + "<symbol> ) </symbol>");
        }
        indent -= 1;
        if (!avoidTermTag)
            termOut.add("  ".repeat(indent) + "</term>");
        return termOut;
    }

    /*
     * Helper functions
     */

    boolean isEndOfExpression(String token) {
        token = getTokenValue(token);
        return token.equals(")") || token.equals("]") || token.equals(",") || token.equals(";");
    }

    ArrayList<String> getTerm(ArrayList<String> expression, int startIndex) {
        ArrayList<String> term = new ArrayList<>();
        int bracketCount = 0;
        if (expression.size() == 0)
            return term;
        if (Arrays.asList(unaryOperators).contains(getTokenValue(expression.get(startIndex)))) {
            term.add(expression.get(startIndex));
            startIndex += 1;
            term.addAll(getTerm(expression, startIndex));
            return term;
        }
        for (int i = startIndex; i < expression.size(); i++) {
            String tokenClass = getTokenClass(expression.get(i));
            String tokenValue = getTokenValue(expression.get(i));
            if (tokenClass.equals("symbol") && (tokenValue.equals("(") || tokenValue.equals("["))) {
                bracketCount += 1;
            }

            if (tokenClass.equals("symbol")
                    && Arrays.asList(termSeparators).contains(tokenValue)) {
                if ((tokenValue.equals(")") || tokenValue.equals("]")) && bracketCount > 0) {
                    term.add(expression.get(i));
                    bracketCount -= 1;
                    continue;
                }
                if (bracketCount > 0) {
                    term.add(expression.get(i));
                    continue;
                }
                break;
            } else {
                term.add(expression.get(i));
            }
        }
        return term;
    }

    ArrayList<String> getExpression(ArrayList<String> expression, int startIndex) {
        String[] expressionSeparators = { ",", ";", ")", "]" };
        ArrayList<String> expressionOut = new ArrayList<>();
        int bracketCount = 0;

        for (int i = startIndex; i < expression.size(); i++) {
            String tokenClass = getTokenClass(expression.get(i));
            String tokenValue = getTokenValue(expression.get(i));
            if (tokenClass.equals("symbol") && (tokenValue.equals("(") || tokenValue.equals("["))) {
                bracketCount += 1;
            }
            if (tokenClass.equals("symbol")
                    && Arrays.asList(expressionSeparators).contains(tokenValue)) {
                if ((tokenValue.equals(")") || tokenValue.equals("]")) && bracketCount > 0) {
                    expressionOut.add(expression.get(i));
                    bracketCount -= 1;
                    continue;
                }
                if (tokenValue.equals(",") && bracketCount > 0) {
                    expressionOut.add(expression.get(i));
                    continue;
                }
                break;
            } else {
                expressionOut.add(expression.get(i));
            }
        }
        return expressionOut;
    }

    ArrayList<String> getExpressionList(ArrayList<String> tokens, int startToken) {
        ArrayList<String> expressionList = new ArrayList<>();
        int bracketCount = 0;
        for (int i = startToken; i < tokens.size(); i++) {
            String tokenClass = getTokenClass(tokens.get(i));
            String tokenValue = getTokenValue(tokens.get(i));
            if (tokenClass.equals("symbol") && (tokenValue.equals("(") || tokenValue.equals("["))) {
                bracketCount += 1;
            }
            if (tokenClass.equals("symbol")
                    && Arrays.asList(termSeparators).contains(tokenValue)) {
                if ((tokenValue.equals(")") || tokenValue.equals("]")) && bracketCount > 0) {
                    expressionList.add(tokens.get(i));
                    bracketCount -= 1;
                    continue;
                }
                if (bracketCount > 0) {
                    expressionList.add(tokens.get(i));
                    continue;
                }
                break;
            } else {
                expressionList.add(tokens.get(i));
            }
        }
        return expressionList;
    }

    String getTokenClass(String token) {
        return token.substring(token.indexOf("<") + 1, token.indexOf(">")).replace("/", "").trim();
    }

    String getTokenValue(String token) {
        if (getTokenClass(token).equals("stringConstant")) {
            return token.substring(token.indexOf("> ") + 2, token.lastIndexOf(" </"));
        } else {
            return token.substring(token.indexOf(">") + 1, token.indexOf("</")).trim();
        }
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
                count++;
                flag = true;
            } else if (getTokenClass(token).equals("symbol") && getTokenValue(token).equals("}")) {
                count--;
            }
            if (count == 0 && flag) {
                break;
            }
        }
        return scope;
    }
}