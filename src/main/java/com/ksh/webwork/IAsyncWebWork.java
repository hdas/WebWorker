package com.ksh.webwork;

/**
 * Created by hansaraj on 19/03/16.
 */
public interface IAsyncWebWork extends IWebWork {
    void setEventHandler(IAsyncWebWorkCallback handler);
    IAsyncWebWorkCallback getEventHandler();
}
