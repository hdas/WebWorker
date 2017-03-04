package com.ksh.webwork;

import java.io.IOException;
import java.util.Set;

import org.openqa.selenium.Cookie;

public interface IWebWorker {
	//boolean workOn(IWebWork work, boolean bDiscardIfAlready) throws IOException;
	WebWorkStatus workOn(IWebWork work) throws IOException;

	@Deprecated
	IWebWork getCurrentWork();

	IWebWork [] getPendingWorks();
	void setLogDir(String logDir);
	Set<Cookie> getCookies();
	void addCookies(Set<Cookie> cookies);

	//@Deprecated //Use setLoginWork instead
	//void attach(ILoginWork lw);
	void setLoginWork(ILoginWork lw);

	void cancel();
	void close();
	void setLogInfo(boolean b);
}
