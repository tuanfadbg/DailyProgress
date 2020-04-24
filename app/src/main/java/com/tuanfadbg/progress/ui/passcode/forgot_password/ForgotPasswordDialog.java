package com.tuanfadbg.progress.ui.passcode.forgot_password;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.utils.GMailSender;
import com.tuanfadbg.progress.utils.SharePreferentUtils;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPasswordDialog extends DialogFragment {

    TextView txtEmail;
    ProgressBar progressBar;
    ImageView imgLock;

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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_forgot_password, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtEmail = view.findViewById(R.id.edt_email);
        progressBar = view.findViewById(R.id.progressBar);
        imgLock = view.findViewById(R.id.imageView3);

        txtEmail.setText(SharePreferentUtils.getEmail());
        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.txt_send_email).setOnClickListener(v -> checkAndSendEmail());
    }

    private void checkAndSendEmail() {
        String email = SharePreferentUtils.getEmail();
        sendEmail(email);
    }

    private boolean isButtonSendEmailEnable = true;

    private void sendEmail(String email) {
        if (!isButtonSendEmailEnable)
            return;
        isButtonSendEmailEnable = false;
        imgLock.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender(
                        "contact.fadeveloper@gmail.com",
                        "tuanfa99");

                sender.sendMail("Forgot passcode", "Your passcode is " + SharePreferentUtils.createTempPasscode(),
                        "forgotpassword_noreply@gmail.com",
                        email);
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> {
                        imgLock.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        showDialogSuccess();
                    });
            } catch (Exception e) {
                isButtonSendEmailEnable = true;
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> {
                        imgLock.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        showDialogFail();
                    });
            }

        }).

                start();
    }

    private void showDialogSuccess() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(R.string.send_email_forgot_password_success);
        sweetAlertDialog.setConfirmText(getString(R.string.dialog_ok));
        sweetAlertDialog.setOnDismissListener(dialog -> dismiss());
        sweetAlertDialog.show();
    }

    private void showDialogFail() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
        sweetAlertDialog.setTitle(R.string.send_email_forgot_password_fail);
        sweetAlertDialog.setConfirmText(getString(R.string.dialog_ok));
        sweetAlertDialog.setOnDismissListener(dialog -> dismiss());
        sweetAlertDialog.show();
    }
}