package com.tuanfadbg.progress.ui.passcode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.ui.MainActivity;
import com.tuanfadbg.progress.ui.edit_name.EnterNameDialog;
import com.tuanfadbg.progress.ui.passcode.forgot_password.ForgotPasswordDialog;
import com.tuanfadbg.progress.utils.SharePreferentUtils;
import com.tuanfadbg.progress.utils.Utils;

import java.util.concurrent.Executor;

public class CheckPasscodeActivity extends AppCompatActivity {

    ConstraintLayout ctPlash, ctPasscode;
    View viewLeft, viewRight;

    private TextView txtTitle;
    private EditText edtPass;
    private View v1, v2, v3, v4;
    private String passcode;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        ctPlash = findViewById(R.id.ct_splash);
        ctPasscode = findViewById(R.id.ct_passcode);
        viewLeft = findViewById(R.id.view_left);
        viewRight = findViewById(R.id.view_right);

        ctPlash.setOnClickListener(v -> {
        });

        int[] dimension = Utils.getScreenWidthAndHeight(this);

        float initWidth = Utils.convertDpToPixel(30, this);
        float scaleX = dimension[0] / initWidth * 2;
        float scaleY = dimension[1] / initWidth * 2;
        viewRight.animate()
                .scaleX(scaleX)
                .setDuration(1000)
                .setStartDelay(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        viewRight.animate()
                .scaleY(scaleY)
                .setDuration(800)
                .setStartDelay(1500)
                .setInterpolator(new AccelerateInterpolator())
                .start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharePreferentUtils.isFirstOpen()) {
                    SharePreferentUtils.setFirstOpen();

                    EnterNameDialog enterNameDialog = new EnterNameDialog(() -> goToMain());
                    enterNameDialog.show(getSupportFragmentManager(), EnterNameDialog.class.getSimpleName());

                    ctPlash.setVisibility(View.GONE);
                    return;
                }
                if (SharePreferentUtils.isPasscodeEnable()) {
                    ctPlash.setVisibility(View.GONE);
                    ctPasscode.setVisibility(View.VISIBLE);
                    showCheckPasscode();
                } else {
                    goToMain();
                }
            }
        }, 2300);
    }

    private void showCheckPasscode() {

        view = findViewById(R.id.root);
        txtTitle = view.findViewById(R.id.textView11);
        edtPass = view.findViewById(R.id.edt_pass);

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
                    checkPassword();
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
        view.findViewById(R.id.txt_forgot_password).setOnClickListener(v -> forgotPassword());
        view.findViewById(R.id.img_check_fingerprint).setOnClickListener(v -> fingerprint());
        fingerprint();
    }

    private Executor executor;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private void fingerprint() {
//        ((ImageView) view.findViewById(R.id.img_fingerprint)).setColorFilter(ContextCompat.getColor(this, R.color.blue_dark), android.graphics.PorterDuff.Mode.MULTIPLY);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                goToMain();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_subtitle))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
//        Button biometricLoginButton = findViewById(R.id.biometric_login);
//        biometricLoginButton.setOnClickListener(view -> {
        biometricPrompt.authenticate(promptInfo);
//        });
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

    private void forgotPassword() {
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
        forgotPasswordDialog.show(getSupportFragmentManager(), ForgotPasswordDialog.class.getSimpleName());
    }


    private void checkPassword() {
        if (SharePreferentUtils.checkPasscode(edtPass.getText().toString().trim())) {
            goToMain();
        } else {
            edtPass.setText("");
            view.findViewById(R.id.linearLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
