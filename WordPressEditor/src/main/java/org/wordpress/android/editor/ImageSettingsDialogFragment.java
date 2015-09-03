package org.wordpress.android.editor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.util.AppLog;

public class ImageSettingsDialogFragment extends DialogFragment {

    public static final int IMAGE_SETTINGS_DIALOG_REQUEST_CODE = 5;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_image_options_container, null);

        final EditText titleText = (EditText) view.findViewById(R.id.image_title);
        final EditText captionText = (EditText) view.findViewById(R.id.image_caption);
        final EditText altText = (EditText) view.findViewById(R.id.image_alt_text);
        final EditText linkTo = (EditText) view.findViewById(R.id.image_link_to);
        final CheckBox featuredCheckBox = (CheckBox) view.findViewById(R.id.featuredImage);
        final CheckBox featuredInPostCheckBox = (CheckBox) view.findViewById(R.id.featuredInPost);
        final Spinner alignmentSpinner = (Spinner) view.findViewById(R.id.alignment_spinner);

        builder.setView(view)
                .setTitle(getString(R.string.image_settings))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(getTargetRequestCode(), getTargetRequestCode(), intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ImageSettingsDialogFragment.this.getDialog().cancel();
                    }
                });

        // Fill the dialog with existing values
        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                JSONObject imageMeta = new JSONObject(bundle.getString("imageMeta"));

                titleText.setText(imageMeta.getString("title"));
                captionText.setText(imageMeta.getString("caption"));
                String alt = imageMeta.getString("alt");
                altText.setText(alt);
                linkTo.setText(imageMeta.getString("linkUrl"));
                imageMeta.getString("align");
                if (imageMeta.getString("align").equals("")) {
                    alignmentSpinner.setSelection(0);
                }
            } catch (JSONException e1) {
                AppLog.d(AppLog.T.EDITOR, "Missing JSON properties");
            }
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Resources res = getResources();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.colorControlActivated, typedValue, true);
        int controlActivatedColor = typedValue.data;

        // If a divider exists under the dialog title (API < 19 by default), change the divider and text color
        // to match the EditText underline color
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = getDialog().findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(controlActivatedColor);

            // Update the title text color
            final int titleId = res.getIdentifier("alertTitle", "id", "android");
            final TextView titleTextView = (TextView) getDialog().findViewById(titleId);
            if (titleTextView != null) {
                titleTextView.setTextColor(controlActivatedColor);
            }
        }
    }
}
