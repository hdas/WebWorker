package com.ksh.webworkerimpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.ksh.webwork.*;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebWorker implements IWebWorker
{
    public class WebWorkerDoumentInfo implements IDocumentInfo {

        private WebDriver webDriver;
        private String dataAsString;
        //private String url;

        WebWorkerDoumentInfo(WebDriver webDriver) {
            this.webDriver = webDriver;
            //this.url = url;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new IOException("InputStream not supported for WebWorker");
        }

        @Override
        public WebElement getWebElement() throws IOException {
            return webDriver.findElement(By.tagName("body"));
        }

        @Override
        public String getUrl() {
            return webDriver.getCurrentUrl();
        }

        @Override
        public String getString() throws IOException {

            if (dataAsString == null) {
                if(webDriver != null)
                {
                    dataAsString = webDriver.getPageSource();  //webElement.getAttribute("innerHTML");
                }
            }
            return dataAsString;
        }
    }

    private WebDriver wb = null;
    private ILoginWork loginWork = null;
    private IWebWork currentWork = null;
    private Queue<IWebWork> workQueue = new PriorityQueue<>(10);
    private Timer timer = new Timer();
    private int loginTryCount = 0;
    private boolean loginLockAcquired = false;
    //private boolean bPageProcessPending = false;
    private int pageLogSeq = 0;

    private boolean logInfo = true;
    private static final Logger logger = LoggerFactory.getLogger(WebWorker.class);
    private static String base_dir = "~/.webworker";
    //private static final String logDir = base_dir + "webworkerlogs/";

    private int id = 1;

    static {
//        final String osname = System.getProperty("os.name");
//        if(osname.contains("Linux")){
//            base_dir = "/var/lib/spmj/";
//        }
//        else if(osname.contains("Mac")){
//            base_dir = "~/spmj/";
//            base_dir = base_dir.replace("~", System.getProperty("user.home"));
//        }
//        else if(osname.contains("Windows")){
//            base_dir = "D:/WSKshFinance/";
//        }

        base_dir = base_dir.replace("~", System.getProperty("user.home"));
    }

    public WebWorker() throws Exception
    {
        this(1);
    }

    public WebWorker(int id) throws Exception
    {
        this.id = id;
        this.wb = createDriver();
        //new RemoteWebDriverProxy(new URL("http://127.0.0.1:4444/wd/hub"), DesiredCapabilities.chrome());

        wb.manage().timeouts().pageLoadTimeout(3, TimeUnit.MINUTES);
    }

    private WebDriver createDriver(){
        if(System.getProperty("os.name").contains("Linux")) {
            //return createHtmlUnitDriver();
            return createChromeDriver();
        }
        else {
            //return createPhantomJsDriver();
            return createChromeDriver();
        }
    }

    private PhantomJSDriver createPhantomJsDriver() {
        final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36";
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "User-Agent", userAgent);
        PhantomJSDriver driver = new PhantomJSDriver(capabilities);
        return driver;
    }

    private HtmlUnitDriver createHtmlUnitDriver() {
        HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.BEST_SUPPORTED, true);
        return driver;
    }

    private ChromeDriver createChromeDriver(){
        ChromeOptions cop = new ChromeOptions();
        cop.addArguments("user-data-dir=" + base_dir + "chrome_user_data/" + id);
        return new ChromeDriver(cop);
    }

    public WebWorker(WebDriver wd) throws Exception
    {
        this.wb = wd;
        wb.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
    }

    @Override
    public void setLoginWork(ILoginWork loginWork)
    {
    	this.loginWork = loginWork;
    }

    @Override
    public void close() {
    	this.wb.close();
        this.wb.quit();
    }
    
    @Override
    public void setLogInfo(boolean bLogInfo)
    {
    	this.logInfo = bLogInfo;
    }

    @Deprecated
    public void setLogDir(String logDir)
    {
//    	if(logDir == null || logDir.isEmpty())
//    	{
//    		this.logDir = null;
//    	}
//    	else if(logDir.endsWith(File.separator))
//    	{
//    		this.logDir = logDir;
//    	}
//    	else
//    	{
//    		this.logDir = logDir + File.separator;
//    	}
    }
    
//    private void logPage(Page page, File file) throws IOException
//    {
//		FileOutputStream os = new FileOutputStream(file);
//		InputStream is = page.getWebResponse().getContentAsStream();
//
//		final short bufsize = 4096;
//		byte [] buff = new byte[bufsize];
//		int off = 0;
//		while(true)
//		{
//			int read = is.read(buff, off, bufsize);
//			if(read <= 0)
//				break;
//
//			os.write(buff, off, read);
//		}
//
//		is.close();
//		os.close();
//    }
//
//    private void logPage(HtmlPage page, File file) throws IOException
//    {
//    	page.save(file);
//    }
    
