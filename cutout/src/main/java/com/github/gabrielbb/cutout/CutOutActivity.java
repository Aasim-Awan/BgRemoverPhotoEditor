package com.github.gabrielbb.cutout;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.gabrielbb.cutout.CutOut.CUTOUT_EXTRA_INTRO;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dev.eren.removebg.RemoveBg;

public class CutOutActivity extends AppCompatActivity {

    private static final int INTRO_REQUEST_CODE = 4;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private static final int IMAGE_CHOOSER_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 3;

    private static final String INTRO_SHOWN = "INTRO_SHOWN";
    public static final String CUTOUT_IMAGE_SOURCE = "IMAGE_URI";
    FrameLayout loadingModal;
    private GestureView gestureView;
    private DrawView drawView;
    private LinearLayout manualClearSettingsLayout;

    private static final short MAX_ERASER_SIZE = 150;
    private static final short BORDER_SIZE = 45;
    private static final float MAX_ZOOM = 4F;
    private Button autoClearButton;
    private Button manualClearButton;

    private RemoveBg remover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_edit);

        //Toolbar toolbar = findViewById(R.id.photo_edit_toolbar);
//        toolbar.setBackgroundColor(Color.BLACK);
//        toolbar.setTitleTextColor(Color.WHITE);
//        setSupportActionBar(toolbar);


        SeekBar strokeBar = findViewById(R.id.strokeBar);
        strokeBar.setMax(MAX_ERASER_SIZE);
        strokeBar.setProgress(30);

        gestureView = findViewById(R.id.gestureView);

        autoClearButton = findViewById(R.id.auto_clear_button);
        manualClearButton = findViewById(R.id.manual_clear_button);
        drawView = findViewById(R.id.drawView);
        drawView.setDrawingCacheEnabled(true);
        //drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);

        drawView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        drawView.setStrokeWidth(strokeBar.getProgress());
        strokeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drawView.setStrokeWidth(seekBar.getProgress());
            }
        });

        loadingModal = findViewById(R.id.loadingModal);
        loadingModal.setVisibility(INVISIBLE);

        drawView.setLoadingModal(loadingModal);

        manualClearSettingsLayout = findViewById(R.id.manual_clear_settings_layout);
        manualClearSettingsLayout.setVisibility(INVISIBLE);
        setUndoRedo();
        initializeActionButtons();

        drawView.setAction(DrawView.DrawViewAction.AUTO_CLEAR);
        manualClearSettingsLayout.setVisibility(INVISIBLE);
        autoClearButton.setActivated(true);
        manualClearButton.setActivated(false);
//        Button autoClearButton = findViewById(R.id.auto_clear_button);
//        autoClearButton.setActivated(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

