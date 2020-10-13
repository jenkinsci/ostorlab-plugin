package co.ostorlab.ci.jenkins.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * The type File handler.
 */
public class FileHelper {
    /**
     * Load byte [ ].
     *
     * @param file the file
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] load(String file) throws IOException {
        return Files.readAllBytes(Paths.get(file));
    }

    /**
     * Find file in the parent directory provided.
     *
     * @param parent the parent
     * @param name   the name
     * @return the file
     * @throws IOException the io exception
     */
    public static File find(File parent, String name) throws IOException {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final File file = new File(name);
        if (file.isFile() && file.exists()) {
            return file;
        }
        Optional<Path> matched = Files
                .find(Paths.get(parent.getCanonicalPath()), 10, new BiPredicate<Path, BasicFileAttributes>() {
                    @Override
                    public boolean test(Path t, BasicFileAttributes u) {
                        return t.toString().endsWith(file.getName());
                    }
                }, FileVisitOption.FOLLOW_LINKS).distinct().findFirst();
        return matched.map(Path::toFile).orElse(null);
    }

    /**
     *
     * Save a file with String content in the path provided
     *
     * @param path     the path
     * @param contents the contents
     * @throws IOException the io exception
     */
    public static void save(String path, String contents) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(contents.trim());
        }
    }

    /**
     *
     * Load InputStream to byte [ ].
     *
     * @param in the in
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] load(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
