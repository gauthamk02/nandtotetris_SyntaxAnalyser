import java.io.*;
import java.util.*;

public class SyntaxAnalyser {
    public static void main(String args[]) {

        if (args.length != 1) {
            System.out.println("Argument Error!\nUsage: java SyntaxAnalyser <input file/folder>");
            return;
        }

        String argFilename = args[0];
        File argFile = new File(argFilename);

        if (argFile.isFile()) {

            String inFilename = argFile.getPath();
            String outFilename = inFilename.substring(0, inFilename.lastIndexOf('.')) + "Output.xml";
            ArrayList<String> jackFile = readfile(inFilename);

            Tokeniser tokeniser = new Tokeniser(jackFile);
            ArrayList<String> tokens = tokeniser.tokenise();

            Parser parser = new Parser(tokens);
            ArrayList<String> outFile = parser.parse();

            writetoFile(outFile, outFilename);
            System.out.println("Output File stored at " + outFilename);

        } else if (argFile.isDirectory()) {
            File[] files = argFile.listFiles();
            for (File file : files) {
                if (file.getPath().endsWith(".jack")) {

                    String inFilename = file.getPath();
                    String outFilename = inFilename.substring(0, inFilename.lastIndexOf('.')) + "Output.xml";
                    ArrayList<String> jackFile = readfile(inFilename);

                    Tokeniser tokeniser = new Tokeniser(jackFile);
                    ArrayList<String> tokens = tokeniser.tokenise();

                    Parser parser = new Parser(tokens);
                    ArrayList<String> outFile = parser.parse();

                    writetoFile(outFile, outFilename);
                    System.out.println("Output File stored at " + outFilename);
                }
            }
        } else {
            System.out.println("File/Folder Error!\nUsage: java SyntaxAnalyser <input file/folder>");
        }
    }

    static ArrayList<String> readfile(String fileName) {

        File infile = new File(fileName);
        Scanner filein;
        ArrayList<String> jackFile = new ArrayList<>();

        try {
            filein = new Scanner(infile);
            boolean blockComment = false;
            while (filein.hasNextLine()) {
                String line = filein.nextLine();

                // Remove inline comments and and skip comment lines and block comments
                if (line.trim().startsWith("//") || line.isEmpty())
                    continue;
                if (line.trim().startsWith("/*")) {
                    if (!line.trim().endsWith("*/")) {
                        blockComment = true;
                    }
                    continue;
                }
                if (blockComment) {
                    if (line.trim().endsWith("*/")) {
                        blockComment = false;
                    }
                    continue;
                }
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                }

                jackFile.add(line.replaceAll("[\\s&&[^ ]]", ""));
            }

            filein.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            System.exit(1);
        }

        return jackFile;
    }

    static void writetoFile(ArrayList<String> list, String filename) {
        try {
            FileWriter fw = new FileWriter(filename);
            for (String str : list) {
                fw.write(str + System.lineSeparator());
            }

            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    static void printFile(ArrayList<String> file) {
        for (String str : file)
            System.out.println(str);
        System.out.println();
    }
}