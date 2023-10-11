package ru.katyshev;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Getter
@Setter
public class Backup {
    private PropertiesLoader propertiesLoader;
    private File dateFile;
    private Path destination;
    private long lastBackupTime;
    private List<Path> sources = new ArrayList<>();
    private final List<Path> filesToCopy = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private long backupSize;
    private final Logger logger = LoggerFactory.getLogger(Backup.class);

    public void start() {
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("properties.properties"));
        logger.info("\nSTART APP");
        propertiesLoader.loadProperties();
        initLastBackupTime();
        searchFilesToCopy();
        printReport();
        askAgree();
        copying();
        recordCurrentDate();
        logger.info("STOP APP\n\n");
    }

    private void initLastBackupTime() {
        try {
            if (!Files.exists(dateFile.toPath())) {
                if (!Files.exists(dateFile.toPath().getParent())) {
                    Files.createDirectory(dateFile.toPath().getParent());
                }
                mapper.writeValue(dateFile, 0);
                logger.info("Previously, there was no backup. Creating dateFile");
            } else {
                lastBackupTime = mapper.readValue(dateFile, Long.class);
                logger.info("Date of the previous backup = " + new Date(lastBackupTime));
            }
        } catch (IOException ex) {
            logger.error("Error read date from dateFile\n" + ex.getMessage());
            throw new RuntimeException(ex);
        }

    }

    private void searchFilesToCopy() {
        logger.info("Start to searching files");

        for (Path src : sources) {
            try {
                Files.walkFileTree(src, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                        if (lastBackupTime > attrs.lastModifiedTime().toMillis()) {
                            return FileVisitResult.CONTINUE;
                        }

                        // copy project folder
                        if (file.toString().contains("\\PROJECT\\")) {
                            if (file.toString().contains("\\PROJECT\\PREMIERE\\") && file.toString().endsWith(".mp4")) {
                                return FileVisitResult.CONTINUE;
                            }
                            filesToCopy.add(file);
                            backupSize += attrs.size();
                            return FileVisitResult.CONTINUE;
                        }

                        // copy design folder
                        if (file.toString().contains("\\DESIGN\\")) {
                            filesToCopy.add(file);
                            backupSize += attrs.size();
                            return FileVisitResult.CONTINUE;
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                logger.error("ERROR searching files\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printReport() {
        logger.info("\nREPORT\n" +
                filesToCopy.size() + " files to copy\nBackup size: " +
                (backupSize / 1054974) + "mb");
    }

    private void askAgree() {
        logger.trace("\ncontinue? (y/n)");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String answer;
            while (true) {
                answer = reader.readLine();
                if (answer.equalsIgnoreCase("y")) {
                    return;
                } else if (answer.equalsIgnoreCase("n")) {
                    logger.info("USER STOPPED APP\n\n");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void copying() {

        for (Path fileToCopy : filesToCopy) {
            Path newPath = Path.of(fileToCopy.toString().replaceFirst(":", ""));
            Path dest = destination.resolve(newPath);

            try{
                if (!Files.exists(dest.getParent())) {
                    Files.createDirectories(dest.getParent());
                }

                if (Files.exists(dest)) {
                    Files.delete(dest);
                }

                logger.info("now copying: " + fileToCopy.toString());
                Files.copy(fileToCopy, dest);

            } catch (IOException e) {
                logger.error("\n\nERROR copying file\n" + e.getMessage() + "\n\n");
            }
        }
    }

    private void recordCurrentDate() {
        try {
            mapper.writeValue(dateFile, new Date().getTime());
        } catch (IOException e) {
            logger.error("Error record current date to file\n" + e.getMessage());
        }
    }
}
