package org.wordpress.android.editor;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;

import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

public class HtmlStyleTextWatcher implements TextWatcher {
    private int mOffset;
    private CharSequence mModifiedText;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        int lastCharacterLocation = start + count - 1;
        if (s.length() > lastCharacterLocation && lastCharacterLocation >= 0) {
            if (after < count) {
                // Text was deleted
                mOffset = start;
                mModifiedText = s.subSequence(start + after, start + count);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int lastCharacterLocation = start + count - 1;
        if (s.length() > lastCharacterLocation) {
            if (count > before) {
                // Text was added
                mOffset = start;
                mModifiedText = s.subSequence(start + before, start + count);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mModifiedText == null) {
            AppLog.d(T.EDITOR, "mModifiedText was null");
            return;
        }

        SpanRange spanRange;

        // If the modified text included a tag or entity symbol ("<", ">", "&" or ";"), find its match and restyle
        if (mModifiedText.toString().contains("<")) {
            spanRange = restyleForChangedOpeningSymbol(s, "<");
        } else if (mModifiedText.toString().contains(">")) {
            spanRange = restyleForChangedClosingSymbol(s, ">");
        } else if (mModifiedText.toString().contains("&")) {
            spanRange = restyleForChangedOpeningSymbol(s, "&");
        } else if (mModifiedText.toString().contains(";")) {
            spanRange = restyleForChangedClosingSymbol(s, ";");
        } else {
            // If the modified text didn't include any tag or entity symbols, restyle if the modified text is inside
            // a tag or entity
            spanRange = restyleNormalTextIfWithinSymbols(s, "<", ">");
            if (spanRange == null) {
                spanRange = restyleNormalTextIfWithinSymbols(s, "&", ";");
            }
        }

        if (spanRange != null) {
            updateSpans(s, spanRange);
        }

        mModifiedText = null;
    }

    protected SpanRange restyleForChangedOpeningSymbol(Editable content, String openingSymbol) {
        String closingSymbol = getMatchingSymbol(openingSymbol);

        // Apply span from the first added/deleted opening symbol until the closing symbol in the content matching the
        // last added/deleted opening symbol
        // e.g. pasting "<b><" before "/b>" - we want the span to be applied from the first "<" until the end of "/b>"
        int firstOpeningTagLoc = mOffset + mModifiedText.toString().indexOf(openingSymbol);
        int lastOpeningTagLoc = mOffset + mModifiedText.toString().lastIndexOf(openingSymbol);

        int closingTagLoc = content.toString().indexOf(closingSymbol, lastOpeningTagLoc);
        if (closingTagLoc > 0) {
            return new SpanRange(firstOpeningTagLoc, closingTagLoc + 1);
        }
        return null;
    }

    protected SpanRange restyleForChangedClosingSymbol(Editable content, String closingSymbol) {
        String openingSymbol = getMatchingSymbol(closingSymbol);

        int firstClosingTagInModLoc = mOffset + mModifiedText.toString().indexOf(closingSymbol);
        int firstClosingTagAfterModLoc = content.toString().indexOf(closingSymbol, mOffset + mModifiedText.length());

        int openingTagLoc = content.toString().lastIndexOf(openingSymbol, firstClosingTagInModLoc - 1);
        if (openingTagLoc >= 0) {
            if (firstClosingTagAfterModLoc >= 0) {
                return new SpanRange(openingTagLoc, firstClosingTagAfterModLoc + 1);
            } else {
                return new SpanRange(openingTagLoc, content.length());
            }
        }
        return null;
    }

    protected SpanRange restyleNormalTextIfWithinSymbols(Editable content, String openingSymbol, String closingSymbol) {
        int openingTagLoc = content.toString().lastIndexOf(openingSymbol, mOffset);
        if (openingTagLoc >= 0) {
            int closingTagLoc = content.toString().indexOf(closingSymbol, openingTagLoc);
            if (closingTagLoc >= mOffset) {
                return new SpanRange(openingTagLoc, closingTagLoc + 1);
            }
        }
        return null;
    }

    protected void updateSpans(Spannable s, SpanRange spanRange) {
        int spanStart = spanRange.getOpeningTagLoc();
        int spanEnd = spanRange.getClosingTagLoc();

        HtmlStyleUtils.clearSpans(s, spanStart, spanEnd);
        HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
    }

    private String getMatchingSymbol(String symbol) {
        switch(symbol) {
            case "<":
                return ">";
            case ">":
                return "<";
            case "&":
                return ";";
            case ";":
                return "&";
            default:
                return "";
        }
    }

    protected static class SpanRange {
        private final int mOpeningTagLoc;
        private final int mClosingTagLoc;

        public SpanRange(int openingTagLoc, int closingTagLoc) {
            mOpeningTagLoc = openingTagLoc;
            mClosingTagLoc = closingTagLoc;
        }

        public int getOpeningTagLoc() {
            return mOpeningTagLoc;
        }

        public int getClosingTagLoc() {
            return mClosingTagLoc;
        }
    }
}