package ru.katyshev;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class Backup {
    private PropertiesLoader propertiesLoader;
    private File dateFile;
    private Path destination;
    private long lastBackupTime ;
    private List<Path> sources = new ArrayList<>();
    private final List<Path> filesToCopy = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private long backupSize;

    public void start() {
        propertiesLoader.loadProperties();
        initLastBackupTime();
        searchFilesToCopy();
        printReport();
        askAgree();
        copying();
        recordCurrentDate();
    }

    private void printReport() {
        System.out.println("\nREPORT\n" +
                filesToCopy.size() + " files to copy\nBackup size: " +
                (backupSize / 1054974) + "mb");
    }

    private void searchFilesToCopy() {
        for (Path src : sources) {
            try {
                Files.walkFileTree(src, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){

                        if (lastBackupTime > attrs.lastModifiedTime().toMillis()) {
                            return FileVisitResult.CONTINUE;
                        }

                        // copy project folder
                        if (file.toString().contains("\\PROJECT\\")) {
                            if(file.toString().contains("\\PROJECT\\PREMIERE\\") && file.toString().endsWith(".mp4")) {
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
                e.printStackTrace();
            }

        }
    }

    private void copying() {
        try {
            for (Path fileToCopy : filesToCopy) {
                Path newPath = Path.of(fileToCopy.toString().replaceFirst(":", ""));
                Path dest = destination.resolve(newPath);

                if(!Files.exists(dest.getParent())) {
                    Files.createDirectories(dest.getParent());
                }

                if(Files.exists(dest)) {
                    Files.delete(dest);
                }

                System.out.println("now copying: " + fileToCopy.toString());
                Files.copy(fileToCopy, dest);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initLastBackupTime() {
        try {
            if (!Files.exists(dateFile.toPath())) {
                Files.createDirectory(dateFile.toPath().getParent());
                mapper.writeValue(dateFile, 0);
                System.out.println("Previously, there was no backup. Creating dateFile");
            } else {
                lastBackupTime = mapper.readValue(dateFile, Long.class);
                System.out.println("Date of the previous backup = " + new Date(lastBackupTime));
            }
        } catch (IOException ex) {
            System.out.println("Error read date from dateFile");
            throw new RuntimeException(ex);
        }

    }

    private void recordCurrentDate() {
        try {
            mapper.writeValue(dateFile, new Date().getTime());
        } catch (IOException e) {
            System.out.println("Error record current date to file");
        }
    }

    private boolean askAgree() {
        System.out.println("\ncontinue? (y/n)");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            String answer = "";
            while (true) {
                answer = reader.readLine();
                if (answer.equalsIgnoreCase("y")) {
                    return true;
                } else if (answer.equalsIgnoreCase("n")) {
                    System.exit(666);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public PropertiesLoader getPropertiesLoader() {
        return propertiesLoader;
    }

    public void setPropertiesLoader(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public List<Path> getSources() {
        return sources;
    }

    public void setSources(List<Path> sources) {
        this.sources = sources;
    }

    public Path getDestination() {
        return destination;
    }

    public void setDestination(Path destination) {
        this.destination = destination;
    }

    public File getDateFile() {
        return dateFile;
    }

    public void setDateFile(File dateFile) {
        this.dateFile = dateFile;
    }
}
