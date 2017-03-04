package com.ksh.webwork;

import java.io.IOException;
import java.net.URI;

public interface IWebWork
{
    URI getInitialUri();
    int getTimeOutInMs();
    void setTimeOutInMs(final int timeout);
    boolean canHandle(final String url);
    void attach(final IPageLogger pageLogger);
    void detach(final IPageLogger pageLogger);

    WebWorkStatus onDocumentCompleted(final IDocumentInfo info) throws IOException;
}