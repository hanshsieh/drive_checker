package com.handoitasdf.drive_checker.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.SwingWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by icand on 2017/9/3.
 */
public class PropertiesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesProvider.class);

    private Properties properties = new Properties();
    private final File propertyFile;

    public PropertiesProvider(@Nonnull File propertyFile) {
        this.propertyFile = propertyFile;
        loadProperties();
    }

    private void loadProperties() {
        try (FileInputStream inputStream = new FileInputStream(propertyFile)) {
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            properties.load(reader);
        } catch (FileNotFoundException ex) {
            LOGGER.debug("Property file {} not found, ignore it", propertyFile.getPath(), ex);
        } catch (Exception ex) {
            LOGGER.error("Fail to load properties from file {}", propertyFile.getPath(), ex);
        }
    }

    @Nonnull
    public synchronized Optional<String> getProperty(@Nonnull String propertyName) {
        return Optional.ofNullable(properties.getProperty(propertyName));
    }

    public synchronized void setProperty(@Nonnull String propertyName, @Nonnull String propertyValue) {
        LOGGER.debug("Setting property {} to value {}", propertyName, propertyValue);
        properties.put(propertyName, propertyValue);
        storeProperties();
    }

    public synchronized void removeProperty(@Nonnull String propertyName) {
        LOGGER.debug("Removing property {}", propertyName);
        properties.remove(propertyName);
        storeProperties();
    }

    @Nonnull
    public synchronized Map<String, String> getProperties() {
        return properties.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()));
    }

    private void storeProperties() {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                LOGGER.debug("Storing properties to file {}", propertyFile.getPath());
                try (FileOutputStream outputStream = new FileOutputStream(propertyFile)) {
                    Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                    properties.store(writer, "Drive checker properties");
                } catch (Exception ex) {
                    LOGGER.error("Fail to store properties to file {}", propertyFile.getPath(), ex);
                }
                return null;
            }
        }.execute();

    }
}
