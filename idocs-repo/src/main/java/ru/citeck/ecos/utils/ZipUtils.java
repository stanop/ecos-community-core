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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

    private static final Charset ENCODING_CP866 = Charset.forName("CP866");

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

        String validName = sanitizeFilename(name);

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

    /**
     * Removes incorrect characters and character sequences from filename
     * @param input filename with extension
     * @return
     */
    private static String sanitizeFilename (String input){
        return input.replaceAll("[^a-zA-Zа-яА-Я0-9\\.\\-]", "_")
                .replaceAll("\\.+", ".")
                .trim();
    }

    @FunctionalInterface
    public interface ZipEntryWork {
        void doWork(ZipEntry entry, InputStream inStream) throws IOException;
    }
}
