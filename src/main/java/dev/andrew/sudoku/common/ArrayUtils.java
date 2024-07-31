package dev.andrew.sudoku.common;

public class ArrayUtils {

    public static byte[][] deepCopy(byte[][] original) {
        if (original == null) return null;

        byte[][] copy = new byte[original.length][];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                copy[i] = original[i].clone();
            }
        }

        return copy;
    }
}