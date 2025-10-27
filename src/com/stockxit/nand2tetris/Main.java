package com.stockxit.nand2tetris;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java com.stockxit.nand2tetris.Main <inputfile.vm | inputDirectory>");
            System.exit(1);
        }

        File input = new File(args[0]);
        File outputFile;
        if (input.isDirectory()) {
            outputFile = new File(input, input.getName() + ".asm");
        } else {
            String outPath = input.getAbsolutePath().replaceAll("\\.vm$", ".asm");
            outputFile = new File(outPath);
        }

        CodeWriter codeWriter = new CodeWriter(outputFile);

        // bootstrap
        codeWriter.writeInit();

        if (input.isDirectory()) {
            File[] files = input.listFiles((dir, name) -> name.endsWith(".vm"));
            if (files != null) {
                // important: process in sorted order for determinism
                Arrays.sort(files);
                for (File f : files) {
                    processFile(f, codeWriter);
                }
            }
        } else {
            processFile(input, codeWriter);
        }

        codeWriter.close();
        System.out.println("Generado: " + outputFile.getAbsolutePath());
    }

    private static void processFile(File vmFile, CodeWriter codeWriter) {
        // set filename for static variables
        String baseName = vmFile.getName().replaceAll("\\.vm$", "");
        codeWriter.setFileName(baseName);

        try (Scanner scanner = new Scanner(vmFile)) {
            Parser parser = new Parser(scanner);
            while (parser.hasMoreCommands()) {
                parser.advance();
                CommandType type = parser.commandType();
                switch (type) {
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.arg1());
                        break;
                    case C_PUSH:
                    case C_POP:
                        codeWriter.writePushPop(type, parser.arg1(), parser.arg2());
                        break;
                    case C_LABEL:
                        codeWriter.writeLabel(parser.arg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;
                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                    default:
                        throw new IllegalStateException("Comando no manejado: " + type);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Archivo VM no encontrado: " + vmFile.getAbsolutePath());
        }
    }
}
