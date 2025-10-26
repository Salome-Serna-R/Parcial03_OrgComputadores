package com.stockxit.nand2tetris;

import java.util.Scanner;

public class Parser {
    private Scanner mScanner;
    private String mCurrentCommand;

    public Parser(Scanner scanner) {
        this.mScanner = scanner;
    }

    public boolean hasMoreCommands() {
        return mScanner.hasNextLine();
    }

    public void advance() {
        if (hasMoreCommands()) {
            mCurrentCommand = mScanner.nextLine();

            // strip out comment
            int commentIndex = mCurrentCommand.indexOf("/");
            if (commentIndex >= 0) {
                mCurrentCommand = mCurrentCommand.substring(0, commentIndex);
            }

            // trim just to be sure
            mCurrentCommand = mCurrentCommand.trim();

            if (mCurrentCommand.isEmpty()) {
                advance();
            }
        }
    }


// Agregamos los nuevos tipos de comando -----

    public CommandType commandType() {
        if (mCurrentCommand == null || mCurrentCommand.isEmpty()) {
            return null;
        }

        String command = mCurrentCommand.split(" ")[0];

        switch (command) {
            case "push":
                return CommandType.C_PUSH;
            case "pop":
                return CommandType.C_POP;
            case "label":
                return CommandType.C_LABEL;
            case "goto":
                return CommandType.C_GOTO;
            case "if-goto":
                return CommandType.C_IF;
            case "function":
                return CommandType.C_FUNCTION;
            case "call":
                return CommandType.C_CALL;
            case "return":
                return CommandType.C_RETURN;
            default:
                return CommandType.C_ARITHMETIC;
            }
    }

// -----------------------------------------

    public String arg1() {
        if (commandType() == CommandType.C_RETURN) {
            return null;
        }

        String[] parts = mCurrentCommand.split("\\s+");
        if (commandType() == CommandType.C_ARITHMETIC) {
            return parts[0];
        }
        return parts.length > 1 ? parts[1] : null;
    }


    public int arg2() {
        if (commandType() == CommandType.C_PUSH
                || commandType() == CommandType.C_POP
                || commandType() == CommandType.C_FUNCTION
                || commandType() == CommandType.C_CALL) {
            return Integer.valueOf(mCurrentCommand.split(" ")[2]);
        }

        return 0;
    }
}
