package com.stockxit.nand2tetris;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter mPrintWriter;
    private String mFileName;

    private int labelCount = 0;

    public CodeWriter(File file) {
        mPrintWriter = null;
        File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
        try {
            mPrintWriter = new PrintWriter(new FileWriter(outputFile));
            mFileName = file.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public void writeArithmetic(String command) {
        // TODO
        mPrintWriter.printf("// %s\n", command);

        // do actual operation
        switch (command) {
            case "add":
                popStackToD();
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=D+M");
                incrementStackPointer();
                break;
            case "sub":
                popStackToD();
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=M-D");
                incrementStackPointer();
                break;
            case "neg":
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=-M");
                incrementStackPointer();
                break;
            case "eq":
                writeCompareLogic("JEQ");
                break;
            case "gt":
                writeCompareLogic("JGT");
                break;
            case "lt":
                writeCompareLogic("JLT");
                break;
            case "and":
                popStackToD();
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=D&M");
                incrementStackPointer();
                break;
            case "or":
                popStackToD();
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=D|M");
                incrementStackPointer();
                break;
            case "not":
                decrementStackPointer();
                loadStackPointerToA();
                mPrintWriter.println("M=!M");
                incrementStackPointer();
                break;
        }
    }

    public void writePushPop(CommandType commandType, String segment, int index) {
        // TODO
        switch (commandType) {
            case C_PUSH:
                mPrintWriter.printf("// push %s %d\n", segment, index);
                switch (segment) {
                    case "constant":
                        // store value in D
                        mPrintWriter.println("@"+index);
                        mPrintWriter.println("D=A");
                        break;
                    case "local":
                        loadSegment("LCL", index);
                        mPrintWriter.println("D=M");
                        break;
                    case "argument":
                        loadSegment("ARG", index);
                        mPrintWriter.println("D=M");
                        break;
                    case "this":
                        loadSegment("THIS", index);
                        mPrintWriter.println("D=M");
                        break;
                    case "that":
                        loadSegment("THAT", index);
                        mPrintWriter.println("D=M");
                        break;
                    case "pointer":
                        mPrintWriter.println("@R"+ String.valueOf(3 + index));
                        mPrintWriter.println("D=M");
                        break;
                    case "temp":
                        mPrintWriter.println("@R"+ String.valueOf(5 + index));
                        mPrintWriter.println("D=M");
                        break;
                    case "static":
                        mPrintWriter.println("@"+mFileName.split("\\.")[0]+String.valueOf(index));
                        mPrintWriter.println("D=M");
                }
                pushDToStack();
                break;
            case C_POP:
                mPrintWriter.printf("// pop %s %d\n", segment, index);
                switch (segment) {
                    case "constant":
                        mPrintWriter.println("@"+index);
                        break;
                    case "local":
                        loadSegment("LCL", index);
                        break;
                    case "argument":
                        loadSegment("ARG", index);
                        break;
                    case "this":
                        loadSegment("THIS", index);
                        break;
                    case "that":
                        loadSegment("THAT", index);
                        break;
                    case "pointer":
                        mPrintWriter.println("@R"+ String.valueOf(3 + index));
                        break;
                    case "temp":
                        mPrintWriter.println("@R"+ String.valueOf(5 + index));
                        break;
                    case "static":
                        mPrintWriter.println("@"+mFileName.split("\\.")[0]+String.valueOf(index));
                        break;

                }
                mPrintWriter.println("D=A");
                mPrintWriter.println("@R13");
                mPrintWriter.println("M=D");
                popStackToD();
                mPrintWriter.println("@R13");
                mPrintWriter.println("A=M");
                mPrintWriter.println("M=D");
                break;
        }
    }

// Nuevos métodos para  traducir nuevos métodos agregados -------------

    public void writeFunction(String functionName, int numLocals) {
        mPrintWriter.printf("// function %s %d\n", functionName, numLocals);
        mPrintWriter.printf("(%s)\n", functionName);

        for (int i = 0; i < numLocals; i++) {
            mPrintWriter.println("@0");
            mPrintWriter.println("D=A");
            pushDToStack();
        }
    }

    public void writeCall(String functionName, int numArgs) {
        mPrintWriter.printf("// call %s %d\n", functionName, numArgs);

        // Guardamos la dirección de retorno
        String returnLabel = "RETURN_ADDRESS" + labelCount++;
        mPrintWriter.println("@" + returnLabel);
        mPrintWriter.println("D=A");
        pushDToStack();

        // Guardamos el local
        mPrintWriter.println("@LCL");
        mPrintWriter.println("D=M");
        pushDToStack();

        // Guardamos el ARG
        mPrintWriter.println("@ARG");
        mPrintWriter.println("D=M");
        pushDToStack();

        // Guardamos el THIS
        mPrintWriter.println("@THIS");
        mPrintWriter.println("D=M");
        pushDToStack();

        // Guardamos el THAT
        mPrintWriter.println("@THAT");
        mPrintWriter.println("D=M");
        pushDToStack();

        // Reposicionamos el ARG
        mPrintWriter.println("@SP");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@" + (numArgs + 5));
        mPrintWriter.println("D=D-A");
        mPrintWriter.println("@ARG");
        mPrintWriter.println("M=D");

        // Reposicionamos el LCL
        mPrintWriter.println("@SP");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@LCL");
        mPrintWriter.println("M=D");

        // Vamos a la función llamada
        mPrintWriter.println("@" + functionName);
        mPrintWriter.println("0;JMP");

        // Declaramos la etiqueta de retorno
        mPrintWriter.printf("(%s)\n", returnLabel);
    }

    public void writeReturn() {
        mPrintWriter.println("// return");

        // FRAME = LCL
        mPrintWriter.println("@LCL");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@R13"); // R13 = FRAME
        mPrintWriter.println("M=D");

        // RET = *(FRAME-5)
        mPrintWriter.println("@5");
        mPrintWriter.println("A=D-A");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@R14"); // R14 = RET
        mPrintWriter.println("M=D");

        // *ARG = pop()
        popStackToD();
        mPrintWriter.println("@ARG");
        mPrintWriter.println("A=M");
        mPrintWriter.println("M=D");

        // SP = ARG + 1
        mPrintWriter.println("@ARG");
        mPrintWriter.println("D=M+1");
        mPrintWriter.println("@SP");
        mPrintWriter.println("M=D");

        // THAT = *(FRAME-1)
        mPrintWriter.println("@R13");
        mPrintWriter.println("AM=M-1");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@THAT");
        mPrintWriter.println("M=D");

        // THIS = *(FRAME-2)
        mPrintWriter.println("@R13");
        mPrintWriter.println("AM=M-1");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@THIS");
        mPrintWriter.println("M=D");

        // ARG = *(FRAME-3)
        mPrintWriter.println("@R13");
        mPrintWriter.println("AM=M-1");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@ARG");
        mPrintWriter.println("M=D");

        // LCL = *(FRAME-4)
        mPrintWriter.println("@R13");
        mPrintWriter.println("AM=M-1");
        mPrintWriter.println("D=M");
        mPrintWriter.println("@LCL");
        mPrintWriter.println("M=D");

        // goto RET
        mPrintWriter.println("@R14");
        mPrintWriter.println("A=M");
        mPrintWriter.println("0;JMP");
    }


    public void close() {
        mPrintWriter.close();
    }


    private void incrementStackPointer() {
        mPrintWriter.println("@SP");
        mPrintWriter.println("M=M+1");
    }

    private void decrementStackPointer() {
        mPrintWriter.println("@SP");
        mPrintWriter.println("M=M-1");
    }

    private void popStackToD() {
        decrementStackPointer();
        mPrintWriter.println("A=M");
        mPrintWriter.println("D=M");
    }

    private void pushDToStack() {
        loadStackPointerToA();
        mPrintWriter.println("M=D");
        incrementStackPointer();
    }

    private void loadStackPointerToA() {
        mPrintWriter.println("@SP");
        mPrintWriter.println("A=M");
    }

    private void writeCompareLogic(String jumpCommand) {
        popStackToD();
        decrementStackPointer();
        loadStackPointerToA();
        mPrintWriter.println("D=M-D");
        mPrintWriter.println("@LABEL" + labelCount);
        mPrintWriter.println("D;"+jumpCommand);
        loadStackPointerToA();
        mPrintWriter.println("M=0");
        mPrintWriter.println("@ENDLABEL" + labelCount);
        mPrintWriter.println("0;JMP");
        mPrintWriter.println("(LABEL" + labelCount + ")");
        loadStackPointerToA();
        mPrintWriter.println("M=-1");
        mPrintWriter.println("(ENDLABEL" + labelCount + ")");
        incrementStackPointer();
        labelCount++;
    }

    private void loadSegment(String segment, int index) {
        mPrintWriter.println("@" + segment);
        mPrintWriter.println("D=M");
        mPrintWriter.println("@"+String.valueOf(index));
        mPrintWriter.println("A=D+A");
    }

}
