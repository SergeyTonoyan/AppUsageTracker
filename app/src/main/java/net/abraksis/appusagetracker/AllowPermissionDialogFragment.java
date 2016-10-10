package net.abraksis.appusagetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class AllowPermissionDialogFragment extends android.support.v4.app.DialogFragment implements
        DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(R.string.permission_dialog_title);
        dialogBuilder.setMessage(R.string.allow_permission_dialog_message);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setInverseBackgroundForced(true);
        dialogBuilder.setPositiveButton(R.string.ok_button_caption, this);
        dialogBuilder.setNegativeButton(R.string.cancel_button_caption, this);
        Dialog dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {

        if (button == BUTTON_POSITIVE) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }
}
