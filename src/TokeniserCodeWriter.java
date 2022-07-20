import java.util.ArrayList;
import java.util.Arrays;

public class TokeniserCodeWriter {
    ArrayList<String> tokenizer_xml(ArrayList<String> arrayList) {
        String[] keywordsStrings = { "class", "constructor", "function", "method", "field", "static", "var", "int",
                "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
                "return" };
        String[] symbolStrings = { "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">",
                "=", "~" };

        ArrayList<String> output = new ArrayList<>();

        for (String i : arrayList) {
            if (Arrays.asList(keywordsStrings).contains(i)) {
                output.add(keyword_tokenize(i));
            } else if (Arrays.asList((symbolStrings)).contains(i)) {
                switch (i) {
                    case "<":
                        output.add(symbols_tokenize("&lt;"));
                        continue;
                    case ">":
                        output.add(symbols_tokenize("&gt;"));
                        continue;
                    case "&":
                        output.add(symbols_tokenize("&amp;"));
                        continue;
                    default:
                        output.add(symbols_tokenize(i));
                        continue;
                }
            } else if (i.startsWith("\"")) {
                output.add(string_tokenize(i.substring(1, i.lastIndexOf("\""))));
            } else if (checkint(i)) {
                output.add(integer_tokenize(i));
            } else {
                output.add(identifier_tokenize(i));
            }
        }
        return output;
    }

    String keyword_tokenize(String keyword) {
        return "<keyword> " + keyword + " </keyword>";
    }

    String identifier_tokenize(String identifier) {
        return "<identifier> " + identifier + " </identifier>";
    }

    String symbols_tokenize(String symbol) {
        return "<symbol> " + symbol + " </symbol>";
    }

    String string_tokenize(String string) {
        return "<stringConstant> " + string + " </stringConstant>";
    }

    String integer_tokenize(String integer) {
        return "<integerConstant> " + integer + " </integerConstant>";
    }

    boolean checkint(String num) {
        try {
            Double.parseDouble(num);
            return true;

        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
