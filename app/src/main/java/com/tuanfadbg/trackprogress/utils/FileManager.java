package com.tuanfadbg.trackprogress.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.dex.util.FileUtils;
import com.tuanfadbg.trackprogress.beforeafterimage.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FileManager {
    private Activity activity;

    public FileManager(Activity context) {
        this.activity = context;
    }

    public String storeImage(Bitmap image) {
        createFolder();
        File pictureFile = getOutputMediaFile();
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            sendBroadcastScanFile(pictureFile);

            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
//            Log.e("storeImage", "FileNotFoundException: " );
        } catch (IOException e) {
//            Log.e("storeImage", "IOException: ");
        }
        return "";
    }

    public File getNewFile() {
        createFolder();
        return getOutputMediaFile();
    }


    public String storeImageWithoutBroadcast(Bitmap image) {
        createFolder();
        File pictureFile = getOutputMediaFile();
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return "";
    }


    public File getOutputMediaFile() {
        createFolder();
        File mediaStorageDir = new File(Utils.getFolderPath());

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmms").format(new Date());
        File mediaFile;
        String mImageName = "Track_Progress_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void createFolder() {
        File f = new File(Utils.getFolderPath());
        if (!f.exists())
            if (!f.mkdir()) {
                Toast.makeText(activity, activity.getString(R.string.folder_cant_be_created), Toast.LENGTH_SHORT).show();
            }
    }

    public void createExportFolder() {
        createFolder();
        File f = new File(Utils.getExportFolderPath());
        if (!f.exists())
            if (!f.mkdir()) {
                Toast.makeText(activity, activity.getString(R.string.folder_cant_be_created), Toast.LENGTH_SHORT).show();
            }
    }

    public File getExportFileFromSourceFile(File sourceFile) {
        File mediaStorageDir = new File(Utils.getExportFolderPath());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + sourceFile.getName());
        return mediaFile;
    }

    public boolean checkAndGraintPermission() {
        if (Utils.isGraintedPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return false;
        }
        return true;
    }

    public void sendBroadcastScanFile(File pictureFile) {
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(pictureFile.getAbsolutePath()))));
        MediaScannerConnection.scanFile(
                activity, new String[]{pictureFile.getAbsolutePath()}, null,
                (path, uri) -> {
                });
    }

    public File storeImageOnPrivateStorage(Bitmap bitmapImage) {
        File mypath = getNewFileInPrivateStorage();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mypath;
    }

    public File getPrivateFolder() {
        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir("data", Context.MODE_PRIVATE);
        return directory;
    }

    public File getNewFileInPrivateStorage() {
        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir("data", Context.MODE_PRIVATE);
        return new File(directory, "image_" + new Date().getTime() + ".jpg");
    }

    public String saveFileFromInputStreamUri(Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = activity.getContentResolver().openInputStream(uri);
                File photoFile = createTemporalFileFrom(inputStream);

                filePath = photoFile.getPath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filePath;
    }

    private File createTemporalFileFrom(InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = getNewFileInPrivateStorage();
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static boolean isWriteStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static void removeOldImage() {
        try {
            List<String> dataOldImage = SharePreferentUtils.getImagePathHaveToRemoveArray();
            if (dataOldImage != null && dataOldImage.size() > 0)
                for (String item : dataOldImage) {
                    File file = new File(item);
                    if (file.exists())
                        file.delete();
                }
        } catch (Exception ignored) {

        }
    }

    public void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }
}
