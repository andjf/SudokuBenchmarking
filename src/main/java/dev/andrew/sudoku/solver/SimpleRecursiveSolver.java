package dev.andrew.sudoku.solver;

import org.springframework.stereotype.Component;

import dev.andrew.sudoku.model.Sudoku;

@Component(value = "recursive-sudoku-solver")
public class SimpleRecursiveSolver implements SudokuSolver {
    @Override
    public Sudoku solve(Sudoku initialState) {
        return solve(initialState.deepCopy(), 0, 0);
    }

    private Sudoku solve(Sudoku board, int x, int y) {
        if (x == Sudoku.SIZE) {
            x = 0;
            y++;
        }

        if (!board.onBoard(x, y)) {
            return board;
        }

        if (board.get(x, y) != 0) {
            return solve(board, x + 1, y);
        }

        for (byte val = 1; val <= Sudoku.SIZE; val++) {
            if (board.validPlacement(x, y, val)) {
                board.set(x, y, val);
                Sudoku solved = solve(board, x + 1, y);
                if (solved != null) {
                    return solved;
                }
            }
            board.set(x, y, (byte)0);
        }

        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getAnnotation(Component.class).value();
    }
}
