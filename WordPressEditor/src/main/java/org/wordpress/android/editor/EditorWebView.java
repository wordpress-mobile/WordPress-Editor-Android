package org.wordpress.android.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

public class EditorWebView extends EditorWebViewAbstract {

    public EditorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    public void execJavaScriptFromString(String javaScript) {
        this.evaluateJavascript(javaScript, null);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean shouldSwitchToCompatibilityMode() {
        if (Build.VERSION.SDK_INT <= 19) {
            try {
                this.evaluateJavascript("", null);
            } catch (NoSuchMethodError | IllegalStateException e) {
                return true;
            }
        }
        return false;
    }
}