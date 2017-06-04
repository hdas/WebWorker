package com.ksh.webwork;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;

public abstract class WebWorkBase implements IWebWork, IAsyncWebWork
{
    private IAsyncWebWorkCallback handler;

    private final URI initialUri;
    private int _timeOutInMs = 40000; //40 Sec
    protected IPageLogger pageLogger = null;

    protected WebWorkBase(final URI initialUri) throws Exception
    {
    	this.initialUri = initialUri;
    }

    public abstract WebWorkStatus onDocumentCompleted(final IDocumentInfo info) throws IOException;
    
    @Override
    public void attach(IPageLogger pageLogger){
    	this.pageLogger = pageLogger;
    }
    
    @Override
    public void detach(IPageLogger pageLogger) {
    	if(this.pageLogger.equals(pageLogger))
    	{
    		this.pageLogger = null;
    	}
    }

    @Override
    public URI getInitialUri()
    {
        return initialUri;
    }

    @Override
    public boolean canHandle(final String url)
    {
        try {
            URI tmpUrl = new URI(url);
            if (tmpUrl.equals(initialUri))
                return true;

            String tfile = StringUtils.stripEnd(tmpUrl.getPath(), "/");
            String ifile = StringUtils.stripEnd(initialUri.getPath(), "/");

            String tqr = tmpUrl.getQuery();
            String iqr = initialUri.getQuery();
            if(StringUtils.isEmpty(tqr)) tqr = "";
            if(StringUtils.isEmpty(iqr)) iqr = "";

            if(tmpUrl.getHost().equals(initialUri.getHost())
                && tfile.equals(ifile)
                && tqr.equals(iqr))
                return true;
        }
        catch (URISyntaxException e){
            return false;
        }

        return false;
    }

    @Override
    public int getTimeOutInMs()
    {
        return _timeOutInMs;
    }
    
    @Override
    public void setTimeOutInMs(int timeout)
    {
    	_timeOutInMs = timeout;
    }
    
//    public boolean onNavigating(String url)
//    {
//        return true;
//    }

    protected static void wait(int milliSec)
    {
        Date dt = Calendar.getInstance().getTime();
        while (true)
        {
            Date dt2 = Calendar.getInstance().getTime();
            long msdiff = dt2.getTime() - dt.getTime();
            if(msdiff > milliSec)
                break;

            //Application.DoEvents();
            try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

//    protected static HtmlElement FindElement(HtmlElement root, String TagName)
//    {
//        HtmlElement eleRet = null;
//        String ltag = TagName.ToLower();
//
//        HtmlElementCollection col = root.All;
//        for (HtmlElement ele in col)
//        {
//            if (ele.TagName.ToLower() == ltag)
//            {
//                eleRet = ele;
//                break;
//            }
//        }
//        return eleRet;
//    }
//
//    protected static HtmlElement FindElement(HtmlElement root, String TagName, String Id)
//    {
//        HtmlElement eleRet = null;
//        String ltag = TagName.ToLower();
//        String lid = Id.ToLower();
//
//        HtmlElementCollection col = root.All;
//        for (HtmlElement ele in col)
//        {
//            if (String.IsNullOrEmpty(ele.Id))
//                continue;
//
//            if (ele.TagName.ToLower() == ltag
//                && ele.Id.ToLower() == lid)
//            {
//                eleRet = ele;
//                break;
//            }
//        }
//        return eleRet;
//    }
//
    protected static WebElement findElementByName(WebElement root, String tagName, String name) throws NotFoundException
    {
        WebElement eleRet = null;
        List<WebElement> eleList = root.findElements(By.tagName(tagName));
        for(WebElement ele : eleList)
        {
        	String eleName = ele.getAttribute("name");
        	if(name.equalsIgnoreCase(eleName))
        	{
        		eleRet = ele;
        		break;
        	}
        }
        if(eleRet == null)
	    	throw new NotFoundException("Could not find element with tag: " + tagName + " and name: " + name);
        
        return eleRet;
    }

    protected static WebElement findElementByType(WebElement root, String tagName, String strType) throws NotFoundException
    {
        WebElement eleRet = null;
	    List<WebElement> eleList = root.findElements(By.tagName(tagName));
	    for(WebElement ele : eleList)
	    {
	    	String eleType = ele.getAttribute("type");
	    	if(strType.equalsIgnoreCase(eleType))
	    	{
	    		eleRet = ele;
	    		break;
	    	}
	    }
	    if(eleRet == null)
	    	throw new NotFoundException("Could not find element with tag: " + tagName + " and type: " + strType);

        return eleRet;
    }

    /// <summary>
    /// This function does not work. The ele.getAttribute("Class") return empty String even there is a class attribute exists in the tag
    /// </summary>
    /// <param name="root"></param>
    /// <param name="TagName"></param>
    /// <param name="strClass"></param>
    /// <returns></returns>
    protected static WebElement findElementByClass(final WebElement root, final String tagName, final String strClass) throws NotFoundException
    {
    	WebElement eleRet = null;
	    List<WebElement> eleList = root.findElements(By.tagName(tagName));
	    for(WebElement ele : eleList)
	    {
	    	final String eleClass = ele.getAttribute("class");
	    	if(strClass.equalsIgnoreCase(eleClass))
	    	{
	    		eleRet = ele;
	    		break;
	    	}
	    }
	    
	    if(eleRet == null)
	    	throw new NotFoundException("Could not find element with tag: " + tagName + " and class: " + strClass);

        return eleRet;        
    }
    
    protected static WebElement findElementById(final WebElement root, final String tagName, final String strId) throws NotFoundException
    {
    	WebElement eleRet = null;
	    List<WebElement> eleList = root.findElements(By.tagName(tagName));
	    for(WebElement ele : eleList)
	    {
	    	final String eleId = ele.getAttribute("id");
	    	if(strId.equalsIgnoreCase(eleId))
	    	{
	    		eleRet = ele;
	    		break;
	    	}
	    }
	    
	    if(eleRet == null)
	    	throw new NotFoundException("Could not find element with tag: " + tagName + " and id: " + strId);

        return eleRet;        
    }

    @Override
    public void setEventHandler(IAsyncWebWorkCallback handler) {
        this.handler = handler;
    }

    @Override
    public IAsyncWebWorkCallback getEventHandler() {
        return this.handler;
    }
    
//    protected static void debugElement(WebElement root) throws IOException
//    {
//    	WebElement eleRet = null;
//	    Iterable<DomElement> eleList = root.findElements(By.)
//	    for(DomElement ele : eleList)
//	    {
//	    	String string = ele.getAttribute("class");
//	    	String name = ele.getNodeName();
//
//	    }
//    }
}
