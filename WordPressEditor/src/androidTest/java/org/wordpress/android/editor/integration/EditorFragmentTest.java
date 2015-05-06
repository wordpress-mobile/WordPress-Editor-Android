package org.wordpress.android.editor.integration;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ToggleButton;

import org.wordpress.android.editor.R;

import java.util.HashMap;
import java.util.Map;

import static org.wordpress.android.editor.TestingUtils.waitFor;

public class EditorFragmentTest extends ActivityInstrumentationTestCase2<MockEditorActivity> {
    Activity mActivity;
    EditorFragmentForTests mFragment;

    public EditorFragmentTest() {
        super(MockEditorActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mFragment = (EditorFragmentForTests) mActivity.getFragmentManager().findFragmentByTag("editorFragment");
    }

    public void testDomLoadedCallbackReceived() {
        // initJsEditor() should have been called on setup
        assertTrue(mFragment.mInitCalled);

        long start = System.currentTimeMillis();
        while(!mFragment.mDomLoaded) {
            waitFor(10);
            if (System.currentTimeMillis() - start > 5000) {
                throw(new RuntimeException("Callback wait timed out"));
            }
        }

        // The JS editor should have sent out a callback when the DOM loaded, triggering onDomLoaded()
        assertTrue(mFragment.mDomLoaded);
    }

    public void testFormatBarToggledOnSelectedFieldChanged() {
        Map<String, String> selectionArgs = new HashMap<>();

        selectionArgs.put("id", "zss_field_title");
        mFragment.onSelectionChanged(selectionArgs);

        waitFor(100);

        ToggleButton boldButton = (ToggleButton) mFragment.getView().findViewById(R.id.format_bar_button_bold);
        assertFalse(boldButton.isEnabled());

        selectionArgs.clear();
        selectionArgs.put("id", "zss_field_content");
        mFragment.onSelectionChanged(selectionArgs);

        waitFor(100);

        assertTrue(boldButton.isEnabled());
    }
}