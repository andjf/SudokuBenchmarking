
## Future Enhancements:
- [ ] Create `SudokuParser` components that have a `boolean accept(String sudokuPuzzleString)` method.
   This can be used in the `fromString` method of `dev.andrew.sudoku.model.Sudoku` to parse many
   different types of sudoku puzzles from strings
- [ ] Improve comments and general documentation.
- [ ] Increase the number of test boards
- [ ] Create further implementations of `SudokuSolver`
- [ ] In `Sudoku` create methods
  - `Stream<Byte> row(int y)`
  - `Stream<Byte> column(int x)`
  - `Stream<Byte> cell(int cellX, int cellY)`
  - `Stream<Stream<Byte>> rows()`
  - `Stream<Stream<Byte>> columns()`
  - `Stream<Stream<Byte>> cells()`