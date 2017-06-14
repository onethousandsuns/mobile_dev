package com.dev.bochkarev.lab4_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageView _imageView;
    private String _pathToPhoto;
    private static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _imageView = (ImageView) findViewById(R.id.imageView);

        ImageButton takePhotoButton = (ImageButton) findViewById(R.id.imageButton);
        takePhotoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new RunDispatchTakePictureIntent().execute();
                    }
                });
    }

    private class RunDispatchTakePictureIntent extends AsyncTask<Void, Void, Intent> {
        @Override
        protected void onPreExecute(){
            Toast.makeText(MainActivity.this,
                    R.string.camera_running,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Intent doInBackground(Void... params) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                return takePictureIntent;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            // code below taken from https://developer.android.com/training/camera/photobasics.html
            if (intent != null) {
                File photoFile = null;
                try {
                    photoFile = new ImageLoader().execute((Void) null).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.camera_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            try{
                new GalleryPictureAdder().execute();
                new PictureViewer().execute();
            } catch (Exception e){
                Toast.makeText(MainActivity.this,
                        R.string.camera_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImageLoader extends AsyncTask<Void, Void, File>{

        @Override
        protected File doInBackground(Void... params) {
            try {
                File storageDir = Environment.getExternalStorageDirectory();
                File image = File.createTempFile("DCIM", ".jpg", storageDir);
                _pathToPhoto = image.getAbsolutePath();
                return image;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class GalleryPictureAdder extends AsyncTask<Void, Void, Void> {
        Intent mediaScanIntent;

        @Override
        protected Void doInBackground(Void... params) {
            // code below taken from https://developer.android.com/training/camera/photobasics.html
            mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(_pathToPhoto);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            return null;
        }

        @Override
        protected void onPostExecute(Void param){
            getApplicationContext().sendBroadcast(mediaScanIntent);
        }
    }

    private class PictureViewer extends AsyncTask<Void, Void, Bitmap> {
        int targetW = _imageView.getWidth();
        int targetH = _imageView.getHeight();

        @Override
        protected void onPreExecute(){
            // Get the dimensions of the View
            targetW = _imageView.getWidth();
            targetH = _imageView.getHeight();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(_pathToPhoto, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            return BitmapFactory.decodeFile(_pathToPhoto, bmOptions);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            _imageView.setImageBitmap(bitmap);
            Toast.makeText(MainActivity.this,
                    getString(R.string.saved_to) + _pathToPhoto,
                    Toast.LENGTH_SHORT).show();
        }
    }
}