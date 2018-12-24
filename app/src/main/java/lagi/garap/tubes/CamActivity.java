package lagi.garap.tubes;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

public class CamActivity extends AppCompatActivity {

    //inisialisasi kode permintaan untuk capture kamera dan video
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    //inisialisasi kunci path dari image yang akan disimpan
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    //inisialisasi variabel untuk tipe media foto dan video
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    //inisialisasi ukuran sample bitmap (foto)
    public static final int BITMAP_SAMPLE_SIZE = 8;

    //inisialisasi variabel untuk direktori galeri tempat foto dan video disimpan di folder Hello Camera
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    //inisialisasi ekstensi foto dan video yang dihasilkan
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    //inisialisasi path dimana foto disimpan
    private static String imageStoragePath;

    //inisialisasi variabel textview, image view, video view, dan button-button
    private TextView txtDescription;
    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture, btnRecordVideo;
    private Button fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        //inisialisasi widget toolbar pada layout activity_camera
        Toolbar toolbar = findViewById(R.id.toolbar);
        //set toolbar untuk bertindak sebagai actionbar dari cameraactivity
        setSupportActionBar(toolbar);

        //memeriksa kesediaan dari kamera pada device yang digunakan
        if (!CamUtility.isDeviceSupportCamera(getApplicationContext())) {
            //membuat toast untuk menampilkan jika tidak terdapat kamera pada device
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            //method untuk menutup aplikasi jika tidak terdapat kamera pada device
            finish();
        }

