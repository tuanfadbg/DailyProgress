package com.tuanfadbg.trackprogress.ui.settings;

import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.tuanfadbg.trackprogress.ui.passcode.CreatePasscodeDialog;
import com.tuanfadbg.trackprogress.ui.restore.RestoreDialog;
import com.tuanfadbg.trackprogress.ui.tag_manager.TagManagerDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsDialog extends DialogFragment {

    private static final String TAG = SettingsDialog.class.getSimpleName();
    private TextView txtName;
    private ConstraintLayout ctPasscode, ctPasscodeSetting, ctExport;
    private Switch switchPassword;
    private TextView txtLocation, txtLanguage;
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
        view = (ViewGroup) inflater.inflate(R.layout.dialog_settings, container, false);
        return view;
    }


    ViewGroup view;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        defineView(view);
        setLayout(null);
        getPrice();
    }

    private void defineView(View view) {
        txtName = view.findViewById(R.id.txt_name);
        ctPasscode = view.findViewById(R.id.ct_password);
        ctPasscodeSetting = view.findViewById(R.id.ct_password_settings);
        ctExport = view.findViewById(R.id.ct_export);
        switchPassword = view.findViewById(R.id.switch_passcode);
        txtLanguage = view.findViewById(R.id.txt_language);
        txtLocation = view.findViewById(R.id.txt_location);
    }


    private void setLayout(Locale locale) {
        try {
            String name = SharePreferentUtils.getName(false);
            if (TextUtils.isEmpty(name))
                name = getString(R.string.enter_your_name);
            txtName.setText(name);

            boolean isPremium = SharePreferentUtils.isPremium();

            switchPassword.setChecked(isPremium && SharePreferentUtils.isPasscodeEnable());
            ctPasscode.setSelected(!isPremium);
            ctPasscodeSetting.setSelected(!isPremium);
            if (locale == null) {
                locale = getResources().getConfiguration().locale;
            }
//            Log.e(TAG, "setLayout: " + locale.getLanguage() );
            txtLanguage.setText(Utils.uppercaseFirstLetter(locale.getDisplayLanguage(locale)));

            ctExport.setSelected(!isPremium);
            switchPassword.setClickable(isPremium);

            if (isPremium) {
                view.findViewById(R.id.ct_premium).setVisibility(View.GONE);
                view.findViewById(R.id.txt_upgrade).setVisibility(View.GONE);
            }

            txtLocation.setText(FOLDER);

            setListener(view);
        } catch (Exception ignored) {

        }

    }

    private void setListener(View view) {
        view.findViewById(R.id.ct_import).setOnClickListener(v -> importPhoto());

        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        txtName.setOnClickListener(v -> {
            EditNameDialog editNameDialog = new EditNameDialog(newName -> {
                txtName.setText(newName);
//                ((MainActivity) getActivity()).updateName(newName);
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
        view.findViewById(R.id.ct_language).setOnClickListener(v -> showDialogSelectLanguages());
        view.findViewById(R.id.ct_tag_manager).setOnClickListener(v -> showTagManager());
        view.findViewById(R.id.txt_upgrade).setOnClickListener(v -> upgradePremium());
        ctExport.setOnClickListener(v -> exportAll());
        view.findViewById(R.id.ct_restore).setOnClickListener(v -> restore());
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
            ((MainActivity) getActivity()).importImage();
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

    AlertDialog alertDialogLanguages;
    CharSequence[] valuesLanguages;

    private void showDialogSelectLanguages() {
        ArrayList<String> listLanguageCodeDefault = new ArrayList<>();
        listLanguageCodeDefault.add("en");
        listLanguageCodeDefault.add("pt");
        listLanguageCodeDefault.add("ar");
        listLanguageCodeDefault.add("ru");
        listLanguageCodeDefault.add("ja");
        listLanguageCodeDefault.add("vi");

        Locale currentLocate = getResources().getConfiguration().locale;

        boolean isContainLanguage = false;
        int checkedItem = 0;
        for (int i = 0; i < listLanguageCodeDefault.size(); i++) {
            if (currentLocate.getLanguage().contains(listLanguageCodeDefault.get(i))) {
                isContainLanguage = true;
                checkedItem = i;
                break;
            }
        }
        if (!isContainLanguage) {
            listLanguageCodeDefault.add(0, currentLocate.getLanguage());
        }
        for (int i = 0; i < listLanguageCodeDefault.size(); i++) {
            if (!isSupportLanguage(listLanguageCodeDefault.get(i))) {
                listLanguageCodeDefault.remove(i);
                i--;
            }
        }

        valuesLanguages = new CharSequence[listLanguageCodeDefault.size()];
        for (int i = 0; i < listLanguageCodeDefault.size(); i++) {
            Locale loc = new Locale(listLanguageCodeDefault.get(i));
            String name = loc.getDisplayName(loc);
            name = Utils.uppercaseFirstLetter(name);
            valuesLanguages[i] = name;
        }

        final int[] userSelectedItem = {checkedItem};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.language);
        builder.setSingleChoiceItems(valuesLanguages, checkedItem, (dialog, item) -> userSelectedItem[0] = item);
        builder.setPositiveButton(R.string.str_ok, (dialog, which) -> {
            if (getActivity() != null)
                setLocaleAndRecreateView(listLanguageCodeDefault.get(userSelectedItem[0]));
        });

        builder.setNegativeButton(R.string.str_cancel, (dialog, which) -> {

        });

        alertDialogLanguages = builder.create();
        alertDialogLanguages.show();
    }

    private boolean isSupportLanguage(String languageCode) {
        List<String> supportedLanguages = Arrays.asList(Resources.getSystem().getAssets().getLocales());
        return supportedLanguages.contains(languageCode);
    }

    public void setLocaleAndRecreateView(String languageCode) {
        Locale localeDefault = getResources().getConfiguration().locale;
        if (localeDefault.getLanguage().equals(languageCode))
            return;

        if (getActivity() != null) {
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Resources resources = getActivity().getResources();
            Configuration config = resources.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
            }
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            recreateView(locale);
        }
    }

    private void recreateView(Locale locale) {
        view.removeAllViews();
        getLayoutInflater().inflate(R.layout.dialog_settings, view);
        defineView(view);
        setLayout(locale);
    }

    private void exportAll() {
        if (SharePreferentUtils.isPremium()) {
            ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
            itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, null, new ItemSelectAsyncTask.OnItemSelectedListener() {
                @Override
                public void onSelected(List<Item> datas) {
                    if (datas.size() > 0) {
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
                                copyAllImageToNewFolder(datas);
                                sweetAlertDialog.dismiss();
                                SweetAlertDialog dialogSuccess = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                                dialogSuccess.setTitle(getString(R.string.export_success));
                                dialogSuccess.setConfirmText(getString(R.string.str_ok));
                                dialogSuccess.show();
                            }
                        });
                        sweetAlertDialog.show();
                    } else {
                        showDialogNoImage();
                    }
                }
            }));
        }
    }

    private void showDialogNoImage() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(R.string.error_no_image);
        sweetAlertDialog.show();
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
        setLayout(null);
    }

    private void restore() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FileManager fileManager = new FileManager(getActivity());
                File dataFolder = fileManager.getPrivateFolder();
                File[] allData = dataFolder.listFiles();
                ArrayList<String> allDataInFolder = new ArrayList<>();
                for (int i = 0; i < allData.length; i++) {
                    allDataInFolder.add(allData[i].getAbsolutePath());
                }

                ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
                itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, null, new ItemSelectAsyncTask.OnItemSelectedListener() {
                    @Override
                    public void onSelected(List<Item> datas) {
                        ArrayList<String> dataInDb = new ArrayList<>();
                        for (int i = 0; i < datas.size(); i++) {
                            String file = datas.get(i).file;
                            dataInDb.add(file);
                        }
                        for (int i = 0; i < allDataInFolder.size(); i++) {
                            if (dataInDb.contains(allDataInFolder.get(i))) {
                                dataInDb.remove(allDataInFolder.get(i));
                                allDataInFolder.remove(i);
                                i--;
                            }
                        }
                        if (allDataInFolder.size() > 0) {
                            showDialogRestore(allDataInFolder);
                        } else
                            showDialogNoDataToRestore();
                    }
                }));

            }
        });
    }

    private void showDialogRestore(ArrayList<String> dataToRestore) {
        RestoreDialog restoreDialog = new RestoreDialog(dataToRestore, new RestoreDialog.OnUpdateItemListener() {
            @Override
            public void onUpdated() {

            }
        });
        restoreDialog.show(getFragmentManager(), RestoreDialog.class.getSimpleName());
    }

    private void showDialogNoDataToRestore() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle(getString(R.string.no_data_to_restore));
        sweetAlertDialog.show();
    }
}