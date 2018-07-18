/*
 * Copyright (c) [2018] [Ankit Kr]. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hubdestro.hpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;


/**
 * Created by hubdestro on 13/9/16
 * @email info@hubdestro.com
 * @web www.project.hubdestro.com
 * @author ankit demonstrate
 */
public final class HPicker {

    private static final String TEMP_IMAGE_NAME = "temp.png";
    private static final String TAG = "ImagePickerLog";

    private static boolean isValidFileSize(File file) {
        return file.length() < 1000000;
    }

    private static boolean isValidFileFormat(File file) {
        String path = file.getAbsolutePath();

        return path.endsWith(".pdf")   ||
                path.endsWith(".jpg")  ||
                path.endsWith(".jpeg") ||
                path.endsWith(".png")  ||
                path.endsWith(".docx") ||
                path.endsWith(".doc");
    }

    public static void selectImage(Fragment fragment) {
        //1- by using camera
        //2- mFrom gallery
        //3- mFrom files

        Intent camera = getPickImageCameraIntent(Objects.requireNonNull(fragment.getActivity()));
        Intent gallery = getPickImageGalleryIntent();

        AlertDialog mDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        @SuppressLint("InflateParams")
        View layout = fragment.getActivity().getLayoutInflater().inflate(R.layout.layout_hpicker, null);
        Button btnCamera = layout.findViewById(R.id.btnCamera);
        Button btnGallery = layout.findViewById(R.id.btnGallery);

        builder.setView(layout);
        mDialog = builder.create();
        mDialog.show();

        btnCamera.setOnClickListener( v -> {
            fragment.startActivityForResult(camera, 13060);
            mDialog.dismiss();
        });

        btnGallery.setOnClickListener( v -> {
            fragment.startActivityForResult(gallery, 23060);
            mDialog.dismiss();
        });
    }

    /***
     * start intent to pick photo mFrom camera or gallery
     */
    public static void selectImage(Activity mActivity) {
        //1- by using camera
        //2- mFrom gallery
        //3- mFrom files

        Intent camera = getPickImageCameraIntent(mActivity);
        Intent gallery = getPickImageGalleryIntent();

        AlertDialog mDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        @SuppressLint("InflateParams")
        View layout = mActivity.getLayoutInflater().inflate(R.layout.layout_hpicker, null);
        LinearLayout btnCamera = layout.findViewById(R.id.btnCamera);
        LinearLayout btnGallery = layout.findViewById(R.id.btnGallery);

        builder.setView(layout);
        mDialog = builder.create();
        mDialog.show();

        btnCamera.setOnClickListener( v -> {
            mActivity.startActivityForResult(camera, 13060);
            mDialog.dismiss();
        });

        btnGallery.setOnClickListener( v -> {
            mActivity.startActivityForResult(gallery, 23060);
            mDialog.dismiss();
        });
    }

    /***
     * start intent to pick photo mFrom camera or gallery
     */
    public static void selectFile(final Activity mActivity) {
        Intent files = getPickFilesIntent(mActivity);
        mActivity.startActivityForResult(files, 33060);
    }

    /***
     * start intent to pick photo mFrom camera or gallery
     */
    public static void selectFile(final Fragment fragment) {
        //1- by using camera
        //2- mFrom gallery
        //3- mFrom files

        Activity mActivity = fragment.getActivity();
        Intent files = getPickFilesIntent(Objects.requireNonNull(mActivity));
        fragment.startActivityForResult(files, 33060);
    }

