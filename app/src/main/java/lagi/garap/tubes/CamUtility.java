package lagi.garap.tubes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CamUtility {

    /**
     * Refreshes gallery on adding new image/video. Gallery won't be refreshed
     * on older devices until device is rebooted
     */
    //method untuk menyegarkan galeri
    public static void refreshGallery(Context context, String filePath) {
        // ScanFile untuk memnuculkan gambar di galeri
        MediaScannerConnection.scanFile(context,
                new String[]{filePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    //memeriksa semua perijinan
    public static boolean checkPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Downsizing the bitmap to avoid OutOfMemory exceptions
     */
    //menurunkan ukuran bitmap untuk menghindari pengecualian jika memori habis
    public static Bitmap optimizeBitmap(int sampleSize, String filePath) {
        // pembuatan bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();

        //perampingan gambar yang lebih besar ke ukuran sample
        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Checks whether device has camera or not. This method not necessary if
     * android:required="true" is used in manifest file
     */
    //mengecek apakah device memiliki kamera atau tidak
    public static boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            //jika device punya kamera
            return true;
        } else {
            // jika tidak ada kamera
            return false;
        }
    }

    /**
     * Open device app settings to allow user to enable permissions
     */
    //method untuk pengaturan device untuk memungkinkan user mengaktifkan ijin
    public static void openSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Uri getOutputMediaFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    /**
     * Creates and returns the image or video file before opening the camera
     */
    //method untuk membuat dan mengembalikan gambar/video sebelum membuka kamera
    public static File getOutputMediaFile(int type) {

        //lokasi eksternal pada sdcard
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                CamActivity.GALLERY_DIRECTORY_NAME);

        //membuat direktori ke internal stirage jika tidak ada sdcard
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(CamActivity.GALLERY_DIRECTORY_NAME, "Oops! Failed create "
                        + CamActivity.GALLERY_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        //menambahkan timestamp pada file media
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == CamActivity.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + "." + CamActivity.IMAGE_EXTENSION);
        } else if (type == CamActivity.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + "." + CamActivity.VIDEO_EXTENSION);
        } else {
            return null;
        }

        return mediaFile;
    }


}
