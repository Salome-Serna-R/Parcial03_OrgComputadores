package com.stockxit.nand2tetris;

import java.util.Scanner;

public class Parser {
    private final Scanner scanner;
    private String currentCommand;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.currentCommand = null;
    }

    public boolean hasMoreCommands() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Remove comments and trim
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            currentCommand = line;
            return true;
        }
        return false;
    }

    public void advance() {
        // currentCommand ya está cargada por hasMoreCommands
        // (este diseño permite que hasMoreCommands prepare la línea)
    }

    public CommandType commandType() {
        if (currentCommand == null) return null;
        String cmd = currentCommand.split(" ")[0];
        switch (cmd) {
            case "push": return CommandType.C_PUSH;
            case "pop": return CommandType.C_POP;
            case "label": return CommandType.C_LABEL;
            case "goto": return CommandType.C_GOTO;
            case "if-goto": return CommandType.C_IF;
            case "function": return CommandType.C_FUNCTION;
            case "call": return CommandType.C_CALL;
            case "return": return CommandType.C_RETURN;
            // arithmetic commands are single word like add, sub, eq...
            default:
                return CommandType.C_ARITHMETIC;
        }
    }

    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            // for arithmetic, arg1 is the command itself
            return currentCommand.split(" ")[0];
        } else {
            String[] parts = currentCommand.split("\\s+");
            if (parts.length >= 2) return parts[1];
            return "";
        }
    }

    public int arg2() {
        CommandType type = commandType();
        if (type == CommandType.C_PUSH || type == CommandType.C_POP
                || type == CommandType.C_FUNCTION || type == CommandType.C_CALL) {
            String[] parts = currentCommand.split("\\s+");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        }
        return 0;
    }
}
