package com.ksh.webwork;

/**
 * Created by hansaraj on 19/03/16.
 */
public interface IAsyncWebWorkCallback {
    boolean onWebWorkEvent(IWebWork work, WebWorkStatus status);
}
