package org.wordpress.android.editor;

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class HtmlStyleTextWatcherTest {

    HtmlStyleTextWatcherForTests mWatcher;
    SpannableStringBuilder mContent;
    CountDownLatch mCountDownLatch;
    HtmlStyleTextWatcher.SpanRange mSpanRange;

    @Before
    public void setUp() {
        mWatcher = new HtmlStyleTextWatcherForTests();
    }

    @Test
    public void testAddSingleTag() throws InterruptedException {
        // -- Test adding a tag to an empty document
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>");

        mWatcher.onTextChanged(mContent, 0, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(0, mSpanRange.getOpeningTagLoc());
        assertEquals(3, mSpanRange.getClosingTagLoc());


        // -- Test adding a tag at the end of a document with text
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("stuff<b>");

        mWatcher.onTextChanged(mContent, 5, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(5, mSpanRange.getOpeningTagLoc());
        assertEquals(8, mSpanRange.getClosingTagLoc());


        // -- Test adding a tag at the end of a document containing other html
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("some text <i>italics</i> <b>");

        mWatcher.onTextChanged(mContent, 25, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(25, mSpanRange.getOpeningTagLoc());
        assertEquals(28, mSpanRange.getClosingTagLoc());


        // -- Test adding a tag at the start of a document with text
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>some text");

        mWatcher.onTextChanged(mContent, 0, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(0, mSpanRange.getOpeningTagLoc());
        assertEquals(3, mSpanRange.getClosingTagLoc());


        // -- Test adding a tag at the start of a document containing other html
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>some text <i>italics</i>");

        mWatcher.onTextChanged(mContent, 0, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(0, mSpanRange.getOpeningTagLoc());
        assertEquals(3, mSpanRange.getClosingTagLoc());


        // -- Test adding a tag within another tag pair
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>some <i>text</b>");

        mWatcher.onTextChanged(mContent, 8, 0, 3);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(8, mSpanRange.getOpeningTagLoc());
        assertEquals(11, mSpanRange.getClosingTagLoc());


        // -- Test adding a closing tag within another tag pair
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>some <i>text</i></b>");

        mWatcher.onTextChanged(mContent, 15, 0, 4);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(15, mSpanRange.getOpeningTagLoc());
        assertEquals(19, mSpanRange.getClosingTagLoc());
    }

    @Test
    public void testPasteTagPair() throws InterruptedException {
        // -- Test pasting in a set of opening and closing tags at the end of the document
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("text <b></b>");

        mWatcher.onTextChanged(mContent, 5, 0, 7);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(5, mSpanRange.getOpeningTagLoc());
        assertEquals(12, mSpanRange.getClosingTagLoc());
    }

    @Test
    public void testInsertOpeningTag() throws InterruptedException {
        // -- Test placing an opening tag at the start of the document
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<");

        mWatcher.onTextChanged(mContent, 0, 0, 1);
        mWatcher.afterTextChanged(mContent);

        boolean updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
        assertEquals(false, updateSpansWasCalled);


        // -- Test adding an opening tag after another tag
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>text</b><");

        mWatcher.onTextChanged(mContent, 11, 0, 1);
        mWatcher.afterTextChanged(mContent);

        updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
        assertEquals(false, updateSpansWasCalled);
    }

    private class HtmlStyleTextWatcherForTests extends HtmlStyleTextWatcher {
        @Override
        protected void updateSpans(Spannable s, SpanRange spanRange) {
            mSpanRange = spanRange;
            mCountDownLatch.countDown();
        }
    }
}
