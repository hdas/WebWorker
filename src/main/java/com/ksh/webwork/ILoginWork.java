package com.ksh.webwork;

import org.openqa.selenium.WebElement;

public interface ILoginWork extends IWebWork {
	boolean isLoginRedirectionDoc(WebElement page);
	public boolean enterLock();
	public void exitLock();
}
