package dev.andrew.sudoku.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dev.andrew.sudoku.common.ArrayUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Sudoku {
    private final byte[][] board;
    public static final int SIZE = 9;
    public static final int CELL_SIZE = 3;
    private final static Pattern LINE_PATTERN = Pattern.compile(String.format("^[\\d]{%d}$", SIZE));

    // Each high bit of the mask represents the presence of that number in a section (row, column, 3x3 cell).
    private final static char COMPLETE_FLAG_MASK = 0b1111111110;

    public static Sudoku fromString(String sudokuBoardString) {
        if (sudokuBoardString == null) {
            log.warn("Cannot extract board from null String.");
            return null;
        }

        String[] lines = sudokuBoardString.split(System.lineSeparator());
        if (lines.length != SIZE) {
            log.warn("Invalid number of lines: expected=[{}] found=[{}]", SIZE, lines.length);
            return null;
        }

        Collection<String> invalidRows = Arrays.stream(lines)
                .filter(line -> !LINE_PATTERN.matcher(line).matches())
                .collect(Collectors.toList());

        if (!invalidRows.isEmpty()) {
            invalidRows.forEach(line -> log.warn("Invalid row: row=[{}]. Must match [{}]", line, LINE_PATTERN));
            return null;
        }

        byte[][] board = new byte[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            char[] row = lines[y].toCharArray();
            for (int x = 0; x < SIZE; x++) {
                board[y][x] = (byte) (row[x] - '0');
            }
        }

        return new Sudoku(board);
    }

    public boolean onBoard(int x, int y) {
        return (0 <= y && y < SIZE) && (0 <= x && x < SIZE);
    }

    public byte get(int x, int y) {
        assert onBoard(x, y);
        return this.board[y][x];
    }

    public void set(int x, int y, byte value) {
        assert onBoard(x, y);
        this.board[y][x] = value;
    }

    public boolean validPlacement(int x, int y, byte value) {
        return validPlacementRow(x, y, value) && validPlacementColumn(x, y, value) && validPlacementCell(x, y, value);
    }

    private boolean validPlacementRow(int x, int y, byte value) {
        for (int x_ = 0; x_ < SIZE; x_++) {
            if (this.board[y][x_] == value) {
                return false;
            }
        }
        return true;
    }

    private boolean validPlacementColumn(int x, int y, byte value) {
        for (int y_ = 0; y_ < SIZE; y_++) {
            if (this.board[y_][x] == value) {
                return false;
            }
        }
        return true;
    }

    private boolean validPlacementCell(int x, int y, byte value) {
        for (int y_ = 0; y_ < CELL_SIZE; y_++) {
            for (int x_ = 0; x_ < CELL_SIZE; x_++) {
                if (this.board[3 * (y / 3) + y_][3 * (x / 3) + x_] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public Sudoku deepCopy() {
        return new Sudoku(ArrayUtils.deepCopy(this.board));
    }

    public boolean solved() {
        return solvedRows() && solvedColumns() && solvedCells();
    }

    private boolean solvedRows() {
        for (int y = 0; y < SIZE; y++) {
            char flags = 0;
            for (int x = 0; x < SIZE; x++) {
                byte val = this.board[y][x];
                if (val == 0) {
                    return false;
                }
                flags ^= (1 << val);
            }
            if (flags != COMPLETE_FLAG_MASK) {
                return false;
            }
        }
        return true;
    }

    private boolean solvedColumns() {
        for (int x = 0; x < SIZE; x++) {
            char flags = 0;
            for (int y = 0; y < SIZE; y++) {
                byte val = this.board[y][x];
                if (val == 0) {
                    return false;
                }
                flags ^= (1 << val);
            }
            if (flags != COMPLETE_FLAG_MASK) {
                return false;
            }
        }
        return true;
    }

    private boolean solvedCells() {
        for (int y = 0; y < SIZE; y += CELL_SIZE) {
            for (int x = 0; x < SIZE; x += CELL_SIZE) {
                char flags = 0;
                for (int y_ = 0; y_ < CELL_SIZE; y_++) {
                    for (int x_ = 0; x_ < CELL_SIZE; x_++) {
                        byte val = this.board[3 * (y / 3) + y_][3 * (x / 3) + x_];
                        if (val == 0) {
                            return false;
                        }
                        flags ^= (1 << val);
                    }
                }
                if (flags != COMPLETE_FLAG_MASK) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (this.board[y][x] == 0) {
                    sb.append(' ');
                } else {
                    sb.append(this.board[y][x]);
                }
                if (x != (SIZE - 1)) {
                    sb.append(' ');
                }
            }
            if (y != (SIZE - 1)) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Sudoku other = (Sudoku) obj;

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (this.get(x, y) != other.get(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }
}