//            if (toolbar.getNavigationIcon() != null) {
//                toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
//            }

        }

        ImageButton doneButton = findViewById(R.id.btn_done);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        if (getIntent().getBooleanExtra(CUTOUT_EXTRA_INTRO, false) && !getPreferences(Context.MODE_PRIVATE).getBoolean(INTRO_SHOWN, false)) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivityForResult(intent, INTRO_REQUEST_CODE);
        } else if (getIntent().getStringExtra(CUTOUT_IMAGE_SOURCE) != null) {
            //Log.d("mURICHeck", Uri.parse(getIntent().getStringExtra(CUTOUT_IMAGE_SOURCE)) + "");
            Bitmap bmp = fileToBitmap(new File(getIntent().getStringExtra(CUTOUT_IMAGE_SOURCE)));

          //  RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
           // FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(bmp.getHeight(), bmp.getWidth());

            //findViewById(R.id.drawViewLayout).setLayoutParams(params2);
            //drawView.setLayoutParams(params);
            setDrawViewBitmap(bmp);
        } else {
            start();
        }

        doneButton.setOnClickListener(v -> {
            Bitmap drawViewBitmap = drawView.getDrawingCache();
            saveDrawing(drawViewBitmap);
        });
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard the image?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle YES click
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public Bitmap fileToBitmap(File file) {
        if (file != null && file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    private void saveDrawing(Bitmap bitmap) {
        if (bitmap != null) {
            File savedUri = saveBitmapToFile(bitmap);
            if (savedUri != null) {
                Log.d("CUTOUT_ACTIVITY", "Saved URI: " + savedUri);
                Intent intent = new Intent();
                intent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, savedUri.toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Log.e("CUTOUT_ACTIVITY", "Failed to save the bitmap");
            }
        } else {
            Log.e("CUTOUT_ACTIVITY", "Bitmap is null");
        }
    }

    /**
     * Helper method to save the bitmap to a file and return the URI.
     */
    private File saveBitmapToFile(Bitmap bitmap) {
        File tempDir = new File(getExternalFilesDir("Temp_filters"), "");

        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String fileName = "filtered_image" + System.currentTimeMillis() + ".png";
        File file = new File(tempDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            return file;
        } catch (IOException e) {
            Log.e("CutOutActivity", "Error saving bitmap: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri getExtraSource() {
        return getIntent().hasExtra(CutOut.CUTOUT_EXTRA_SOURCE) ? (Uri) getIntent().getParcelableExtra(CutOut.CUTOUT_EXTRA_SOURCE) : null;
    }

    private void start() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //   Uri uri = getExtraSource();

            /*if (getIntent().getBooleanExtra(CutOut.CUTOUT_EXTRA_CROP, false)) {

                CropImage.ActivityBuilder cropImageBuilder;
                if (uri != null) {
                    cropImageBuilder = CropImage.activity(uri);
                } else {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        cropImageBuilder = CropImage.activity();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_CODE);
                        return;
                    }
                }

                cropImageBuilder = cropImageBuilder.setGuidelines(CropImageView.Guidelines.ON);
                cropImageBuilder.start(this);
            } else {
                if (uri != null) {
                    setDrawViewBitmap(uri);
                } else {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        EasyImage.openChooserWithGallery(this, getString(R.string.image_chooser_message), IMAGE_CHOOSER_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_CODE);
                    }
                }
            }*/

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
        }
    }

    private void startSaveDrawingTask() {
        SaveDrawingTask task = new SaveDrawingTask(this);

        int borderColor;
        if ((borderColor = getIntent().getIntExtra(CutOut.CUTOUT_EXTRA_BORDER_COLOR, -1)) != -1) {
            Bitmap image = BitmapUtility.getBorderedBitmap(this.drawView.getDrawingCache(), borderColor, BORDER_SIZE);
            task.execute(image);

        } else {
            task.execute(this.drawView.getDrawingCache());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            start();
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private void activateGestureView() {
        gestureView.getController().getSettings().setMaxZoom(MAX_ZOOM).setDoubleTapZoom(-1f) // Falls back to max zoom level
                .setPanEnabled(true).setZoomEnabled(true).setDoubleTapEnabled(true).setOverscrollDistance(0f, 0f).setOverzoomFactor(2f);
    }

    private void deactivateGestureView() {
        gestureView.getController().getSettings().setPanEnabled(false).setZoomEnabled(false).setDoubleTapEnabled(false);
    }

    private void initializeActionButtons() {
        //Button zoomButton = findViewById(R.id.zoom_button);

        autoClearButton.setActivated(true);
        autoClearButton.setOnClickListener((buttonView) -> {
            if (!autoClearButton.isActivated()) {
                drawView.setAction(DrawView.DrawViewAction.AUTO_CLEAR);
                manualClearSettingsLayout.setVisibility(INVISIBLE);
                autoClearButton.setActivated(true);
                manualClearButton.setActivated(false);
                // zoomButton.setActivated(false);
                deactivateGestureView();
            }
        });

        manualClearButton.setActivated(false);
        // drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
        manualClearButton.setOnClickListener((buttonView) -> {
            if (!manualClearButton.isActivated()) {
                drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
                manualClearSettingsLayout.setVisibility(VISIBLE);
                manualClearButton.setActivated(true);
                autoClearButton.setActivated(false);
                // zoomButton.setActivated(false);
                deactivateGestureView();
            }
        });

//zoomButton.setActivated(false);
        //    deactivateGestureView();
        //     zoomButton.setOnClickListener((buttonView) -> {
//if (!zoomButton.isActivated()) {
//                drawView.setAction(DrawView.DrawViewAction.ZOOM);
//                manualClearSettingsLayout.setVisibility(INVISIBLE);
//                zoomButton.setActivated(true);
//                manualClearButton.setActivated(false);
//                autoClearButton.setActivated(false);
//                activateGestureView();
//            }

        //  });
    }

    private void setUndoRedo() {
        Button undoButton = findViewById(R.id.undo);
        undoButton.setEnabled(false);
        undoButton.setOnClickListener(v -> undo());
        Button redoButton = findViewById(R.id.redo);
        redoButton.setEnabled(false);
        redoButton.setOnClickListener(v -> redo());

        drawView.setButtons(undoButton, redoButton);
    }

    void exitWithError(Exception e) {
        Intent intent = new Intent();
        intent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, e);
        setResult(CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE, intent);
        finish();
    }

    private void setDrawViewBitmap(Bitmap bitmap) {
        try {
            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            drawView.setBitmap(bitmap);
        } catch (Exception e) {
            exitWithError(e);
        }
    }

    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (requestCode == IMAGE_PICK_CODE) {
                setDrawViewBitmap(imageUri); // Handle image from gallery
            } else if (requestCode == CAMERA_REQUEST_CODE && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                drawView.setBitmap(bitmap); // Handle image from camera
            }
        }*/
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {

                setDrawViewBitmap(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                exitWithError(result.getError());
            } else {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        } else if (requestCode == INTRO_REQUEST_CODE) {
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            editor.putBoolean(INTRO_SHOWN, true);
            editor.apply();
            start();
        } else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    exitWithError(e);
                }

                @Override
                public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                    setDrawViewBitmap(Uri.parse(imageFile.toURI().toString()));
                }

                @Override
                public void onCanceled(EasyImage.ImageSource source, int type) {
                    // Cancel handling, removing taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(CutOutActivity.this);
                        if (photoFile != null) photoFile.delete();
                    }

                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }
    }*/

    private void undo() {
        drawView.undo();
    }

    private void redo() {
        drawView.redo();
    }

}