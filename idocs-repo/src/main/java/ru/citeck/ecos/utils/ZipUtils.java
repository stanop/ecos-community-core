package ru.citeck.ecos.utils;

import com.google.common.collect.Lists;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

    private static final Charset ENCODING_CP866 = Charset.forName("CP866");

    public static List<File> getContent(File zipFile) throws IOException {

        File tempDirFile = new File(TempFileProvider.getTempDir(), GUID.generate());
        tempDirFile.mkdir();
        Path tempDir = tempDirFile.toPath();

        forEachFile(zipFile, (entry, stream) -> {

            if (!entry.isDirectory()) {

                Path entryPath = tempDir.resolve(entry.getName());
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

    @FunctionalInterface
    public interface ZipEntryWork {
        void doWork(ZipEntry entry, InputStream inStream) throws IOException;
    }
}
