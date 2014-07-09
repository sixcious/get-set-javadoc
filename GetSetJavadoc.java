/*
 * Copyright (c) 2014 Roy Six
 */

import java.beans.Introspector;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code GetSetJavadoc} adds Javadoc comments for getters and setters found in Java classes.
 * <p/>
 * For usage and examples, see the accompanying README.MD file.
 *
 * @author  Roy Six
 * @version 1.0
 */
public class GetSetJavadoc {

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.print("\n\tWalking file tree...\n\n");
        walkFileTree();
    }

    /**
     * Walks the file tree (recursively) of the "src" directory and writes to each file.
     */
    private static void walkFileTree() {
        try {
            Files.walkFileTree(Paths.get("src"), new SimpleFileVisitor<Path>() { // Java 7 Method -- 8 simpler walk method
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.format("%-50s", "\tWriting: " + file + "... ");
                    writeFile(file.toString());
                    System.out.print("Done!\n");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e) {
            System.err.print(e.getMessage() + "\n");
            System.exit(1);
        }
    }

    /**
     * Writes the getter and setter comments to the file.
     *
     * @param  fileName file name and path
     * @throws IOException
     */
    private static void writeFile(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, false))) {
            String in = "    ";
            String ls = System.lineSeparator();
            Pattern getp = Pattern.compile("(?<=.*get).*(?=\\(.*\\{)"); // still finds if statements and such with curly braces!
            Pattern setp = Pattern.compile("(?<=.*set).*(?=\\(.*\\{)");
            for (int i = 0; i < lines.size(); i++) {
                if (i - 1 > 0 && !lines.get(i - 1).contains("*/")) { // In case this get/set method isn't already commented
                    Matcher getm = getp.matcher(lines.get(i)); // Get:
                    if (getm.find()) {
                        String field = Introspector.decapitalize(getm.group());
                        writer.write(in + "/**" + ls +
                                     in + " * Gets {@link #" + field + "}." + ls +
                                     in + " * " + ls +
                                     in + " * @return {@link #" + field + "}" + ls +
                                     in + " */" + ls);
                    }
                    Matcher setm = setp.matcher(lines.get(i)); // Set:
                    if (setm.find()) {
                        String field = Introspector.decapitalize(setm.group());
                        writer.write(in + "/**" + ls +
                                     in + " * Sets {@link #" + field + "}." + ls +
                                     in + " * " + ls +
                                     in + " * @param " + field + " {@link #" + field + "}" + ls +
                                     in + " */" + ls);
                    }
                }
                writer.write(lines.get(i) + i != ls.size() - 1 ? ls : ""); // Write the original line
            }
        }
    }

}