    private static Intent getPickImageCameraIntent(Context mContext) {
        //code to create camera intent
        Intent cameraInt = new Intent
                (MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraInt.resolveActivity(mContext.getPackageManager()) != null) {

            Uri photoURI = FileProvider.getUriForFile(mContext,
                    BuildConfig.APPLICATION_ID + ".provider",
                    getTempFile(mContext));

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                cameraInt.setClipData(ClipData.newRawUri("", photoURI));
                cameraInt.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (photoURI != null) {
                cameraInt.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }

        return cameraInt;
    }

    private static Intent getPickImageGalleryIntent() {
        return new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    private static Intent getPickFilesIntent(Activity activity) {
        Log.d(TAG, "getPickFilesIntent: Inside HPiker");
        String mimeType = "*/*";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("CONTENT_TYPE", mimeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooserIntent;
        if (activity.getPackageManager().resolveActivity(sIntent, 0) != null) {

            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent,
                    activity.getString(R.string.image_picker_open_file));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    new Intent[] { intent});
        } else {

            chooserIntent = Intent.createChooser(intent,
                    activity.getString(R.string.image_picker_open_file));
        }

        return chooserIntent;
    }

    //create temp file in android directory
    private static File getTempFile(Context context) {
        return HStorageUtil
                .getExternalStorageAppFilesFile(context, TEMP_IMAGE_NAME);
    }

    public static void onActivityResult(Context mContext,
                                        int requestCode,
                                        int resultCode,
                                        Intent data,
                                        OnImagePicked mCallback) {

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm;
            File fileInstance = getTempFile(mContext);
            Uri selectedFileUri;
            switch (requestCode) {

                case 13060 :
                    selectedFileUri = Uri.fromFile(fileInstance);
                    //scale down version
                    //bm = getImageResized(mContext, selectedFileUri);
                    int rotation = getRotation(mContext, selectedFileUri, true);
                    //DebugLog.e("Image Orentation: "+rotation);
                    bm = decodeFile(fileInstance);

                    //handle image orientation
                    if (bm != null) {
                        bm = rotate(bm,rotation);
                        //save fixed orientation bitmap into file
                        saveBitmap(bm, fileInstance);
                        mCallback.onSuccess(fileInstance, bm);
                    } else
                        mCallback.onError();
                    break;

                case 23060 :
                    selectedFileUri = data.getData();
                    fileInstance = HPathUtils.getRealPathFromURI(mContext, selectedFileUri);

                    if (fileInstance != null) {
                        //scale down version
                        //bm = getImageResized(mContext, selectedFileUri);
                        bm = decodeFile(fileInstance);

                        if (bm != null) {
                            //handle image orientation
                            bm = rotate(bm,
                                    getRotation(mContext, selectedFileUri, false));
                            mCallback.onSuccess(fileInstance, bm);
                        } else
                            mCallback.onError();
                    } else
                        mCallback.onError();

                    break;

                case 33060 :
                    Log.d(TAG, "onActivityResult: Inside HPiker");
                    selectedFileUri = data.getData();
                    fileInstance = HPathUtils.getRealPathFromURI(mContext, selectedFileUri);

                    if (fileInstance != null)
                        mCallback.onFileSuccess(fileInstance);
                    else
                        mCallback.onError();
                    break;
            }
        }
    }

    // Decodes image and scales it to reduce memory consumption
    private static Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),
                    null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=100;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
                    null,
                    o2);

            saveBitmap(bitmap, f);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveBitmap(Bitmap bm, File file) {
        OutputStream outStream;
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //to get rotation of bitmap
    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        Log.d(TAG, "Image rotation: " + rotation);
        return rotation;
    }

    //to get rotation of bitmap capture mFrom camera
    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    //to get rotation of bitmap picked mFrom gallery
    private static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri,
                    columns,
                    null,
                    null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    //to rotate bitmap
    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(
                    bm, 0, 0,
                    bm.getWidth(),
                    bm.getHeight(),
                    matrix,
                    true);
        }
        return bm;
    }

    public static abstract class OnImagePicked {

        public abstract void onSuccess(File imageFile, Bitmap bm);
        public void onFileSuccess(File imageFile){}
        public void onError() {}

    }


    /****************************UNSED**************************************/
    //Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;

        try {
            int[] sampleSizes = new int[]{8, 5, 2, 1};
            int i = 0;
            int minWidthQuality = 300;
            do {
                bm = decodeBitmap(context, selectedImage, sampleSizes[i]);

                Log.d(TAG, "ReSizer: new bitmap width = " + bm.getWidth());
                i++;
            } while (bm.getWidth() < minWidthQuality && i < sampleSizes.length);
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return bm;
    }

    //decode bitmap and manipulate there sample
    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver()
                    .openAssetFileDescriptor(theUri,
                            "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap actuallyUsableBitmap = null;
        if (fileDescriptor != null) {
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.getFileDescriptor(),
                    null,
                    options);

            Log.d(TAG, options.inSampleSize + " sample method bitmap ... " +
                    actuallyUsableBitmap.getWidth() + " " + actuallyUsableBitmap.getHeight());
        }

        return actuallyUsableBitmap;
    }
}
