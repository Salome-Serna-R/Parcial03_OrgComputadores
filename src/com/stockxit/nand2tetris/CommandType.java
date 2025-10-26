package com.stockxit.nand2tetris;

public enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_FUNCTION, // Nuevo tipo de comando function para definir funciones
    C_RETURN, // Nuevo tipo de comando return para retornar de funciones
    C_CALL // Nuevo tipo de comando call para llamar a funciones
}
