package dev.andrew.sudoku;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import dev.andrew.sudoku.model.Sudoku;
import dev.andrew.sudoku.solver.SudokuSolver;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application.properties")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SudokuApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${puzzles.solved.dir}")
    private String SOLVED_DIR;
    @Value("${puzzles.unsolved.dir}")
    private String UNSOLVED_DIR;

    @Test
    public void SudokuSolver_beans_are_successfully_loaded_into_application_context() {
        Map<String, SudokuSolver> beans = applicationContext.getBeansOfType(SudokuSolver.class);
        assertThat(beans).isNotEmpty();
    }

    @Test
    public void Sudoku_can_load_puzzle_from_String() throws IOException {
        String puzzleString = FileUtils.fileContents(String.format("%s/board00.txt", UNSOLVED_DIR));
        assertThat(puzzleString).isNotNull();
        Sudoku puzzle = Sudoku.fromString(puzzleString);
        assertThat(puzzle).isNotNull();
        assertThat(puzzle.toString()).isEqualToIgnoringWhitespace(puzzleString.replaceAll("0", " "));
    }

    private Stream<SudokuSolver> beanStream() {
        return applicationContext.getBeansOfType(SudokuSolver.class).values().stream();
    }

    @MethodSource("beanStream")
    @ParameterizedTest(name = "SudokuSolver bean [{0}] can solves puzzles correctly")
    public void testSolver(SudokuSolver solver) throws IOException {
        Map<String, String> unsolvedToSolved = getUnsolvedToSolvedMapping();
        long totalTime = unsolvedToSolved.entrySet().parallelStream()
                .mapToLong(entry -> benchmarkSolver(solver, entry.getKey(), entry.getValue()))
                .sum();

        System.out.printf("Solver [%s] successfully solved [%d] puzzles in a total of [%d]ms\n",
                solver.toString(), unsolvedToSolved.size(), totalTime);
    }

    private long benchmarkSolver(SudokuSolver solver, String unsolvedPuzzleString, String knownSolvedPuzzleString) {
        Sudoku unsolvedPuzzle = Sudoku.fromString(unsolvedPuzzleString);
        Sudoku knownSolvedPuzzle = Sudoku.fromString(knownSolvedPuzzleString);

        long startTime = System.currentTimeMillis();
        Sudoku solvedPuzzle = solver.solve(unsolvedPuzzle);
        long elapsed = System.currentTimeMillis() - startTime;

        assertThat(solvedPuzzle.solved()).isTrue(); // what about unsolvable boards?

        /*
         * The following line would work just fine, but printing the
         * (possible) difference in the toString representation is
         * more helpful when debugging a faulty solver
         */
        // assertThat(solvedPuzzle.equals(knownSolvedPuzzle)).isTrue();
        assertThat(solvedPuzzle.toString()).isEqualTo(knownSolvedPuzzle.toString());

        return elapsed;
    }

    public Map<String, String> getUnsolvedToSolvedMapping() throws IOException {
        Map<String, String> solvedFileContentMap = getFileContentMap(SOLVED_DIR);
        Map<String, String> unsolvedFileContentMap = getFileContentMap(UNSOLVED_DIR);

        return unsolvedFileContentMap.entrySet().stream()
                .filter(entry -> solvedFileContentMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        entry -> entry.getValue(),
                        entry -> solvedFileContentMap.get(entry.getKey())));
    }

    /**
     * Returns a Map where the key is the filename
     * and the value is the content of that file
     *
     * Let the test/resources directory look like this:
     *
     * <pre>{@code
     * .
     * └── puzzles
     *     ├── solved
     *     │   ├── board00.txt
     *     │   ├── board01.txt
     *     │   ├── ...
     *     │   └── board##.txt
     *     └── unsolved
     *         ├── board00.txt
     *         ├── board01.txt
     *         ├── ...
     *         └── board##.txt
     * }</pre>
     *
     * <code>getFileContentMap("puzzles/solved")</code> would return a Map where
     * the keys are <code>{board00.txt, board01.txt, ...}</code>
     * and the values are
     * <code>{content of board00.txt, content of board01.txt, ...}</code>
     *
     * @param directory the directory to interrogate the files of
     * @return A map of filenames and associated content
     * @throws IOException
     */
    private Map<String, String> getFileContentMap(String directory) throws IOException {
        Resource resource = new ClassPathResource(directory);
        Path path = Paths.get(resource.getURI());

        try (Stream<Path> filePaths = Files.walk(path)) {
            return filePaths.filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            filePath -> filePath.getFileName().toString(),
                            filePath -> FileUtils.fileContents(filePath)));
        }
    }
}