//	@Override
//    public void logPage(Page page) throws IOException
//    {
//		if(this.logDir == null)
//    		return;
//
//    	this.pageLogSeq++;
//
//    	String strUrl = page.getUrl().toString();
//
//    	String urlff = String.format("page%d.txt", this.pageLogSeq);
//    	FileWriter fw = new FileWriter(this.logDir + urlff);
//    	fw.write(strUrl);
//    	fw.append('\n');
//    	fw.close();
//
//    	//Now write file
//    	String ff = String.format("page%d.htm", this.pageLogSeq);
//    	String dd = String.format("page%d", this.pageLogSeq);
//		String fullPathname = this.logDir + ff;
//		String ddFullPathName = this.logDir + dd;
//		File file = new File(fullPathname);
//
//		if(file.exists())
//		{
//			file.delete();
//		}
//
//		File dir = new File(ddFullPathName);
//		if(dir.exists())
//		{
//			FileUtils.deleteDirectory(dir);
//		}
//
//		if(page instanceof HtmlPage)
//		{
//			this.logPage((HtmlPage)page, file);
//		}
//		else
//		{
//			this.logPage(page, file);
//		}
//    }

    private WebWorkStatus onPageReceived(final IDocumentInfo docInfo) throws IOException
    {
        //assert (page != null);
        WebWorkStatus wst = WebWorkStatus.WebWorkFailed;
        final String url = docInfo.getUrl();
           
        if(this.logInfo)
        	logger.info("Page received: " + url);
        
        if (loginTryCount > 2)
        {
            wst = WebWorkStatus.WebWorkLoginError;
        }
//        else if (url.getHost().equalsIgnoreCase("ieframe.dll"))
//        {
//            wst = WebWorkStatus.WebWorkNetworkError;
//            bWorkCompleted = true;
//        }
        else if (loginWork != null && loginWork.isLoginRedirectionDoc(docInfo.getWebElement()))
        {
            wst = WebWorkStatus.WebWorkMoreStepPending;
        }
        else if (currentWork != null && currentWork.canHandle(url))
        {
            wst = currentWork.onDocumentCompleted(docInfo);
        }
        else if (loginWork != null && loginWork.canHandle(url))
        {
            if (!loginLockAcquired)
            {
                loginLockAcquired = loginWork.enterLock();
            } 

            if (!loginLockAcquired)
            {
                return WebWorkStatus.WebWorkLoginError;
            }

            WebWorkStatus loginWst = loginWork.onDocumentCompleted(docInfo);
            if(loginWst == WebWorkStatus.WebWorkMoreStepPending)
            {
                wst = loginWst;
            }
            else if(loginWst == WebWorkStatus.WebWorkSuccessful && currentWork != null)
            {
                wst = this.workOn(currentWork);
            }
        }
        
        return wst;
    }

    private void onCurrentWorkCompleted(WebWorkStatus wst)
    {
        timer.cancel();
        
        if (loginLockAcquired)
        {
            loginWork.exitLock();
            loginLockAcquired = false;
        }

        //TODO: to create handler mechanism
        if(currentWork instanceof IAsyncWebWork){
            IAsyncWebWork asyncWebWork = (IAsyncWebWork)currentWork;
            final IAsyncWebWorkCallback handler = asyncWebWork.getEventHandler();
            if(handler != null)
                handler.onWebWorkEvent(currentWork, wst);
        }
        if(currentWork != null)
        	currentWork = null;

        if (wst != WebWorkStatus.WebWorkLoginError)
        {
            loginTryCount = 0;
        }
        //Look for next work

        try
        {
            if (!workQueue.isEmpty())
            {
                this.workOn(workQueue.peek());
            }
            else
            {
                currentWork = null;
            }
        }
        catch (Exception e)
        {
            currentWork = null;
        }
    }
    
