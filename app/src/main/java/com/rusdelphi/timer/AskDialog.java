package com.rusdelphi.timer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.util.List;

public class AskDialog extends DialogFragment {

    PackageManager manager;
    public boolean mSharedDialog, mRateDialog;


    boolean canShareText(boolean allowSmsMms, Intent intent) {
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);

        if (list != null && list.size() > 0) {
            if (allowSmsMms)
                return true;
            int handlersCount = 0;
            for (ResolveInfo li : list) {
                if (li != null && li.activityInfo != null
                        && li.activityInfo.packageName != null
                        && li.activityInfo.packageName.equals("com.android.mms")) {
                } else
                    ++handlersCount;
            }
            if (handlersCount > 0)
                return true;
        }
        return false;
    }

    private boolean MyStartActivity(Intent aIntent) {
        try {
            startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mRateDialog) {
            builder.setMessage(R.string.Vote_text)
                    .setPositiveButton(R.string.Vote_text_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    MainActivity.mAskRate = false;
                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW);
                                    // Try Google play
                                    intent.setData(Uri.parse(
                                            getString(R.string.Market_url)));
                                    if (!MyStartActivity(intent)) {
                                        // Market (Google play) app seems not
                                        // installed, let's try to open a
                                        // webbrowser
                                        intent.setData(Uri.parse(getString(R.string.URL_to_vote)));
                                        if (!MyStartActivity(intent)) {
                                            // Well if this also fails, we have
                                            // run
                                            // out of options, inform the user.
                                            Toast.makeText(getActivity(),
                                                    R.string.Market_error,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                    .setNegativeButton(R.string.Vote_text_cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    MainActivity.mAskRate = false;
                                }
                            })
                    .setNeutralButton(R.string.Text_later,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Тихо уходим
                                    MainActivity.mDontAsk = true;
                                }
                            });
        }
        if (mSharedDialog) {
            builder.setMessage(R.string.Share_text_ask)
                    .setPositiveButton(R.string.Share_text_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    MainActivity.mAskShare = false;
                                    Intent share = new Intent(
                                            Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TEXT,
                                            getString(R.string.URL_to_vote));
                                    share.setType("text/plain");
                                    if (canShareText(true, share)) {
                                        startActivityForResult(
                                                Intent.createChooser(share, getString(R.string.Share_via)
                                                ),
                                                0);
                                    } else {
                                        Toast.makeText(
                                                getActivity(),
                                                R.string.Share_error,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton(R.string.Share_text_cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    MainActivity.mAskShare = false;
                                }
                            })
                    .setNeutralButton(R.string.Text_later,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // Тихо уходим
                                    MainActivity.mDontAsk = true;
                                }
                            });
        }
        return builder.create();
    }
}
