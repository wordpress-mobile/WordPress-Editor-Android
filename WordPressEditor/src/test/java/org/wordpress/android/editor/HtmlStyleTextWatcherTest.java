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
    public void testAddTagFromFormatBar() throws InterruptedException {
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

        mWatcher.onTextChanged(mContent, 25, 0, 3); // Added "<b>"
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

        mWatcher.onTextChanged(mContent, 8, 0, 3); // Added <i>
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(8, mSpanRange.getOpeningTagLoc());
        assertEquals(11, mSpanRange.getClosingTagLoc());


        // -- Test adding a closing tag within another tag pair
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<b>some <i>text</i></b>");

        mWatcher.onTextChanged(mContent, 15, 0, 4); // Added "</i>"
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(15, mSpanRange.getOpeningTagLoc());
        assertEquals(19, mSpanRange.getClosingTagLoc());
    }

    @Test
    public void testAddingListTags() throws InterruptedException {
        // -- Test adding a list tag to an empty document
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<ul>\n\t<li>");

        mWatcher.onTextChanged(mContent, 0, 0, 10);
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(0, mSpanRange.getOpeningTagLoc());
        assertEquals(10, mSpanRange.getClosingTagLoc());


        // -- Test adding a closing list tag
        mCountDownLatch = new CountDownLatch(1);
        mContent = new SpannableStringBuilder("<ul>\n" + //5
                "\t<li>list item</li>\n" + //20
                "\t<li>another list item</li>\n" + //22
                "</ul>");

        mWatcher.onTextChanged(mContent, 47, 0, 11); // Added "</li>\n</ul>"
        mWatcher.afterTextChanged(mContent);

        mCountDownLatch.await();
        assertEquals(47, mSpanRange.getOpeningTagLoc());
        assertEquals(58, mSpanRange.getClosingTagLoc());
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
    public void testTypingInOpeningTag() throws InterruptedException {
        // Test with several different cases of pre-existing text
        String[] previousTextCases = new String[]{"", "plain text", "<i>",
                "<blockquote>some existing content</blockquote> "};
        for (String initialText : previousTextCases) {
            int offset = initialText.length();

            // -- Test typing in an opening tag symbol
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "<");

            mWatcher.onTextChanged(mContent, offset, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            boolean updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in the tag name
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "<b");

            mWatcher.onTextChanged(mContent, offset + 1, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in a closing tag symbol
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "<b>");

            mWatcher.onTextChanged(mContent, offset + 2, 0, 1);
            mWatcher.afterTextChanged(mContent);

            mCountDownLatch.await();
            assertEquals(offset, mSpanRange.getOpeningTagLoc());
            assertEquals(offset + 3, mSpanRange.getClosingTagLoc());
        }
    }

    @Test
    public void testTypingInClosingTag() throws InterruptedException {
        // Test with several different cases of pre-existing text
        String[] previousTextCases = new String[]{"<b>stuff", "plain text <b>stuff", "<i><b>stuff",
                "<blockquote>some existing content</blockquote> <b>stuff"};

        for (String initialText : previousTextCases) {
            int offset = initialText.length();

            // -- Test typing in an opening tag symbol
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "<");

            mWatcher.onTextChanged(mContent, offset, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            boolean updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in the closing tag slash
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "</");

            mWatcher.onTextChanged(mContent, offset + 1, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);

            // -- Test typing in the tag name
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "</b");

            mWatcher.onTextChanged(mContent, offset + 2, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in a closing tag symbol
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "</b>");

            mWatcher.onTextChanged(mContent, offset + 3, 0, 1);
            mWatcher.afterTextChanged(mContent);

            mCountDownLatch.await();
            assertEquals(offset, mSpanRange.getOpeningTagLoc());
            assertEquals(offset + 4, mSpanRange.getClosingTagLoc());

        }
    }

    @Test
    public void testTypingInEntity() throws InterruptedException {
        // Test with several different cases of pre-existing text
        String[] previousTextCases = new String[]{"", "plain text", "&rho;",
                "<blockquote>some existing content &dagger;</blockquote> "};
        for (String initialText : previousTextCases) {
            int offset = initialText.length();

            // -- Test typing in the entity's opening '&'
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "&");

            mWatcher.onTextChanged(mContent, offset, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            boolean updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in the entity's main text
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "&amp");

            mWatcher.onTextChanged(mContent, offset + 3, 0, 1);
            mWatcher.afterTextChanged(mContent);

            // No formatting should be applied/removed
            updateSpansWasCalled = mCountDownLatch.await(500, TimeUnit.MILLISECONDS);
            assertEquals(false, updateSpansWasCalled);


            // -- Test typing in the entity's closing ';'
            mCountDownLatch = new CountDownLatch(1);
            mContent = new SpannableStringBuilder(initialText + "&amp;");

            mWatcher.onTextChanged(mContent, offset + 4, 0, 1);
            mWatcher.afterTextChanged(mContent);

            mCountDownLatch.await();
            assertEquals(offset, mSpanRange.getOpeningTagLoc());
            assertEquals(offset + 5, mSpanRange.getClosingTagLoc());
        }
    }

    private class HtmlStyleTextWatcherForTests extends HtmlStyleTextWatcher {
        @Override
        protected void updateSpans(Spannable s, SpanRange spanRange) {
            mSpanRange = spanRange;
            mCountDownLatch.countDown();
        }
    }
}
