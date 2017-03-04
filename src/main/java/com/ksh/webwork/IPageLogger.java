package com.ksh.webwork;

import java.io.IOException;

import org.openqa.selenium.WebElement;

public interface IPageLogger {
	public void logPage(WebElement page) throws IOException;
}
