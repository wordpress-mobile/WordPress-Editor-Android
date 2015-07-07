package org.wordpress.android.editor;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

public class HtmlStyleTextWatcher implements TextWatcher {
    private int mStart;
    private CharSequence mModifiedText;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (s.length() > start + count - 1 && start + count - 1 > 0) {
            if (after < count) {
                mStart = start;
                mModifiedText = s.subSequence(start + after, start + count);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > start + count - 1) {
            if (count > before) {
                mStart = start;
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

        // If the modified text included a tag or entity symbol ("<", ">", "&" or ";"), find its match and restyle
        if (mModifiedText.toString().contains("<")) {
            restyleForChangedOpeningSymbol(s, "<");
        } else if (mModifiedText.toString().contains(">")) {
            restyleForChangedClosingSymbol(s, ">");
        } else if (mModifiedText.toString().contains("&")) {
            restyleForChangedOpeningSymbol(s, "&");
        } else if (mModifiedText.toString().contains(";")) {
            restyleForChangedClosingSymbol(s, ";");
        } else {
            // If the modified text didn't include any tag or entity symbols, restyle if the modified text is inside
            // a tag or entity
            if (!restyleNormalTextIfWithinSymbols(s, "<", ">")) {
                restyleNormalTextIfWithinSymbols(s, "&", ";");
            }
        }

        mModifiedText = null;
    }

    private void restyleForChangedOpeningSymbol(Editable content, String openingSymbol) {
        String closingSymbol = getMatchingSymbol(openingSymbol);

        int openingTagLoc = mModifiedText.toString().indexOf(openingSymbol);
        int closingTagLoc = content.toString().indexOf(closingSymbol, mStart + openingTagLoc);
        if (closingTagLoc > 0) {
            updateSpans(content, mStart + openingTagLoc, closingTagLoc + 1);
        }
    }

    private void restyleForChangedClosingSymbol(Editable content, String closingSymbol) {
        String openingSymbol = getMatchingSymbol(closingSymbol);

        int closingTagLoc = mModifiedText.toString().indexOf(closingSymbol);
        int openingTagLoc = content.toString().lastIndexOf(openingSymbol, mStart + closingTagLoc);
        if (openingTagLoc > 0) {
            updateSpans(content, openingTagLoc, mStart + closingTagLoc + 1);
        }
    }

    private boolean restyleNormalTextIfWithinSymbols(Editable content, String openingSymbol, String closingSymbol) {
        int openingTagLoc = content.toString().lastIndexOf(openingSymbol, mStart);
        if (openingTagLoc >= 0) {
            int closingTagLoc = content.toString().indexOf(closingSymbol, openingTagLoc);
            if (closingTagLoc >= mStart) {
                updateSpans(content, openingTagLoc, closingTagLoc + 1);
                return true;
            }
        }
        return false;
    }

    private void updateSpans(Spannable s, int spanStart, int spanEnd) {
        clearSpans(s, spanStart, spanEnd);
        HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
    }

    private void clearSpans(Spannable s, int spanStart, int spanEnd) {
        CharacterStyle[] spans = s.getSpans(spanStart, spanEnd, CharacterStyle.class);

        for (CharacterStyle span : spans) {
            if (span instanceof ForegroundColorSpan || span instanceof StyleSpan || span instanceof RelativeSizeSpan) {
                s.removeSpan(span);
            }
        }
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
}
