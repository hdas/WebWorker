package com.ksh.webworkerimpl;

import com.ksh.webwork.*;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

/**
 * Created by hansaraj on 2/29/2016.
 */
public class SimpleWebWorker implements IWebWorker {

    public class SimpleDoumentInfo implements IDocumentInfo {

        private InputStream istrm;
        private String dataAsString;
        private String url;

        SimpleDoumentInfo(String url, InputStream istrm) {
            this.istrm = istrm;
            this.url = url;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return istrm;
        }

        @Override
        public WebElement getWebElement() throws IOException {
            throw new IOException("WebElement is not supported for SimpleWebWorker");
        }

        @Override
        public JavascriptExecutor getJavascriptExecutor() throws IOException {
            return null;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getString() throws IOException {
            if (dataAsString == null) {
                final byte[] buf = new byte[1024];
                int n = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while (-1 != (n = this.istrm.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                this.istrm.close();
                byte[] response = out.toByteArray();
                dataAsString = out.toString("UTF-8");
            }
            return dataAsString;
        }
    }

//    @Override
//    public boolean workOn(IWebWork work, boolean bDiscardIfAlready) throws IOException {
//        return false;
//    }

    @Override
    public WebWorkStatus workOn(IWebWork work) throws IOException {

        URI link = work.getInitialUri();

        BufferedInputStream bis = null;

        if (link.getScheme().equals("file")) {
            final String fileName = link.getHost().isEmpty() ?
                    link.getPath() ://for Mac/Linux
                    link.getHost() + ":" + link.getPath();  //for Windows

            bis = new BufferedInputStream(new FileInputStream(fileName));
        } else {
            //Code to download
            bis = new BufferedInputStream(link.toURL().openStream());
        }

        final IDocumentInfo sdi = new SimpleDoumentInfo(link.toString(), bis);
        work.onDocumentCompleted(sdi);
        return WebWorkStatus.WebWorkSuccessful;
    }

    @Override
    @Deprecated
    public IWebWork getCurrentWork() {
        return null;
    }

    @Override
    public IWebWork[] getPendingWorks() {
        return new IWebWork[0];
    }

    @Override
    public void setLogDir(String logDir) {

    }

    @Override
    public Set<Cookie> getCookies() {
        return null;
    }

    @Override
    public void addCookies(Set<Cookie> cookies) {

    }

    @Override
    public void setLoginWork(ILoginWork lw) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void close() {

    }

    @Override
    public void setLogInfo(boolean b) {

    }
}
