package com.capubbs.lib;

import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import org.xml.sax.XMLReader;
import java.util.Stack;

public class CenterTagHandler implements Html.TagHandler {
    
    private Stack<Integer> startIndices = new Stack<>();
    private Stack<String> tagStack = new Stack<>();
    
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("center")) {
            if (opening) {
                startIndices.push(output.length());
                tagStack.push("center");
            } else {
                if (!startIndices.isEmpty()) {
                    int startIndex = startIndices.pop();
                    tagStack.pop();
                    int stopIndex = output.length();
                    if (startIndex < stopIndex) {
                        output.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                startIndex, stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        } else if (tag.equalsIgnoreCase("right")) {
            if (opening) {
                startIndices.push(output.length());
                tagStack.push("right");
            } else {
                if (!startIndices.isEmpty()) {
                    int startIndex = startIndices.pop();
                    tagStack.pop();
                    int stopIndex = output.length();
                    if (startIndex < stopIndex) {
                        output.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                                startIndex, stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }
}