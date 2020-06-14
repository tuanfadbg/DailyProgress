package com.tuanfadbg.trackprogress.ui.passcode.forgot_password;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPasswordDialog extends DialogFragment {

    private TextView txtEmail;
    private ProgressBar progressBar;
    private ImageView imgLock;

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
        sendEmail(email, SharePreferentUtils.createTempPasscode());
    }

    private boolean isButtonSendEmailEnable = true;

    private void sendEmail(String email, String passCode) {
        if (!isButtonSendEmailEnable)
            return;
        isButtonSendEmailEnable = false;
        imgLock.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://tuanfadbg.com/api/sendmail.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
//                        Log.d("Response", response);
                        isButtonSendEmailEnable = true;

                        imgLock.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                        if (response.equals("success")) {
                            showDialogSuccess();
                        } else {
                            showDialogFail();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isButtonSendEmailEnable = true;
                        imgLock.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        showDialogFail();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("passcode", passCode);
                params.put("email", email);

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void showDialogSuccess() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(R.string.send_email_forgot_password_success);
        sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
        sweetAlertDialog.setOnDismissListener(dialog -> dismiss());
        sweetAlertDialog.show();
    }

    private void showDialogFail() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
        sweetAlertDialog.setTitle(R.string.send_email_forgot_password_fail);
        sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
        sweetAlertDialog.setOnDismissListener(dialog -> dismiss());
        sweetAlertDialog.show();
    }
}