package com.ksh.webworkerimpl;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Created by hansaraj on 26-05-2016.
 */
public class MyHtmlUnitDriver extends HtmlUnitDriver {
    public MyHtmlUnitDriver(BrowserVersion version, boolean enableJavascript) {
        super(version, enableJavascript);
        this.getWebClient().waitForBackgroundJavaScript(5000);
        this.getWebClient().setJavaScriptTimeout(5000);
        this.getWebClient().waitForBackgroundJavaScriptStartingBefore(1000);

        this.getWebClient().setAjaxController(new NicelyResynchronizingAjaxController());
//        this.getWebClient().setAjaxController(new AjaxController(){
//            @Override
//            public boolean processSynchron(HtmlPage page, WebRequest request, boolean async)
//            {
//                return true;
//            }
//        });

    }
}
