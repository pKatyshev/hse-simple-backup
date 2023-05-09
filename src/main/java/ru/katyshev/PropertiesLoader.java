package ru.katyshev;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesLoader {
    private Backup backup;
    private Path destination;
    private File dateFile;
    private List<Path> sources = new ArrayList<>();
    private Properties properties = new Properties();
    private String path;
//    private String path = "src/main/resources/properties.properties";

    public PropertiesLoader(Backup backup, String path) {
        this.backup = backup;
        this.path = path;
    }

    public void loadProperties() {
        try {
            properties.load(new FileReader(path));
        } catch (IOException e) {
            System.out.println("couldn't read the file: " + path);
            throw new RuntimeException(e);
        }
        //reading DESTINATION
        destination = Path.of(properties.getProperty("destination"));
        backup.setDestination(destination);
        System.out.println("Backup will be made to the dirrectory: " + properties.getProperty("destination"));

        // reading last backup date
        dateFile = new File(properties.getProperty("dateFile"));
        backup.setDateFile(dateFile);

        //reading SOURCE`s from properties
        List<String> keys = properties.stringPropertyNames().stream().sorted().collect(Collectors.toList());
        Collections.sort(keys);
        for (int i = 3; i < keys.size(); i++) {
            String src = properties.getProperty(keys.get(i));
            System.out.println("add source: " + src);
            sources.add(Path.of(src));
        }
        backup.setSources(sources);
    }
}
