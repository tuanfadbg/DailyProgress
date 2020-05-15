package com.tuanfadbg.trackprogress.ui.settings;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.edit_name.EditNameDialog;
import com.tuanfadbg.trackprogress.ui.image_note.ImageNoteDialog;
import com.tuanfadbg.trackprogress.ui.passcode.CreatePasscodeDialog;
import com.tuanfadbg.trackprogress.ui.tag_manager.TagManagerDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;
import com.tuanfadbg.trackprogress.utils.Utils;
import com.tuanfadbg.trackprogress.utils.takephoto.TakePhotoCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsDialog extends DialogFragment {

    private static final String TAG = SettingsDialog.class.getSimpleName();
    private TextView txtName;
    private ConstraintLayout ctPasscode, ctPasscodeSetting, ctExport;
    private Switch switchPassword;
    private TextView txtLocation;
    private String FOLDER = String.format("Storage/%s/%s", Utils.FOLDER, Utils.EXPORT_FOLDER);

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
        txtLocation = view.findViewById(R.id.txt_location);

        String name = SharePreferentUtils.getName(false);
        if (TextUtils.isEmpty(name))
            name = getString(R.string.enter_your_name);
        txtName.setText(name);

        setLayout();
        setListener(view);
        getPrice();
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

        txtLocation.setText(FOLDER);
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
        ctExport.setOnClickListener(v -> exportAll());
    }

    private BillingClient billingClient;
    SkuDetails productSku;

    private void getPrice() {
        String mySku = "progress_1_usd";
//        String mySku = "android.test.purchased";
        billingClient = BillingClient.newBuilder(getContext()).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    grantEtitlement();
                }
            }
        }).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>();
                    skuList.add(mySku);

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();
                                            productSku = skuDetails;
                                            if (mySku.equals(sku)) {
                                                ((TextView) view.findViewById(R.id.txt_money)).setText(price);
                                            }
                                        }
                                    }
                                }
                            });
                }

                billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, (billingResult1, purchasesList) -> {
                    if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && purchasesList != null) {
                        for (PurchaseHistoryRecord purchase : purchasesList) {
                            if (purchase.getSku().equals(mySku)) {
                                grantEtitlement();
                            }
                        }
                    }
                });
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
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
        sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
        sweetAlertDialog.show();
    }

    private void showAlertImportSuccess() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(R.string.import_image);
        sweetAlertDialog.setContentText(getString(R.string.import_success));
        sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
        sweetAlertDialog.show();
    }

    private void showPassCodeSettings(boolean setEnablePassCodeAfterDone) {
        if (SharePreferentUtils.isPremium()) {
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
    }

    private void exportAll() {
        if (SharePreferentUtils.isPremium()) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog.setTitle(getString(R.string.ask_export_image));
            sweetAlertDialog.setContentText(FOLDER);
            sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
            sweetAlertDialog.setCancelText(getString(R.string.cancel));
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    sweetAlertDialog.setTitle("");
                    sweetAlertDialog.setContentText("");
                    sweetAlertDialog.hideConfirmButton();
                    sweetAlertDialog.showCancelButton(false);
                    ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
                    itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, null, new ItemSelectAsyncTask.OnItemSelectedListener() {
                        @Override
                        public void onSelected(List<Item> datas) {
                            copyAllImageToNewFolder(datas);
                            sweetAlertDialog.dismiss();
                            SweetAlertDialog dialogSuccess = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                            dialogSuccess.setTitle(getString(R.string.export_success));
                            dialogSuccess.setConfirmText(getString(R.string.str_ok));
                            dialogSuccess.show();
                        }
                    }));
                }
            });
            sweetAlertDialog.show();

        }
    }

    private void copyAllImageToNewFolder(List<Item> datas) {
        if (getActivity() != null) {
            FileManager fileManager = new FileManager(getActivity());
            fileManager.createExportFolder();
            for (Item item : datas) {
                File sourceFile = new File(item.file);
                File desFile = fileManager.getExportFileFromSourceFile(sourceFile);
                try {
                    fileManager.copyFile(sourceFile, desFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

        }
    };

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            grantEtitlement();
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private void upgradePremium() {
        if (productSku != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(productSku)
                    .build();

            BillingResult billingResult = billingClient.launchBillingFlow(getActivity(), flowParams);
//            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.)
        }


    }

    private void grantEtitlement() {
        SharePreferentUtils.setPremium(true);
        setLayout();
    }
}