package com.tuanfadbg.trackprogress.ui.rate;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagInsertAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectNewestAsyncTask;
import com.tuanfadbg.trackprogress.utils.Constants;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;

import java.util.ArrayList;
import java.util.List;

public class RateDialog extends DialogFragment {

    public static final String TAG = RateDialog.class.getSimpleName();

    public RateDialog() {

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog_Animation_Up);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_rate, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar3);
        TextView txtCancel = view.findViewById(R.id.txt_cancel);
        TextView txtOk = view.findViewById(R.id.txt_ok);
        txtOk.setOnClickListener(v -> {
            if (ratingBar.getRating() == 0) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.please_give_5_star), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
                return;
            }
            if (ratingBar.getRating() > 4) {
                openRateApp();
                SharePreferentUtils.disableShowRate();
            } else {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.thanks_for_your_feed_back), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
            }
            dismiss();
        });
        txtCancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    public void openRateApp() {
        Context context = getActivity();
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }
}