package com.tuanfadbg.progress.ui.settings;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.android.dx.command.Main;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.ui.MainActivity;
import com.tuanfadbg.progress.ui.edit_name.EditNameDialog;
import com.tuanfadbg.progress.ui.image_note.ImageNoteDialog;
import com.tuanfadbg.progress.ui.passcode.CreatePasscodeDialog;
import com.tuanfadbg.progress.ui.tag_manager.TagManagerAdapter;
import com.tuanfadbg.progress.ui.tag_manager.TagManagerDialog;
import com.tuanfadbg.progress.utils.Constants;
import com.tuanfadbg.progress.utils.SharePreferentUtils;
import com.tuanfadbg.progress.utils.Utils;
import com.tuanfadbg.progress.utils.takephoto.TakePhotoCallback;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsDialog extends DialogFragment {

    private static final String TAG = SettingsDialog.class.getSimpleName();
    private TextView txtName;
    private ConstraintLayout ctPasscode, ctPasscodeSetting, ctExport;
    private Switch switchPassword;

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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog_Animation_Left);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_settings, container, false);
        return view;
    }

    View view;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        txtName = view.findViewById(R.id.txt_name);
        ctPasscode = view.findViewById(R.id.ct_password);
        ctPasscodeSetting = view.findViewById(R.id.ct_password_settings);
        ctExport = view.findViewById(R.id.ct_export);
        switchPassword = view.findViewById(R.id.switch_passcode);

        String name = SharePreferentUtils.getName(false);
        if (TextUtils.isEmpty(name))
            name = getString(R.string.enter_your_name);
        txtName.setText(name);
        setLayout();
        setListener(view);
    }

    private void setLayout() {
        boolean isPremium = SharePreferentUtils.isPremium();

        switchPassword.setChecked(isPremium && SharePreferentUtils.isPasscodeEnable());
        ctPasscode.setSelected(!isPremium);
        ctPasscodeSetting.setSelected(!isPremium);
        ctExport.setSelected(!isPremium);
        switchPassword.setClickable(isPremium);

        if (isPremium) {
            view.findViewById(R.id.ct_premium).setVisibility(View.GONE);
            view.findViewById(R.id.txt_upgrade).setVisibility(View.GONE);
        }
    }

    private void setListener(View view) {
        view.findViewById(R.id.ct_import).setOnClickListener(v -> importPhoto());

        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        txtName.setOnClickListener(v -> {
            EditNameDialog editNameDialog = new EditNameDialog(newName -> {
                txtName.setText(newName);
                ((MainActivity) getActivity()).updateName(newName);
            });
            editNameDialog.show(getFragmentManager(), EditNameDialog.class.getSimpleName());
        });
        view.findViewById(R.id.textView17).setOnClickListener(v -> txtName.performClick());
        switchPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (SharePreferentUtils.hasPasscode()) {
                    SharePreferentUtils.setPasscodeEnable(true);
                } else {
                    showPassCodeSettings(true);
                }
            } else {
                SharePreferentUtils.setPasscodeEnable(false);
            }
        });
        ctPasscodeSetting.setOnClickListener(v -> showPassCodeSettings(false));
        view.findViewById(R.id.ct_tag_manager).setOnClickListener(v -> showTagManager());
        view.findViewById(R.id.txt_upgrade).setOnClickListener(v -> upgradePremium());
    }

    private void showTagManager() {
        TagManagerDialog tagManagerDialog = new TagManagerDialog();
        tagManagerDialog.show(getChildFragmentManager(), TagManagerDialog.class.getSimpleName());
    }

    private void importPhoto() {
        if (getActivity() != null)
            ((MainActivity) getActivity())
                    .getTakePhotoUtils()
                    .selectMultiple()
                    .getImageFromGallery()
                    .setListener(new TakePhotoCallback() {
                        @Override
                        public void onMultipleSuccess(List<String> imagesEncodedList, ArrayList<Uri> mArrayUri, List<Long> lastModifieds) {
                            if (getActivity() == null)
                                return;
                            if (imagesEncodedList != null)
                                for (String imgPath : imagesEncodedList) {
                                    if (!Utils.isImage(imgPath)) {
                                        showAlertNotImage();
                                        return;
                                    }
                                }
                            ImageNoteDialog imageNoteDialog =
                                    new ImageNoteDialog(mArrayUri,
                                            ((MainActivity) getActivity()).getCurrentTagSelected(),
                                            (hasNewTag, tagId) -> {
                                                ((MainActivity) getActivity()).updateNewItem(hasNewTag, tagId);
                                                showAlertImportSuccess();
                                            });

                            imageNoteDialog.setLastModifieds(lastModifieds);
                            imageNoteDialog.show(getActivity().getSupportFragmentManager(), ImageNoteDialog.class.getSimpleName());
                        }

                        @Override
                        public void onSuccess(Bitmap bitmap, int width, int height, Uri sourceUri, long lastModified) {
                            if (getActivity() == null)
                                return;

                            ImageNoteDialog imageNoteDialog =
                                    new ImageNoteDialog(bitmap,
                                            ((MainActivity) getActivity()).getCurrentTagSelected(),
                                            (hasNewTag, tagId) -> {
                                                ((MainActivity) getActivity()).updateNewItem(hasNewTag, tagId);
                                                showAlertImportSuccess();
                                            });
                            imageNoteDialog.setLastModified(lastModified);
                            imageNoteDialog.show(getActivity().getSupportFragmentManager(), ImageNoteDialog.class.getSimpleName());
                        }

                        @Override
                        public void onFail() {

                        }
                    });
    }

    private void showAlertNotImage() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(R.string.import_image);
        sweetAlertDialog.setContentText(getString(R.string.import_not_image));
        sweetAlertDialog.setConfirmText(getString(R.string.dialog_ok));
        sweetAlertDialog.show();
    }

    private void showAlertImportSuccess() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(R.string.import_image);
        sweetAlertDialog.setContentText(getString(R.string.import_success));
        sweetAlertDialog.setConfirmText(getString(R.string.dialog_ok));
        sweetAlertDialog.show();
    }

    private void showPassCodeSettings(boolean setEnablePassCodeAfterDone) {
        CreatePasscodeDialog createPasscodeDialog = new CreatePasscodeDialog(new CreatePasscodeDialog.OnPasscodeSetup() {
            @Override
            public void onSuccess() {
                if (setEnablePassCodeAfterDone) {
                    SharePreferentUtils.setPasscodeEnable(true);
                    switchPassword.setChecked(true);
                }
            }

            @Override
            public void onFail() {
                SharePreferentUtils.setPasscodeEnable(false);
                switchPassword.setChecked(false);
            }
        });
        createPasscodeDialog.show(getFragmentManager(), CreatePasscodeDialog.class.getSimpleName());
    }

    private void upgradePremium() {
        SharePreferentUtils.setPremium(true);
        setLayout();
    }
}