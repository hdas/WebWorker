package com.ksh.webworkerimpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverProxy extends RemoteWebDriver {

    private static final String SSN_PROP_FILE = "wdsession.prop";
    private static final String SSN_PROP = "prev_session_id";

    public RemoteWebDriverProxy(final URL remoteAddress, final Capabilities desiredCapabilities) {
        super(remoteAddress, desiredCapabilities);

        String prevSsnId = loadSessionId();
        if(prevSsnId != null) {
            String newSsnId = getSessionId().toString();
            try {
                setSessionId(prevSsnId);
                Set<String> windowHandles = getWindowHandles();
                if(!windowHandles.isEmpty()) {
                    setSessionId(newSsnId);
                    quit();
                    setSessionId(prevSsnId);
                }
            }
            catch (WebDriverException e) {
                Throwable cause = e.getCause();
                setSessionId(newSsnId);
            }
        }

        saveSessionId(getSessionId().toString());

        //quit(); setSessionId("aff7c1a3-cbf3-4b95-a5b0-6fd3cd0572d8");
    }

    private void saveSessionId(String sessionId) {
        Properties prop = new Properties();

        try {
            prop.setProperty(SSN_PROP, sessionId);
            prop.store(new FileOutputStream(SSN_PROP_FILE), "Saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadSessionId() {
        Properties prop = new Properties();
        String ss=null;
        try {
            //load a properties file
            prop.load(new FileInputStream(SSN_PROP_FILE));
            ss = prop.getProperty(SSN_PROP);

        } catch (IOException e) {

        }
        return ss;
    }
}
