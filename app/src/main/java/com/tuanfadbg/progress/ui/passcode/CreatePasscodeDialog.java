package com.tuanfadbg.progress.ui.passcode;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.utils.SharePreferentUtils;
import com.tuanfadbg.progress.utils.Utils;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CreatePasscodeDialog extends DialogFragment {

    private TextView txtTitle;
    private EditText edtPass;
    private View v1, v2, v3, v4;
    private String passcode;
    private ConstraintLayout ctEmail;
    private EditText edtEmail;
    private OnPasscodeSetup onPasscodeSetup;

    public CreatePasscodeDialog() {

    }

    public CreatePasscodeDialog(OnPasscodeSetup onPasscodeSetup) {
        this.onPasscodeSetup = onPasscodeSetup;
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_create_passcode, container, false);
        return view;
    }

    View view;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        setCancelable(false);
        txtTitle = view.findViewById(R.id.textView11);
        edtPass = view.findViewById(R.id.edt_pass);
        ctEmail = view.findViewById(R.id.ct_enter_email);
        edtEmail = view.findViewById(R.id.edt_email);

        view.findViewById(R.id.txt_forgot_password).setVisibility(View.INVISIBLE);
        ctEmail.setVisibility(View.GONE);

        v1 = view.findViewById(R.id.view_1);
        v2 = view.findViewById(R.id.view_2);
        v3 = view.findViewById(R.id.view_3);
        v4 = view.findViewById(R.id.view_4);

        edtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pass = edtPass.getText().toString().trim();
                if (pass.length() == 0) {
                    v1.setSelected(false);
                    v2.setSelected(false);
                    v3.setSelected(false);
                    v4.setSelected(false);
                    return;
                }

                if (pass.length() == 1) {
                    v1.setSelected(true);
                    v2.setSelected(false);
                    v3.setSelected(false);
                    v4.setSelected(false);
                }

                if (pass.length() == 2) {
                    v1.setSelected(true);
                    v2.setSelected(true);
                    v3.setSelected(false);
                    v4.setSelected(false);
                }

                if (pass.length() == 3) {
                    v1.setSelected(true);
                    v2.setSelected(true);
                    v3.setSelected(true);
                    v4.setSelected(false);
                }

                if (pass.length() == 4) {
                    v1.setSelected(true);
                    v2.setSelected(true);
                    v3.setSelected(true);
                    v4.setSelected(true);
                    if (TextUtils.isEmpty(passcode))
                        showReEnterPassword();
                    else {
                        checkPassword();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setClickNumber(R.id.num_0, 0);
        setClickNumber(R.id.num_1, 1);
        setClickNumber(R.id.num_2, 2);
        setClickNumber(R.id.num_3, 3);
        setClickNumber(R.id.num_4, 4);
        setClickNumber(R.id.num_5, 5);
        setClickNumber(R.id.num_6, 6);
        setClickNumber(R.id.num_7, 7);
        setClickNumber(R.id.num_8, 8);
        setClickNumber(R.id.num_9, 9);

        view.findViewById(R.id.img_backspace).setOnClickListener(v -> backspace());
        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.txt_cancel).setOnClickListener(v -> dismiss());
        ctEmail.setOnClickListener(v -> {
        });
        view.findViewById(R.id.txt_send_email).setOnClickListener(v -> checkEmailAndFinishSetup());
    }

    public void setClickNumber(int idView, int number) {
        view.findViewById(idView).setOnClickListener(v ->
                edtPass.setText(edtPass.getText().toString().trim() + number));
    }

    private void backspace() {
        String number = edtPass.getText().toString().trim();
        if (number.length() == 0)
            return;
        edtPass.setText(number.substring(0, number.length() - 1));
    }

    private void showReEnterPassword() {
        txtTitle.setText(R.string.reenter_password);
        passcode = edtPass.getText().toString().trim();
        edtPass.setText("");
    }

    private void checkPassword() {
        if (passcode.equals(edtPass.getText().toString().trim())) {
            ctEmail.setVisibility(View.VISIBLE);
            edtEmail.setText(SharePreferentUtils.getEmail());
        } else {
            edtPass.setText("");
            view.findViewById(R.id.linearLayout).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
        }
    }

    private void checkEmailAndFinishSetup() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Utils.isEmailValid(email)) {
            edtEmail.setError("");
        } else {
            SharePreferentUtils.setEmail(email);
            SharePreferentUtils.setNewPasscode(passcode);
            showDialogSuccess();
        }
    }

    private void showDialogSuccess() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(R.string.pass_code);
        sweetAlertDialog.setContentText(getString(R.string.passcode_successfully));
        sweetAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();

            }
        });
        sweetAlertDialog.show();
    }

    public interface OnPasscodeSetup {
        void onSuccess();
        void onFail();
    }
}