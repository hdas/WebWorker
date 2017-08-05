package com.ksh.webwork;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hansaraj on 2/29/2016.
 */
public interface IDocumentInfo {
    String getUrl();
    String getString() throws IOException;
    InputStream getInputStream() throws IOException;
    WebElement getWebElement() throws IOException;
    JavascriptExecutor getJavascriptExecutor() throws IOException;
}
