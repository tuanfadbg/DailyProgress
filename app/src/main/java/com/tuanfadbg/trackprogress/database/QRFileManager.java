package com.tuanfadbg.trackprogress.database;

public class QRFileManager {
//    private Activity activity;
//
//    public QRFileManager(Activity context) {
//        this.activity = context;
//    }
//
//    public String storeImage(Bitmap image) {
//        QRFileManager qrFileManager = new QRFileManager(activity);
//        qrFileManager.createFolder();
//        File pictureFile = qrFileManager.getOutputMediaFile();
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            fos.close();
//
//            sendBroadcastScanFile(pictureFile);
//
//            return pictureFile.getAbsolutePath();
//        } catch (FileNotFoundException e) {
//            Log.e("storeImage", "FileNotFoundException: " );
//        } catch (IOException e) {
//            Log.e("storeImage", "IOException: ");
//        }
//        return "";
//    }

//    private File getOutputMediaFile() {
//        File mediaStorageDir = new File(Utils.getFolderPath());
//
//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmms").format(new Date());
//        File mediaFile;
//        String mImageName = "QRCODE_" + timeStamp + ".jpg";
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
//        return mediaFile;
//    }
//
//    private void createFolder() {
//        File f = new File(Utils.getFolderPath());
//        if (!f.exists())
//            if (!f.mkdir()) {
//                Toast.makeText(activity, activity.getString(R.string.folder_cant_be_created), Toast.LENGTH_SHORT).show();
//            }
//    }
//
//    public boolean checkAndGraintPermission() {
//        if (Utils.isGraintedPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    1);
//            return false;
//        }
//        return true;
//    }
//
//    private void sendBroadcastScanFile(File pictureFile) {
//        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(pictureFile.getAbsolutePath()))));
//        MediaScannerConnection.scanFile(
//                activity, new String[]{pictureFile.getAbsolutePath()}, null,
//                (path, uri) -> {});
//    }
}
