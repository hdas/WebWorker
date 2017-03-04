package com.ksh.webwork;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Deprecated
public class HtmlTable implements IHtmlTable {
    private WebElement _tableElement = null;

    private List<Object> _dataRowList = new ArrayList<>(10);

    private boolean bConsiderColSpan = false;
    private int colcount = 0;

    @Override
    public int getRowCount()
    {
        return _dataRowList.size();
    }

    /// This will optimize the processing
    @Override
    public String[] getRow(int index)
    {
        Object obj = _dataRowList.get(index);

        if(obj instanceof WebElement){
            _dataRowList.set(index, parseRow((WebElement)obj));
        }

        return (String[]) _dataRowList.get(index);
    }

    @Override
    public boolean isAtLeast(final int row, final int col) {
        return false;
    }

    public HtmlTable(WebElement tableElement){
        this(tableElement, true);
    }

    public HtmlTable(WebElement tableElement, boolean bConsiderColSpan)
    {
        this.bConsiderColSpan = bConsiderColSpan;
        _tableElement = tableElement;
        this.parse(_tableElement);
    }

    private boolean parse(final WebElement ele)
    {
        if (ele == null)
            return false;

        final List<WebElement> children = ele.findElements(By.xpath("*"));
        for(final WebElement child: children)
        {
            final String tag = child.getTagName();
            if (tag.equalsIgnoreCase("tr"))
            {
                //final String [] rdArr = parseRow(child);
                _dataRowList.add(child);
            }
            else if (tag.equalsIgnoreCase("tbody") || tag.equalsIgnoreCase("form"))
            {
                this.parse(child);
            }
        }
        return true;
    }

    private String [] parseRow(final WebElement trow)
    {
        final List<String> rowDataList = new ArrayList<>(2);
        final List<WebElement> children = trow.findElements(By.xpath("*"));
        for(final WebElement child : children)
        {
            final String tag = child.getTagName();
            if (tag.equalsIgnoreCase("td") || tag.equalsIgnoreCase("th"))
            {
                int r = 1;
                final String text = child.getText();

                if (this.bConsiderColSpan)
                {
                    final String colspan = child.getAttribute("colspan");
                    if(colspan != null && !colspan.isEmpty()) {
                        r = Integer.parseInt(colspan);
                    }
                }

                for (int i = 0; i < r; i++)
                    rowDataList.add(text);
            }
        }

        final int cnt = rowDataList.size();

        if (colcount < cnt)
            colcount = cnt;

        String[] rdArr = new String[colcount];

        for (int i=0; i<cnt; i++)
        {
            rdArr[i] = rowDataList.get(i);
        }

        return rdArr;
    }
}
