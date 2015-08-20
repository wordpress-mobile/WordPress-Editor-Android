package org.wordpress.android.editor.example;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.wordpress.android.editor.EditorFragmentAbstract;
import org.wordpress.android.editor.EditorFragmentAbstract.EditorFragmentListener;
import org.wordpress.android.editor.EditorMediaUploadListener;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.android.util.helpers.MediaFile;

public class EditorExampleActivity extends AppCompatActivity implements EditorFragmentListener {
    public static final String EDITOR_PARAM = "EDITOR_PARAM";
    public static final String TITLE_PARAM = "TITLE_PARAM";
    public static final String CONTENT_PARAM = "CONTENT_PARAM";
    public static final String DRAFT_PARAM = "DRAFT_PARAM";
    public static final String TITLE_PLACEHOLDER_PARAM = "TITLE_PLACEHOLDER_PARAM";
    public static final String CONTENT_PLACEHOLDER_PARAM = "CONTENT_PLACEHOLDER_PARAM";
    public static final int USE_NEW_EDITOR = 1;
    public static final int USE_LEGACY_EDITOR = 2;

    public static final int ADD_MEDIA_ACTIVITY_REQUEST_CODE = 1111;

    private EditorFragmentAbstract mEditorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getIntExtra(EDITOR_PARAM, USE_NEW_EDITOR) == USE_NEW_EDITOR) {
            ToastUtils.showToast(this, R.string.starting_new_editor);
            setContentView(R.layout.activity_new_editor);
        } else {
            ToastUtils.showToast(this, R.string.starting_legacy_editor);
            setContentView(R.layout.activity_legacy_editor);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof EditorFragmentAbstract) {
            mEditorFragment = (EditorFragmentAbstract) fragment;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == ADD_MEDIA_ACTIVITY_REQUEST_CODE) {
            final Uri imageUri = data.getData();

            MediaFile mediaFile = new MediaFile();
            final String mediaId = String.valueOf(System.currentTimeMillis());
            mediaFile.setMediaId(mediaId);

            mEditorFragment.appendMediaFile(mediaFile, imageUri.toString(), null);

            if (mEditorFragment instanceof EditorMediaUploadListener) {
                simulateFileUpload(mediaId, imageUri.toString());
            }
        }
    }

    @Override
    public void onSettingsClicked() {
        // TODO
    }

    @Override
    public void onAddMediaClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(intent, "Pick photo");

        startActivityForResult(intent, ADD_MEDIA_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onEditorFragmentInitialized() {
        // arbitrary setup
        mEditorFragment.setFeaturedImageSupported(true);
        mEditorFragment.setBlogSettingMaxImageWidth("600");

        // get title and content and draft switch
        String title = getIntent().getStringExtra(TITLE_PARAM);
        String content = getIntent().getStringExtra(CONTENT_PARAM);
        boolean isLocalDraft = getIntent().getBooleanExtra(DRAFT_PARAM, true);
        mEditorFragment.setTitle(title);
        mEditorFragment.setContent(content);
        mEditorFragment.setTitlePlaceholder(getIntent().getStringExtra(TITLE_PLACEHOLDER_PARAM));
        mEditorFragment.setContentPlaceholder(getIntent().getStringExtra(CONTENT_PLACEHOLDER_PARAM));
        mEditorFragment.setLocalDraft(isLocalDraft);
    }

    @Override
    public void saveMediaFile(MediaFile mediaFile) {
        // TODO
    }

    private void simulateFileUpload(final String mediaId, final String mediaUrl) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    float count = (float) 0.1;
                    while (count < 1.1) {
                        sleep(500);

                        ((EditorMediaUploadListener) mEditorFragment).onMediaUploadProgress(mediaId, count);

                        count += 0.1;
                    }

                    ((EditorMediaUploadListener) mEditorFragment).onMediaUploadSucceeded(mediaId, mediaUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
