package com.ksh.webworkerimpl;

import com.ksh.webwork.*;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncWebWorker implements IWebWorker {

    private WebDriver wb = null;
    private ILoginWork loginWork = null;
    //private IWebWork currentWork = null;
    private Queue<IWebWork> workQueue = new PriorityQueue<>(10);
    private Timer timer = new Timer();

    private int pageLogSeq = 0;
    private String logDir = null;
    private boolean logInfo = true;
    private static final Logger logger = LoggerFactory.getLogger(AsyncWebWorker.class);
    private Hashtable<IWebWork, String> workWindowHandle = new Hashtable<>(10);
    private Hashtable<String, Boolean> windowHandlesBusy = new Hashtable<>(2);
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public AsyncWebWorker() throws Exception {
        this(createWebDriver());
    }

    private static WebDriver createWebDriver() {
        //        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        //        capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

        //ChromeOptions cop = new ChromeOptions();
        WebDriver wd =
                //new RemoteWebDriverProxy(new URL("http://127.0.0.1:4444/wd/hub"), DesiredCapabilities.chrome());
                //new JBrowserDriver();
                //new InternetExplorerDriver(capabilities);
                new ChromeDriver();
        //new PhantomJSDriver();
        //new HtmlUnitDriver();
        return wd;
    }

    public AsyncWebWorker(WebDriver wd) throws Exception {
        this.wb = wd;
        wb.manage().timeouts().pageLoadTimeout(90, TimeUnit.SECONDS);
        windowHandlesBusy.put(wb.getWindowHandle(), false);
    }

    @Override
    public void setLoginWork(ILoginWork loginWork) {
        this.loginWork = loginWork;
    }

    @Override
    public void close() {
        this.wb.close();
        this.wb.quit();
    }

    @Override
    public void setLogInfo(boolean bLogInfo) {
        this.logInfo = bLogInfo;
    }

    public void setLogDir(String logDir) {
        if (logDir == null || logDir.isEmpty()) {
            this.logDir = null;
        } else if (logDir.endsWith(File.separator)) {
            this.logDir = logDir;
        } else {
            this.logDir = logDir + File.separator;
        }
    }

    @Deprecated
    public boolean isWorking() {
        return true;//(currentWork != null);
    }

    public IWebWork[] getPendingWorks() {
        return workQueue.toArray(new IWebWork[0]);
    }

    @Deprecated
    public IWebWork getCurrentWork() {
        return null; //currentWork;
    }

    public void cancel() {
        wb.close();
        workQueue.clear();
        //onWorkCompleted(WebWorkStatus.WebWorkCancelled);
    }

    public synchronized String assignWindow(IWebWork work){
        String currentHandle = null;
        if(workWindowHandle.containsKey(work)){
            currentHandle = workWindowHandle.get(work);
        }
        else {
            for (Map.Entry<String, Boolean> wh : windowHandlesBusy.entrySet()) {
                if (!wh.getValue()) {
                    currentHandle = wh.getKey();
                    break;
                }
            }

            if (currentHandle == null) {
                JavascriptExecutor jse = (JavascriptExecutor) wb;
                jse.executeScript("window.open();");
                for (String h : wb.getWindowHandles()) {
                    if (!windowHandlesBusy.containsKey(h)) {
                        //windowHandlesBusy.put(h, false);
                        currentHandle = h;
                        break;
                    }
                }
            }
            workWindowHandle.put(work, currentHandle);
        }

        windowHandlesBusy.put(currentHandle, true);

        return currentHandle;
    }

    public synchronized IDocumentInfo navigate(IWebWork forWork, URI uri) throws IOException {
        String currentHandle = assignWindow(forWork);

        windowHandlesBusy.put(currentHandle, true);
        wb.switchTo().window(currentHandle);
        wb.navigate().to(uri.toURL());
        return new AsyncWebWorkerDocumentInfo(this.wb, currentHandle);
    }

//    public synchronized AsyncWebWorkerDocumentInfo navigate(String url, String windowHandle) {
//        windowHandlesBusy.put(windowHandle, true);
//        wb.switchTo().window(windowHandle);
//        wb.navigate().to(url);
//        return new AsyncWebWorkerDocumentInfo(this.wb, windowHandle);
//    }

    public WebWorkStatus workOn(IWebWork work) throws IOException {
        //currentWork = work;
        //currentWork.attach(this);
        //_timer.Interval = _currentWork.getTimeOutInMs();
        //_timer.Start();

        synchronized (workQueue) {
            workQueue.add(work);
        }

        while (!workQueue.isEmpty()) {
            IWebWork w;

            synchronized (workQueue){
                w = workQueue.remove();
            }

            assignWindow(w);
            WebWorkerRunner runner = new WebWorkerRunner(this, w, loginWork);
            executor.execute(runner);
        }

        //Wait for work to complete
        int counter = 0;
        while (workWindowHandle.containsKey(work)){
            try {
                Thread.sleep(20);
                if(++counter % 500 == 0)
                    logger.warn("Waiting for work to complete: " + work);
            }
            catch (InterruptedException e){

            }
        }
        logger.info("Work completed: " + work);

        return WebWorkStatus.WebWorkMoreStepPending; //TODO: to return proper code
    }

    public WebWorkStatus workOn(IWebWork work, boolean bDiscardIfAlready) throws IOException {
        Class<?> workClass = work.getClass();
        if (bDiscardIfAlready) {
            for (IWebWork w : this.getPendingWorks()) {
                if (workClass.equals(w.getClass()))
                    return WebWorkStatus.WebWorkFailed;
            }
        }

        return workOn(work);
    }

    @Override
    public Set<Cookie> getCookies() {
        final Options op = wb.manage();
        return op.getCookies();
    }

    @Override
    public void addCookies(Set<Cookie> cookies) {
        final Options op = wb.manage();
        for (Cookie ck : cookies) {
            op.addCookie(ck);
        }
    }

    public synchronized void onWorkCompleted(IWebWork work, WebWorkStatus wst) {
        String currentWindowHandle = workWindowHandle.get(work);
        windowHandlesBusy.put(currentWindowHandle, false);
        workWindowHandle.remove(work);
    }


}
