package com.ksh.webwork;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class HtmlTable2 implements IHtmlTable {
    //private final List<Object> _dataRowList = new ArrayList<>(10);

    private final List<Element> unparsedRowList = new ArrayList<>(10);
    private final List<List<Element>> elementRowList = new ArrayList<>(10);

    private boolean bConsiderColSpan = true;
    private int colcount = 0;
    private Document tableDoc = null;

    public HtmlTable2(WebElement tableElement){
        this(tableElement, true);
    }

    public HtmlTable2(Element tableElement){
        this.parse(tableElement);
    }

    public HtmlTable2(WebElement tableElement, boolean bConsiderColSpan)
    {
        String source =
                "<table>" + tableElement.getAttribute("innerHTML") + "<table>";
        tableDoc = Jsoup.parse(source, "UTF-8");
        this.bConsiderColSpan = bConsiderColSpan;
        this.parse(tableDoc.body());
    }

    @Override
    public int getRowCount()
    {
        return unparsedRowList.size();
    }

    @Override
    public boolean isAtLeast(final int row, final int col) {
        if(getRowCount() < row)
            return false;

        if(getRow(0).length < col)
            return false;

        return true;
    }

    /// This will optimize the processing
    @Override
    public String[] getRow(int index)
    {
        List<Element> rowElements = getRowElements(index);

        String [] texts = new String[rowElements.size()];
        for(int i=0; i<rowElements.size(); i++)
        {
            texts[i] = rowElements.get(i).text();
        }

        return texts;
    }

    public List<Element> getRowElements(int row){

        if(elementRowList.size() < unparsedRowList.size()
            && row < unparsedRowList.size()){

            for(int i = elementRowList.size(); i<=row; i++) {
                List<Element> elements = parseRowElements(unparsedRowList.get(row));
                elementRowList.add(elements);
            }
        }

        return elementRowList.get(row);
    }

    private boolean parse(final Element ele)
    {
        if (ele == null)
            return false;

        final List<Node> children = ele.childNodes();
        for(final Node c: children)
        {
            if(!(c instanceof Element))
                continue;
            Element child = (Element)c;

            final String tag = child.nodeName(); // .getTagName();
            if (tag.equalsIgnoreCase("tr"))
            {
                //final String [] rdArr = parseRow(child);
                //_dataRowList.add(child);
                unparsedRowList.add(child);
            }
            else if (tag.equalsIgnoreCase("tbody")
                            || tag.equalsIgnoreCase("table")
                            //|| tag.equalsIgnoreCase("form")
                    )
            {
                this.parse(child);
            }
        }
        return true;
    }

    private List<Element> parseRowElements(final Node trow)
    {
        final List<Element> rowDataList = new ArrayList<>(2);
        final List<Node> children = trow.childNodes();
        for(final Node c : children)
        {
            if(!(c instanceof Element))
                continue;
            Element child = (Element)c;
            final String tag = child.tagName();
            if (tag.equalsIgnoreCase("td") || tag.equalsIgnoreCase("th"))
            {
                int r = 1;
                if (this.bConsiderColSpan)
                {
                    final String colspan = child.attr("colspan");
                    if(colspan != null && !colspan.isEmpty()) {
                        r = Integer.parseInt(colspan);
                    }
                }

                for (int i = 0; i < r; i++)
                    rowDataList.add(child);
            }
        }

        final int cnt = rowDataList.size();

        if (colcount < cnt)
            colcount = cnt;

        return rowDataList;
    }

    @Deprecated
    private String [] parseRow(final Node trow)
    {
        final List<String> rowDataList = new ArrayList<>(2);
        final List<Node> children = trow.childNodes();
        for(final Node c : children)
        {
            if(!(c instanceof Element))
                continue;
            Element child = (Element)c;
            final String tag = child.tagName();
            if (tag.equalsIgnoreCase("td") || tag.equalsIgnoreCase("th"))
            {
                int r = 1;
                final String text = child.text();

                if (this.bConsiderColSpan)
                {
                    final String colspan = child.attr("colspan");
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
