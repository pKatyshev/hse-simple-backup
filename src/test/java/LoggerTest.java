import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {

    @Test
    public void loggerTest() {
        Logger logger = LoggerFactory.getLogger(LoggerTest.class);
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("properties.properties"));
        logger.info("This is good");
    }
}
