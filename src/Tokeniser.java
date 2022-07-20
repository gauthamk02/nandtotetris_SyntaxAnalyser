import java.util.ArrayList;

public class Tokeniser {
    private ArrayList<String> input;

    Tokeniser(ArrayList<String> inputfile) {
        input = inputfile;
    }

    ArrayList<String> tokenise() {
        ArrayList<String> tokeniserOut = new ArrayList<>();
        tokeniserOut.add("<tokens>");

        ArrayList<String> arr;
        try {

            boolean flag = false;
            for (String line : input) {
                // String line = sc.nextLine();

                // removing comments
                String str = line.trim();

                arr = new ArrayList<String>();
                String temp = "";
                boolean stringflag = false;
                char[] symbols = { '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>',
                        '=', '~' };

                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) == '\"' || stringflag == true) {
                        if (str.charAt(i) == '\"') {
                            stringflag = !stringflag;
                        } else {
                            temp += str.charAt(i);
                            continue;
                        }
                    }
                    if (str.charAt(i) == ' ') {
                        if (temp != "") {
                            arr.add(temp);
                        }
                        temp = "";
                    } else if (new String(symbols).indexOf(str.charAt(i)) != -1) {
                        if (temp != "") {
                            arr.add(temp);
                        }
                        arr.add(Character.toString(str.charAt(i)));
                        temp = "";
                    } else {
                        temp += str.charAt(i);

                    }

                }

                TokeniserCodeWriter cw = new TokeniserCodeWriter();

                tokeniserOut.addAll(cw.tokenizer_xml(arr));

                arr.clear();
            }

            tokeniserOut.add("</tokens>");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokeniserOut;
    }
}
