package dev.andrew.sudoku.solver;

import dev.andrew.sudoku.model.Sudoku;

@FunctionalInterface
public interface SudokuSolver {
    public Sudoku solve(Sudoku initialState);
}
