package com.ksh.webworkerimpl;

import com.ksh.webwork.IDocumentInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hansaraj on 19/03/16.
 */
class AsyncWebWorkerDocumentInfo implements IDocumentInfo {

    private final WebDriver webDriver;
    private String dataAsString;
    private final String windowHandle;
    //private String url;

    AsyncWebWorkerDocumentInfo(WebDriver webDriver, String windowHandle) {
        this.webDriver = webDriver;
        this.windowHandle = windowHandle;
        //this.url = url;
    }

    public String getWindowHandle(){
        return windowHandle;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new IOException("InputStream not supported for WebWorker");
    }

    @Override
    public synchronized WebElement getWebElement() throws IOException {
        webDriver.switchTo().window(windowHandle);
        return webDriver.findElement(By.tagName("body"));
    }

    @Override
    public JavascriptExecutor getJavascriptExecutor() throws IOException {
        return (JavascriptExecutor)webDriver;
    }

    @Override
    public synchronized String getUrl() {
        webDriver.switchTo().window(windowHandle);
        return webDriver.getCurrentUrl();
    }

    @Override
    public synchronized String getString() throws IOException {

        if (dataAsString == null) {
            webDriver.switchTo().window(windowHandle);
            dataAsString = webDriver.getPageSource();  //webElement.getAttribute("innerHTML");
        }
        return dataAsString;
    }
}