        //inisialisasi deskripsi text, image view. videoview, button" pada layout activity_camera
        txtDescription = findViewById(R.id.txt_desc);
        imgPreview = findViewById(R.id.imgPreview);
        videoPreview = findViewById(R.id.videoPreview);
        btnCapturePicture = findViewById(R.id.btnCapturePicture);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);
        fragment = findViewById(R.id.fragment);

        //memberi aksi saat button fragment ditekan
        fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //memulai intent activity dari camera ke main
                startActivity(new Intent(CamActivity.this, MainActivity.class));
            }
        });

        //memberi aksi saat btnCapturePicture ditekan
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //memeriksa apakah sudah mendapat permission dari user untuk mengakses kamera
                if (CamUtility.checkPermissions(getApplicationContext())) {
                    //method untuk menangkap gambar
                    captureImage();
                } else {
                    //memanggil method untuk meminta permission kamera jika belum dapat ijin
                    requestCameraPermission(MEDIA_TYPE_IMAGE);
                }
            }
        });

        //memberikan aksi saat btnRecordVideo di tekan
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //memeriksa apakah sudah mendapat permission dari user untuk mengakses kamera
                if (CamUtility.checkPermissions(getApplicationContext())) {
                    //method untuk mulai merekam
                    captureVideo();
                } else {
                    //memanggil method untuk meminta permission kamera jika belum dapat ijin
                    requestCameraPermission(MEDIA_TYPE_VIDEO);
                }
            }
        });


        //memulihkan jalur gambar penyimpanan dari keadaan instance yang disimpan
        restoreFromBundle(savedInstanceState);
    }

    //menyimpan kembali path gambar dari keadaan instance yang disimpan
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //memeriksa apakah instance state sudah memiliki kunci path penyimpanan gambar
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                //jika ada, maka path tempat gambar disimpan akan diambil dalam tipe data string
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                //memeriksa apakah path gambar tersebut ada
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    //jika ada, periksa path penyimpanan gambar, apakah sama dengan ekstensi gambar
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                        //jika cocok dengan ekstensi gambar, panggil method preview gambar
                        previewCapturedImage();
                    } else
                        //jika tidak cocok, memeriksa apakah ekstensi tsb sama dengan ekstensi video
                        if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + VIDEO_EXTENSION)) {
                            //jika iya, panggil preview video
                            previewVideo();
                        }
                }
            }
        }
    }


    //Permintaan ijin untuk menggunakan library dexter
    private void requestCameraPermission(final int type) {
        //method dari library dexter untuk meminta ijin akses kamera, penyimpan eksternal, merekam audio
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {

                    //method memeriksa ijin
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        //memeriksa apakah semua perijinan sudah diijinkan
                        if (report.areAllPermissionsGranted()) {
                            //jika iya, memeriksa tipe apakah sama dengan ekstensi gambar
                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture foto
                                captureImage();
                            } else {
                                //jika ada ekstensi selain gambar, panggil method rekam video
                                captureVideo();
                            }

                        } else
                            //jika ada yang tidak diijinkan, periksa apa setiap perijinan ditolak permanen
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                //jika ada yang ditolak permanen, panggil method untuk menunjukkan peringatan perijinan
                                showPermissionsAlert();
                            }
                    }

                    //method untuk menunjukkan perijinan yang rasional
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        //panggil method token untuk melanjutkan permintaan ijin akses
                        token.continuePermissionRequest();
                    }
                })
                //panggil method untuk memeriksa ijin
                .check();
    }


    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    //method untuk menjalankan camera (foto)
    private void captureImage() {
        //membuat intent baru untuk capture image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //membuat file media dari aksi kamera
        File file = CamUtility.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        //memeriksa apakah file berhasil dibuat
        if (file != null) {
            //jika berhasil, maka panggil method untuk dapatkan path
            imageStoragePath = file.getAbsolutePath();
        }
        //membuat fileUri dari camera utils
        Uri fileUri = CamUtility.getOutputMediaFileUri(getApplicationContext(), file);
        //memanggil method intent untuk meletakkan output tambahan ke fileUri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        //memulai intent untuk menangkap gambar berdasar kode permintaan menangkap gambar
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Saving stored image path to saved instance state
     */
    //Method untuk menympan hasil gambar
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // menyimpan url file dalam bundel, karena saat orientasi layar berubah bisa menjadi null
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    //mengembalikann gambar dari keadaan instance
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //menyimpan url file yang didapatkan ke variabel
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Launching camera app to record video
     */
    //method menjalankan record video
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CamUtility.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CamUtility.getOutputMediaFileUri(getApplicationContext(), file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    //untuk mengeluarkan preview hasil dari record dan capture gambar
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // jika hasil adalah gambar
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refresh galery
                CamUtility.refreshGallery(getApplicationContext(), imageStoragePath);

                //berhasil mengambil gambar
                //mengeluarkan gambar
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                //cancel pengambilan gambar
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // gagal mengambil gambar
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
            //jika hasil adalah video
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refresh galeri
                CamUtility.refreshGallery(getApplicationContext(), imageStoragePath);

                // record video berhasil
                // preview hasil record
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // pengguna cancer revord
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // gagal merecord video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Display image from gallery
     */
    //menampilkan gambar dari galeri
    private void previewCapturedImage() {
        try {
            //menyembunyikan preview video dan deskripsi text
            txtDescription.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);
            //menampilkan visibilitas preview gambar
            imgPreview.setVisibility(View.VISIBLE);
            //menyimpan optimalisasi gambar dari CamUtility ke variabel bitmap
            Bitmap bitmap = CamUtility.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            //memunculkan gambar
            imgPreview.setImageBitmap(bitmap);

        }
        //mencetak jejak stack instance ke system error
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displaying video in VideoView
     */
    //menampilkan preview video
    private void previewVideo() {
        try {
            // sembunyikan image preview
            txtDescription.setVisibility(View.GONE);
            imgPreview.setVisibility(View.GONE);
            // mengubah visibilitas preview video agar terlihat
            videoPreview.setVisibility(View.VISIBLE);
            //memasang path video
            videoPreview.setVideoPath(imageStoragePath);
            // start mainkan
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    //dialog peringatan aplikasi setting untuk enable ijin
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CamUtility.openSettings(CamActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
