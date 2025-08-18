package helper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Assignment_Formatter {

    public static void main(String[] args) throws Exception {
    	@SuppressWarnings("resource")
    	Scanner scanner = new Scanner(System.in);

        // === Load base folder from config file ===
        Path configFile = Paths.get("config.txt");
        if (!Files.exists(configFile)) {
            System.out.println("Error: config.txt not found. Please create it with the base course folder path.");
            return;
        }
        String baseFolder = Files.readString(configFile).trim();

        // === Ask user for info ===
        System.out.print("Enter unit number: ");
        String unit = scanner.nextLine().trim();

        // === Paths ===
        String sourceFolder = baseFolder + "/src/cs_1102_base/Unit_" + unit;
        String outputFolder = baseFolder + "/submissions";
        String assignmentName = "Unit " + unit + " Assignment";

        // Ensure output folder exists
        Files.createDirectories(Paths.get(outputFolder));

        // Collect all .java files in the source folder
        List<String> javaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sourceFolder), "*.java")) {
            for (Path entry : stream) {
                javaFiles.add(entry.toString());
            }
        }

        if (javaFiles.isEmpty()) {
            System.out.println("No Java files found in " + sourceFolder);
            return;
        }

        // Read all source code & detect main class
        StringBuilder allSources = new StringBuilder();
        String mainClassName = null;

        for (String file : javaFiles) {
            String code = Files.readString(Paths.get(file));
            allSources.append("=== FILE: ").append(Paths.get(file).getFileName()).append(" ===\n");
            allSources.append(code).append("\n\n");

            if (code.contains("public static void main")) {
                String fileName = Paths.get(file).getFileName().toString();
                mainClassName = "cs_1102_base.Unit_" + unit + "." + fileName.replace(".java", "");
            }
        }

        if (mainClassName == null) {
            System.out.println("No main class found. Please check your files.");
            return;
        }

        // Compile
        List<String> compileCommand = new ArrayList<>();
        compileCommand.add("javac");
        compileCommand.addAll(javaFiles);

        ProcessBuilder compilePb = new ProcessBuilder(compileCommand);
        compilePb.directory(new File(sourceFolder));
        compilePb.redirectErrorStream(true);
        Process compileProcess = compilePb.start();
        String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
        compileProcess.waitFor();

        // Run
        ProcessBuilder runPb = new ProcessBuilder("java", "-cp", baseFolder + "/src", mainClassName);
        runPb.redirectErrorStream(true);
        Process runProcess = runPb.start();
        String runOutput = new String(runProcess.getInputStream().readAllBytes());
        runProcess.waitFor();

        // Build report
        StringBuilder report = new StringBuilder();
        report.append("University of the People - CS 1102\n");
        report.append(assignmentName).append("\n\n");

        report.append("=== SOURCE CODE ===\n");
        report.append(allSources).append("\n");

        report.append("=== COMPILATION OUTPUT ===\n");
        report.append(compileOutput.isBlank() ? "No errors.\n" : compileOutput).append("\n");

        report.append("=== PROGRAM OUTPUT ===\n");
        report.append(runOutput.isBlank() ? "(No output)\n" : runOutput).append("\n");

        // Save report
        Path resultFile = Paths.get(outputFolder, assignmentName + " - Code and Output.txt");
        Files.writeString(resultFile, report.toString());

        System.out.println("Assignment saved to " + resultFile);
    }
}
