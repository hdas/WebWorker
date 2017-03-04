package com.ksh.webworkerimpl;

import com.ksh.webwork.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;

/**
 * Created by hansaraj on 19/03/16.
 */
class WebWorkerRunner implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WebWorkerRunner.class);

    private final AsyncWebWorker worker;
    private final IWebWork work;
    private ILoginWork loginWork = null;
    private Timer timer = new Timer();

    private int loginTryCount = 0;
    private boolean loginLockAcquired = false;
    private IDocumentInfo docInfo;


    public WebWorkerRunner(AsyncWebWorker worker, IWebWork work, ILoginWork loginWork){
        this.worker = worker;
        this.work = work;
        this.loginWork = loginWork;
    }

    @Override
    public void run() {

        WebWorkStatus wst = WebWorkStatus.WebWorkFailed;
        try {
            worker.assignWindow(work);
            String currentUrl = work.getInitialUri().toString();
            logger.info("Opening " + currentUrl);
            docInfo = worker.navigate(work, work.getInitialUri());
            currentUrl = docInfo.getUrl();
            while (true) {
                Thread.sleep(16);
                wst = this.onPageReceived(docInfo);
                if(wst == WebWorkStatus.WebWorkMoreStepPending)
                    continue;

                final String nowUrl = docInfo.getUrl();
                if (!nowUrl.equalsIgnoreCase(currentUrl)) {
                    currentUrl = nowUrl;
                    continue;
                }
                break;
            }
        } catch (Exception e) {
            logger.error("Error executing work: " + work + ". " + e.getMessage(), e);
            wst = WebWorkStatus.WebWorkFailed;
        }
        finally {
            onWorkCompleted(wst);
        }

    }

    private WebWorkStatus onPageReceived(final IDocumentInfo docInfo) throws IOException {
        WebWorkStatus wst = WebWorkStatus.WebWorkFailed;

        final String url = docInfo.getUrl();
        logger.info("Page received: " + url);

        if (loginTryCount > 2) {
            wst = WebWorkStatus.WebWorkLoginError;
            //bWorkCompleted = true;
        }
        else if(url.equals("about:blank")){  //Happens for chrome
            wst = WebWorkStatus.WebWorkNetworkError;
            //bWorkCompleted = true;
        }
        else if (loginWork != null && loginWork.isLoginRedirectionDoc(docInfo.getWebElement())) {
            //_timer.Stop();
            //_timer.Interval = _loginWork.getTimeOutInMs();
            //_timer.Start();
            wst = WebWorkStatus.WebWorkMoreStepPending;
        } else if (work.canHandle(url)) {
            wst = work.onDocumentCompleted(docInfo);
//            if (wst != WebWorkStatus.WebWorkMoreStepPending) {
//                //bWorkCompleted = true;
//            }
        } else if (loginWork != null && loginWork.canHandle(url)) {
            if (!loginLockAcquired) {
                loginLockAcquired = loginWork.enterLock();
            }

            if (!loginLockAcquired) {
                wst = WebWorkStatus.WebWorkLoginError;
                //bWorkCompleted = true;
            } else if (loginWork.onDocumentCompleted(docInfo) == WebWorkStatus.WebWorkSuccessful) {
                this.docInfo = this.worker.navigate(work, work.getInitialUri());
                wst = WebWorkStatus.WebWorkMoreStepPending;
                //wst = this.onPageReceived(documentInfo);
                //_timer.Stop();
                //_timer.Interval = _currentWork.TimeOutInMs;
                //_timer.Start();
            }
        }

        return wst;

//        if (wst == WebWorkStatus.WebWorkMoreStepPending)
//            this.bPageProcessPending = true;
//
//        //Now raising the event
//        if (bWorkCompleted && work != null) {
//            onWorkCompleted(wst);
//        }
    }

    private void onWorkCompleted(WebWorkStatus wst) {
        timer.cancel();

        if (loginLockAcquired) {
            loginWork.exitLock();
            loginLockAcquired = false;
        }

        if(work instanceof IAsyncWebWork){
            IAsyncWebWork asyncWebWork = (IAsyncWebWork)work;
            final IAsyncWebWorkCallback handler = asyncWebWork.getEventHandler();
            if(handler != null)
                handler.onWebWorkEvent(work, wst);
        }

        if (wst != WebWorkStatus.WebWorkLoginError) {
            loginTryCount = 0;
        }
        this.worker.onWorkCompleted(work, wst);
    }
}
