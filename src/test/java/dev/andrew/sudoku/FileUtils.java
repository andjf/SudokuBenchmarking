package dev.andrew.sudoku;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ClassPathResource;

/**
 * Utility class for file operations.
 * Provides methods to read the contents of a file from the classpath or from a
 * given file path.
 */
public class FileUtils {

    /**
     * Reads the contents of a file specified by its path String
     *
     * @param pathString the path to the file as a string, relative to the classpath
     * @return the contents of the file as a string, or {@code null} if an
     *         IOException occurs
     */
    public static String fileContents(String pathString) {
        try {
            return fileContents(Paths.get(new ClassPathResource(pathString).getURI()));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads the contents of a file specified by its {@link Path}.
     *
     * @param path the path to the file
     * @return the contents of the file as a string, or {@code null} if an
     *         IOException occurs
     */
    public static String fileContents(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return null;
        }
    }
}
