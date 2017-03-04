package com.ksh.webwork;

/**
 * Created by hansaraj on 2/29/2016.
 */
public interface IHtmlTable {
    int getRowCount();
    String[] getRow(int index);
    boolean isAtLeast(int row, int col);
}
