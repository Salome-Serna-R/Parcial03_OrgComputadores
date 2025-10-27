package com.stockxit.nand2tetris;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter out;
    private String currentFileName; // basename sin extensión
    private int labelCount = 0;

    public CodeWriter(File outputFile) {
        try {
            this.out = new PrintWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Cannot open output file: " + outputFile.getAbsolutePath(), e);
        }
    }

    public void setFileName(String filename) { // e.g. "Main" (sin .vm)
        this.currentFileName = filename;
    }

    public void close() {
        if (out != null) out.close();
    }

    // Bootstrap: SP=256; call Sys.init 0
    public void writeInit() {
        // set SP = 256
        out.println("// bootstrap");
        out.println("@256");
        out.println("D=A");
        out.println("@SP");
        out.println("M=D");
        // call Sys.init 0
        writeCall("Sys.init", 0);
    }

    /* ---------- Arithmetic ---------- */
    public void writeArithmetic(String command) {
        out.println("// " + command);
        switch (command) {
            case "add": binaryOp("M=D+M"); break;
            case "sub": binaryOp("M=M-D"); break;
            case "and": binaryOp("M=D&M"); break;
            case "or":  binaryOp("M=D|M"); break;
            case "neg": unaryOp("M=-M"); break;
            case "not": unaryOp("M=!M"); break;
            case "eq": comparison("JEQ"); break;
            case "gt": comparison("JGT"); break;
            case "lt": comparison("JLT"); break;
            default:
                throw new IllegalArgumentException("Unknown arithmetic command: " + command);
        }
    }

    private void unaryOp(String comp) {
        // SP--; M = unary(M)
        out.println("@SP");
        out.println("A=M-1");
        out.println(comp);
    }

    private void binaryOp(String comp) {
        // pop y, pop x; compute x op y; push result
        // D = *SP--; A = SP-1; M = M op D
        out.println("@SP");
        out.println("AM=M-1"); // SP = SP-1; A = SP
        out.println("D=M");   // D = y
        out.println("A=A-1"); // A = SP-1 (x)
        out.println(comp);    // M = M op D
    }

    private void comparison(String jmp) {
        String labelTrue = uniqueLabel("TRUE");
        String labelEnd = uniqueLabel("END");
        out.println("@SP");
        out.println("AM=M-1"); // SP-- ; A=SP
        out.println("D=M");   // D = y
        out.println("A=A-1"); // A = SP-1 (x)
        out.println("D=M-D"); // D = x - y
        out.println("@" + labelTrue);
        out.println("D;" + jmp);
        // false
        out.println("@SP");
        out.println("A=M-1");
        out.println("M=0");
        out.println("@" + labelEnd);
        out.println("0;JMP");
        // true
        out.println("(" + labelTrue + ")");
        out.println("@SP");
        out.println("A=M-1");
        out.println("M=-1");
        // end
        out.println("(" + labelEnd + ")");
    }

    private String uniqueLabel(String base) {
        return base + "." + (labelCount++);
    }

    /* ---------- Push / Pop ---------- */
    public void writePushPop(CommandType cmd, String segment, int index) {
        out.println("// " + cmd + " " + segment + " " + index);
        if (cmd == CommandType.C_PUSH) {
            switch (segment) {
                case "constant":
                    out.println("@" + index);
                    out.println("D=A");
                    pushDToStack();
                    break;
                case "local":
                case "argument":
                case "this":
                case "that":
                    String base = segmentBase(segment);
                    out.println("@" + base);
                    out.println("D=M");
                    out.println("@" + index);
                    out.println("A=D+A");
                    out.println("D=M");
                    pushDToStack();
                    break;
                case "temp":
                    if (index < 0 || index > 7) throw new IllegalArgumentException("temp index out of range");
                    out.println("@" + (5 + index));
                    out.println("D=M");
                    pushDToStack();
                    break;
                case "pointer":
                    if (index == 0) out.println("@THIS");
                    else out.println("@THAT");
                    out.println("D=M");
                    pushDToStack();
                    break;
                case "static":
                    out.println("@" + currentFileName + "." + index);
                    out.println("D=M");
                    pushDToStack();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown segment: " + segment);
            }
        } else if (cmd == CommandType.C_POP) {
            switch (segment) {
                case "local":
                case "argument":
                case "this":
                case "that":
                    String base = segmentBase(segment);
                    // compute address in R13 = base + index
                    out.println("@" + base);
                    out.println("D=M");
                    out.println("@" + index);
                    out.println("D=D+A");
                    out.println("@R13");
                    out.println("M=D");
                    // pop stack into D
                    popStackToD();
                    // *R13 = D
                    out.println("@R13");
                    out.println("A=M");
                    out.println("M=D");
                    break;
                case "temp":
                    if (index < 0 || index > 7) throw new IllegalArgumentException("temp index out of range");
                    popStackToD();
                    out.println("@" + (5 + index));
                    out.println("M=D");
                    break;
                case "pointer":
                    popStackToD();
                    if (index == 0) out.println("@THIS");
                    else out.println("@THAT");
                    out.println("M=D");
                    break;
                case "static":
                    popStackToD();
                    out.println("@" + currentFileName + "." + index);
                    out.println("M=D");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown segment for pop: " + segment);
            }
        } else {
            throw new IllegalArgumentException("Invalid command type for writePushPop");
        }
    }

    private void pushDToStack() {
        out.println("@SP");
        out.println("A=M");
        out.println("M=D");
        out.println("@SP");
        out.println("M=M+1");
    }

    private void popStackToD() {
        out.println("@SP");
        out.println("AM=M-1");
        out.println("D=M");
    }

    private String segmentBase(String segment) {
        switch (segment) {
            case "local": return "LCL";
            case "argument": return "ARG";
            case "this": return "THIS";
            case "that": return "THAT";
            default: throw new IllegalArgumentException("segmentBase unknown " + segment);
        }
    }

    /* ---------- Labels / Goto / If ---------- */
    public void writeLabel(String label) {
        out.println("(" + label + ")");
    }

    public void writeGoto(String label) {
        out.println("@" + label);
        out.println("0;JMP");
    }

    public void writeIf(String label) {
        // pop stack -> if value != 0 goto label
        popStackToD();
        out.println("@" + label);
        out.println("D;JNE");
    }

    /* ---------- Function / Call / Return ---------- */
    public void writeFunction(String functionName, int nLocals) {
        out.println("(" + functionName + ")");
        // initialize nLocals to 0
        for (int i = 0; i < nLocals; i++) {
            out.println("@0");
            out.println("D=A");
            pushDToStack();
        }
    }

    public void writeCall(String functionName, int nArgs) {
        String returnLabel = uniqueLabel("RET");
        // push return address
        out.println("@" + returnLabel);
        out.println("D=A");
        pushDToStack();
        // push LCL
        out.println("@LCL");
        out.println("D=M");
        pushDToStack();
        // push ARG
        out.println("@ARG");
        out.println("D=M");
        pushDToStack();
        // push THIS
        out.println("@THIS");
        out.println("D=M");
        pushDToStack();
        // push THAT
        out.println("@THAT");
        out.println("D=M");
        pushDToStack();

        // ARG = SP - nArgs - 5
        out.println("@SP");
        out.println("D=M");
        out.println("@" + (nArgs + 5));
        out.println("D=D-A");
        out.println("@ARG");
        out.println("M=D");

        // LCL = SP
        out.println("@SP");
        out.println("D=M");
        out.println("@LCL");
        out.println("M=D");

        // goto f
        out.println("@" + functionName);
        out.println("0;JMP");

        // (returnLabel)
        out.println("(" + returnLabel + ")");
    }

    public void writeReturn() {
        // FRAME = LCL (R13)
        out.println("@LCL");
        out.println("D=M");
        out.println("@R13");
        out.println("M=D");

        // RET = *(FRAME - 5) in R14
        out.println("@5");
        out.println("A=D-A");
        out.println("D=M");
        out.println("@R14");
        out.println("M=D");

        // *ARG = pop()
        popStackToD();
        out.println("@ARG");
        out.println("A=M");
        out.println("M=D");

        // SP = ARG + 1
        out.println("@ARG");
        out.println("D=M+1");
        out.println("@SP");
        out.println("M=D");

        // THAT = *(FRAME - 1)
        out.println("@R13");
        out.println("AM=M-1");
        out.println("D=M");
        out.println("@THAT");
        out.println("M=D");

        // THIS = *(FRAME - 2)
        out.println("@R13");
        out.println("AM=M-1");
        out.println("D=M");
        out.println("@THIS");
        out.println("M=D");

        // ARG = *(FRAME - 3)
        out.println("@R13");
        out.println("AM=M-1");
        out.println("D=M");
        out.println("@ARG");
        out.println("M=D");

        // LCL = *(FRAME - 4)
        out.println("@R13");
        out.println("AM=M-1");
        out.println("D=M");
        out.println("@LCL");
        out.println("M=D");

        // goto RET
        out.println("@R14");
        out.println("A=M");
        out.println("0;JMP");
    }
}
