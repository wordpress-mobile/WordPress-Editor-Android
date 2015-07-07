package org.wordpress.android.editor;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

public class CustomTextWatcher implements TextWatcher {
    private int mStart;
    private CharSequence mModifiedText;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (s.length() > start+count-1 && start+count-1 > 0) {
            if (after < count) {
                mStart = start;
                mModifiedText = s.subSequence(start+after, start + count);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > start+count-1) {
            if (count > before) {
                mStart = start;
                mModifiedText = s.subSequence(start + before, start+count);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mModifiedText == null) {
            AppLog.d(T.EDITOR, "mModifiedText was null");
            return;
        }

        if (mModifiedText.toString().contains("<")) {
            int openingTagLoc = mModifiedText.toString().indexOf("<");
            int closingTagLoc = s.toString().indexOf(">", mStart + openingTagLoc);
            if (closingTagLoc > 0) {
                int spanStart = mStart + openingTagLoc;
                int spanEnd = closingTagLoc + 1;
                clearSpans(s, spanStart, spanEnd);
                HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
            }
        } else if (mModifiedText.toString().contains(">")) {
            int closingTagLoc = mModifiedText.toString().indexOf(">");
            int openingTagLoc = s.toString().lastIndexOf("<", mStart + closingTagLoc);
            if (openingTagLoc > 0) {
                int spanStart = openingTagLoc;
                int spanEnd = mStart + closingTagLoc + 1;
                clearSpans(s, spanStart, spanEnd);
                HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
            }
        } else if (mModifiedText.toString().contains("&")) {
            int openingTagLoc = mModifiedText.toString().indexOf("&");
            int closingTagLoc = s.toString().indexOf(";", mStart + openingTagLoc);
            if (closingTagLoc > 0) {
                int spanStart = mStart + openingTagLoc;
                int spanEnd = closingTagLoc + 1;
                clearSpans(s, spanStart, spanEnd);
                HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
            }
        } else if (mModifiedText.toString().contains(";")) {
            int closingTagLoc = mModifiedText.toString().indexOf(";");
            int openingTagLoc = s.toString().lastIndexOf("&", mStart + closingTagLoc);
            if (openingTagLoc > 0) {
                int spanStart = openingTagLoc;
                int spanEnd = mStart + closingTagLoc + 1;
                clearSpans(s, spanStart, spanEnd);
                HtmlStyleUtils.styleHtmlForDisplay(s, spanStart, spanEnd);
            }
        } else {
            int openingTagLoc = s.toString().lastIndexOf("<", mStart);
            if (openingTagLoc >= 0) {
                int closingTagLoc = s.toString().indexOf(">", openingTagLoc);
                if (closingTagLoc >= mStart) {
                    int spanEnd = closingTagLoc + 1;
                    clearSpans(s, openingTagLoc, spanEnd);
                    HtmlStyleUtils.styleHtmlForDisplay(s, openingTagLoc, spanEnd);
                } else {
                    openingTagLoc = s.toString().lastIndexOf("&", mStart);
                    if (openingTagLoc >= 0) {
                        closingTagLoc = s.toString().indexOf(";", openingTagLoc);
                        if (closingTagLoc >= mStart) {
                            int spanEnd = closingTagLoc + 1;
                            clearSpans(s, openingTagLoc, spanEnd);
                            HtmlStyleUtils.styleHtmlForDisplay(s, openingTagLoc, spanEnd);
                        }
                    }
                }
            }
        }

        mModifiedText = null;
    }

    private void clearSpans(Spannable s, int spanStart, int spanEnd) {
        CharacterStyle[] spans = s.getSpans(spanStart, spanEnd, CharacterStyle.class);

        for (CharacterStyle span : spans) {
            if (span instanceof ForegroundColorSpan || span instanceof  StyleSpan) {
                s.removeSpan(span);
            }
        }
    }
}
