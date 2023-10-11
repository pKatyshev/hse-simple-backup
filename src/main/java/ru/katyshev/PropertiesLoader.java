package ru.katyshev;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Getter
@Setter
public class PropertiesLoader {
    private Backup backup;
    private Path destination;
    private File dateFile;
    private List<Path> sources = new ArrayList<>();
    private Properties properties = new Properties();
    private final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public PropertiesLoader(Backup backup) {
        this.backup = backup;
    }

    public void loadProperties() {
        // load properties
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("properties.properties")){
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // reading DESTINATION
        destination = Path.of(properties.getProperty("destination"));
        backup.setDestination(destination);
        logger.info("Backup will be made to the dirrectory: " + properties.getProperty("destination"));

        // reading last backup date
        dateFile = new File(destination + "\\config\\lastBackupDate.json");
        backup.setDateFile(dateFile);

        // reading SOURCE`s from properties
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());

        for (String key : keys) {
            if (key.startsWith("source")) {
                String src = properties.getProperty(key);
                logger.info("add source: " + src);
                sources.add(Path.of(src));
            }
        }
        backup.setSources(sources);
    }
}
