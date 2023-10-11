package ru.katyshev;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PropertiesLoaderTest {
    @BeforeClass
    public static void configLogger() {
        PropertyConfigurator.configure(
                PropertiesLoaderTest
                        .class
                        .getClassLoader()
                        .getResource("properties.properties"));
    }

    @Test
    public void loadPropertiesTest() {
        PropertiesLoader propertiesLoader = new PropertiesLoader(new Backup());

        propertiesLoader.loadProperties();

        Assert.assertNotNull(propertiesLoader.getDestination());
        Assert.assertNotNull(propertiesLoader.getDateFile());
        Assert.assertNotNull(propertiesLoader.getSources());
        Assert.assertNotNull(propertiesLoader.getProperties());

        Assert.assertTrue(propertiesLoader.getSources().size() >= 1);
        Assert.assertTrue(propertiesLoader.getProperties().size() >= 1);
    }

}