package com.cabatuan.dismathoid2014;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by cobalt on 9/13/14.
 */
public class ScoreDialogFragment extends DialogFragment {

    public static ScoreDialogFragment newInstance(String title, String message, int icon) {
        ScoreDialogFragment frag = new ScoreDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putInt("icon", icon);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        int icon = getArguments().getInt("icon");

        return new AlertDialog.Builder(getActivity())
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.score_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((MainActivity) getActivity())
                                        .playAgain();
                            }
                        })
                .setNegativeButton(R.string.score_dialog_key,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((MainActivity) getActivity())
                                        .showAnswers();
                            }
                        }).create();
    }


}

