package ru.citeck.ecos.utils;

import com.google.common.collect.Lists;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

    private static final Charset ENCODING_CP866 = Charset.forName("CP866");

    private static final Pattern ILLEGAL_CHARS_PATTERN = Pattern.compile(
            "# Match a valid Windows filename (unspecified file system).          \n" +
                    "^                                # Anchor to start of string.        \n" +
                    "(?!                              # Assert filename is not: CON, PRN, \n" +
                    "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
                    "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
                    "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
                    "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
                    "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
                    "  $                              # and end of string                 \n" +
                    ")                                # End negative lookahead assertion. \n" +
                    "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
                    "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
                    "$                                # Anchor to end of string.            ",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);

    private static final int FILENAME_MAX_LENGTH = 150;
    private static final int EXTENSION_MAX_LENGTH = 10;

    public static List<File> getContent(File zipFile) throws IOException {

        File tempDirFile = new File(TempFileProvider.getTempDir(), GUID.generate());
        tempDirFile.mkdir();
        Path tempDir = tempDirFile.toPath();

        forEachFile(zipFile, (entry, stream) -> {

            if (!entry.isDirectory()) {

                Path entryPath = getValidNewPath(tempDir, entry.getName());
                File entryFile = entryPath.toFile();

                entryPath.getParent().toFile().mkdirs();
                entryFile.createNewFile();

                try (FileOutputStream fileStream = new FileOutputStream(entryFile, false)) {
                    IOUtils.copy(stream, fileStream);
                }
            }
        });

        File[] files = tempDir.toFile().listFiles();

        return files != null ? Lists.newArrayList(files) : Collections.emptyList();
    }

    public static void forEachFile(File zipFile, ZipEntryWork work) throws IOException {

        try (ZipFile zip = new ZipFile(zipFile, ENCODING_CP866)) {
            Enumeration<? extends ZipEntry> entries;
            for (entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    try (InputStream inStream = zip.getInputStream(entry)) {
                        work.doWork(entry, inStream);
                    }
                }
            }
        }
    }

    private static Path getValidNewPath(Path dir, String name) {

        String validName = ILLEGAL_CHARS_PATTERN.matcher(name).replaceAll("_");

        String baseName = FilenameUtils.removeExtension(validName);
        String extension = FilenameUtils.getExtension(validName);

        if (baseName.length() > FILENAME_MAX_LENGTH) {
            baseName = baseName.substring(0, FILENAME_MAX_LENGTH) + "~";
        }
        if (extension.length() > EXTENSION_MAX_LENGTH) {
            extension = extension.substring(0, EXTENSION_MAX_LENGTH) + "~";
        }

        Path result = null;
        try {
            for (int i = 0; i < 100; i++) {

                String idxBaseName = baseName + (i > 0 ? String.format("(%d)", i) : "");
                String filename = String.format("%s.%s", idxBaseName, extension);

                Path path = dir.resolve(filename);
                if (!path.toFile().exists()) {
                    result = path;
                    break;
                }
            }
            if (result == null) {
                result = dir.resolve(String.format("%s.%s", GUID.generate(), extension));
            }
        } catch (InvalidPathException e) {
            result = dir.resolve(GUID.generate());
        }
        return result;
    }

    @FunctionalInterface
    public interface ZipEntryWork {
        void doWork(ZipEntry entry, InputStream inStream) throws IOException;
    }
}
