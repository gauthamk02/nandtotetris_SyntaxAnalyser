import java.io.*;
import java.util.*;

public class SyntaxAnalyser {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Argument Error!\nUsage: java VirtualMachine <inputfile>");
            return;
        }

        String inFilename = args[0];

        String outFilename = inFilename.substring(0, inFilename.lastIndexOf('.')) + ".xml";
        File infile = new File(inFilename);
        Scanner filein;
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<String> xmlFile = new ArrayList<>();

        try {
            filein = new Scanner(infile);
            while (filein.hasNextLine()) {
                String line = filein.nextLine();

                // Remove inline comments and and skip comment lines
                if (line.startsWith("//") || line.isEmpty())
                    continue;
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                }

                //TODO: Check if replace function is necessary
                tokens.add(line.replaceAll("[\\s&&[^ ]]", ""));
            }

            Parser parser = new Parser(tokens);

            xmlFile = parser.parse();
            for(String line : xmlFile) {
                System.out.println(line);
            }
            //writetoFile(xmlFile, outFilename);

        } catch (IOException e) {
            System.out.println(e);
            System.out.println(e.getStackTrace());
            return;
        }

        System.out.println("Tokens stored at " + outFilename);
        filein.close();
    }

    static void writetoFile(ArrayList<String> list, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        for (String str : list) {
            fw.write(str + System.lineSeparator());
        }

        fw.close();
    }

    static void printFile(ArrayList<String> file) {
        for (String str : file)
            System.out.println(str);
        System.out.println();
    }
}