//    private void _wb_Navigating(object sender, System.Windows.Forms.WebBrowserNavigatingEventArgs e)
//    {
//        if (_currentWork != null && _currentWork.CanHandle(e.Url)) //todo: This sequence has an bug, the login work and order entry work support a same url
//        {
//            _executingWork = _currentWork;
//        }
//        else if(_loginWork != null && _loginWork.CanHandle(e.Url))
//        {
//            _executingWork = _loginWork;
//
//            if (_loginWork.getInitialUri() == e.Url.AbsoluteUri)
//            {
//                _loginTryCount++;
//            }
//        }
//
//        if (_executingWork != null)
//        {
//            _timer.Stop();
//            _timer.Interval = _executingWork.TimeOutInMs;
//            _timer.Start();
//            _executingWork.OnNavigating(e.Url);
//        }
//    }


    public boolean isWorking()
    {
        return (currentWork != null);
    }

    public IWebWork [] getPendingWorks()
    {
        return workQueue.toArray(new IWebWork[0]);
    }

    @Deprecated
    public IWebWork getCurrentWork()
    {
        return currentWork;
    }

    public void cancel()
    {
    	wb.close();
        workQueue.clear();
        onCurrentWorkCompleted(WebWorkStatus.WebWorkCancelled);
    }

    @Override
    public WebWorkStatus workOn(IWebWork work) throws IOException
    {
    	currentWork = work;
        WebWorkStatus wst = WebWorkStatus.WebWorkFailed;

        try
        {
        	String url = currentWork.getInitialUri().toString();
        	if(this.logInfo)
        		logger.info("Opening " + url);

            //this.bPageProcessPending = true;
            wb.navigate().to(url);
            url = wb.getCurrentUrl();
            boolean workCompleted = false;
            while(!workCompleted)
            {
                //Workaround for HtmlUnitDrier
//                if(StringUtils.isEmpty(wb.getPageSource())){
//                    wb.get(wb.getCurrentUrl());
//                }

                final IDocumentInfo docInfo = new WebWorkerDoumentInfo(wb);
                wst = this.onPageReceived(docInfo);
                switch (wst){
                    case WebWorkMoreStepPending: {
                        workCompleted = false;
                        break;
                    }
                    default: workCompleted = true;
                }

                if(!workCompleted) {
                    final String newUrl = wb.getCurrentUrl();
                    if (newUrl.equalsIgnoreCase(url)) {
                        url = newUrl;
                        workCompleted = true;
                    }
                }
            }
        }
        catch(IOException e)
        {
        	logger.error("(IOException) Error executing work: " + currentWork + ". " + e.getMessage(), e);
            wst = WebWorkStatus.WebWorkFailed;
            //throw e;
        }
        catch (UnreachableBrowserException e) { //Happens when browser/(phantomjs) crashes
            //TODO: To close the worker and start again
            logger.error("(UnreachableBrowserException) Error executing work: " + currentWork + ". " + e.getMessage(), e);
            wst = WebWorkStatus.WebWorkFailed;
            if(wb != null) {
                wb.close();
                wb = createDriver();
            }
        }
        catch (NotFoundException e) { //Happens when no element found that requested
            logger.error("(NotFoundException) Error executing work: " + currentWork + ". " + e.getMessage(), e);
            wst = WebWorkStatus.WebWorkFailed;
        }
        finally {
            timer.cancel();
            currentWork = null;
            onCurrentWorkCompleted(wst);
        }
        return wst;
    }

    @Deprecated
    public boolean workOnXX(IWebWork work, boolean bDiscardIfAlready) throws IOException
    {
    	Class<?> workClass = work.getClass();
        if (bDiscardIfAlready)
        {
            for(IWebWork w : this.getPendingWorks())
            {
                if(workClass.equals(w.getClass()))
                    return false;
            }
        }

        boolean ret = false;
        if (currentWork == null)
        {
            this.workOn(work);
        }
        else
        {
            workQueue.add(work);
            ret = true;
        }
        return ret;
    }

//	@Override
//	public void webWindowClosed(WebWindowEvent evt) {
//	}
//
//	@Override
//	public void webWindowContentChanged(WebWindowEvent evt)
//	{
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        final URL url = evt.getNewPage().getUrl();
//		//logger.info("Window content changed:" + url); // + evt.getNewPage().getWebResponse().getContentAsString());
////        for(int i=0; i<100; i++){
////            final int st = evt.getNewPage().getWebResponse().getStatusCode();
////            logger.info(st);
////            try {
////                Thread.sleep(500);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////            if(st==200) break;
////        }
//
////        if(url.getPath().contains("login/login.jsp")) {
////            this.close();
////            logger.info("Window closed.");
////        }
//		//try {
//			this.bPageProcessPending = true;
//			//this.onPageReceived(evt.getNewPage());
//		//} catch (IOException e) {
//		//	e.printStackTrace();
//		//}
//	}

//	@Override
//	public void webWindowOpened(WebWindowEvent evt) {
//
//        if(evt.getNewPage() != null)
//            logger.info(evt.getNewPage().getUrl());
//	}

    @Override
	public Set<Cookie> getCookies() {
        final Options op = wb.manage();
        return op.getCookies();
	}

    @Override
    public void addCookies(Set<Cookie> cookies) {
        final Options op = wb.manage();
        for(Cookie ck: cookies){
            op.addCookie(ck);
        }
    }


}
