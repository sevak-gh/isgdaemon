package com.infotech.isg;

import com.infotech.isg.service.ISGBalanceService;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gets ISG balance for service providers
 *
 * @author Sevak Gharibian
 */
public class App {

    private static final String INI_FILE_PATH = "/etc/isg/isgdaemon.ini";
    private ApplicationContext context;

    public static void main(String[] args) {
        App app = new App();
        app.init();
        app.goWithScheduler();
    }

    private void init() {
        loadProperties();
        context = new ClassPathXmlApplicationContext("/spring/applicationContext.xml");
    }

    private void goWithScheduler() {
        // do nothing
    }

    private void loadProperties() {
        InputStream input = null;
        try {
            input = new FileInputStream(INI_FILE_PATH);
            Properties properties = new Properties(System.getProperties());
            properties.load(input);
            System.setProperties(properties);
            LoggerFactory.getLogger(App.class).debug("properties file: {} loaded successfully", INI_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("error in loading properties from file: " + INI_FILE_PATH, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                //TODO: there is nothing to de here!!!
            }
        }
    }
}
