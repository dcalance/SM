package com.yjing.imageeditlibrary.editimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.yjing.imageeditlibrary.BaseActivity;
import com.yjing.imageeditlibrary.R;
import com.yjing.imageeditlibrary.editimage.contorl.SaveMode;
import com.yjing.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;
import com.yjing.imageeditlibrary.editimage.inter.SaveCompletedInte;
import com.yjing.imageeditlibrary.utils.BitmapUtils;
import com.yjing.imageeditlibrary.utils.FileUtils;
import com.yjing.imageeditlibrary.editimage.view.CropImageView;
import com.yjing.imageeditlibrary.editimage.view.CustomPaintView;
import com.yjing.imageeditlibrary.editimage.view.RotateImageView;
import com.yjing.imageeditlibrary.editimage.view.StickerView;
import com.yjing.imageeditlibrary.editimage.view.TextStickerView;
import com.yjing.imageeditlibrary.editimage.view.imagezoom.ImageViewTouch;
import com.yjing.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;
import com.yjing.imageeditlibrary.editimage.view.mosaic.MosaicView;

/**
 * 图片编辑 主页面
 * 包含 1.贴图 2.滤镜 3.剪裁 4.底图旋转 功能
 */
public class EditImageActivity extends BaseActivity {
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    public String filePath;
    public String saveFilePath;
    private int imageWidth, imageHeight;
    private LoadImageTask mLoadImageTask;

    protected int mOpTimes = 0;
    protected boolean isBeenSaved = false;

    private EditImageActivity mContext;
    public Bitmap mainBitmap;
    public ImageViewTouch mainImage;
    private View backBtn;

    public ViewFlipper bannerFlipper;
    private View applyBtn;
    private View saveBtn;
    public StickerView mStickerView;
    public CropImageView mCropPanel;
    public RotateImageView mRotatePanel;
    public TextStickerView mTextStickerView;
    public CustomPaintView mPaintView;
    public MosaicView mMosaicView;

    private MainMenuFragment mMainMenuFragment;// Menu

    private SaveImageTask mSaveImageTask;
    public SaveMode.EditFactory editFactory;
    public View fl_main_menu;
    public View banner;

    public static void start(Activity context, final String editImagePath, final String outputPath, final int requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        context.startActivityForResult(it, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitImageLoader();
        setContentView(R.layout.activity_image_edit);
        initView();
        getData();
    }

    private void getData() {
        filePath = getIntent().getStringExtra(FILE_PATH);
        saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);
        loadImage(filePath);
    }

    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        banner = findViewById(R.id.banner);
        applyBtn = findViewById(R.id.apply);
        applyBtn.setOnClickListener(new ApplyBtnClick());
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new SaveBtnClick(true, null));

        mainImage = (ImageViewTouch) findViewById(R.id.main_image);
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mStickerView = (StickerView) findViewById(R.id.sticker_panel);
        mCropPanel = (CropImageView) findViewById(R.id.crop_panel);
        mRotatePanel = (RotateImageView) findViewById(R.id.rotate_panel);
        mTextStickerView = (TextStickerView) findViewById(R.id.text_sticker_panel);
        mPaintView = (CustomPaintView) findViewById(R.id.custom_paint_view);
        mMosaicView = (MosaicView) findViewById(R.id.mosaic_view);


        View fl_edit_bottom_height = findViewById(R.id.fl_edit_bottom_height);
        View fl_edit_bottom_full = findViewById(R.id.fl_edit_bottom_full);
        View fl_edit_above_mainmenu = findViewById(R.id.fl_edit_above_mainmenu);
        editFactory = new SaveMode.EditFactory(this, fl_edit_bottom_height, fl_edit_bottom_full, fl_edit_above_mainmenu);

        fl_main_menu = findViewById(R.id.fl_main_menu);
        mMainMenuFragment = MainMenuFragment.newInstance(this);
        this.getSupportFragmentManager().beginTransaction().add(R.id.fl_main_menu, mMainMenuFragment)
                .show(mMainMenuFragment).commit();
    }

    public void backToMain() {
        ImageEditInte currentMode = editFactory.getCurrentMode();
        if (SaveMode.getInstant().getMode() != SaveMode.EditMode.NONE && currentMode != null) {
            currentMode.backToMain();
            SaveMode.getInstant().setMode(SaveMode.EditMode.NONE);
        }
        editFactory.setCurrentEditMode(SaveMode.EditMode.NONE);
    }

    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            mainImage.setImageBitmap(result);
            mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            // mainImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
        }
    }

    @Override
    public void onBackPressed() {
        Boolean ifFinish = true;
        if ((SaveMode.getInstant().getMode() == SaveMode.EditMode.CROP || SaveMode.getInstant().getMode() == SaveMode.EditMode.TEXT) && editFactory.getCurrentMode() != null) {
            ifFinish = false;
        }
        backToMain();

        if (!ifFinish) {
            return;
        }

        if (canAutoExit()) {
            onSaveTaskDone();
        } else if (false) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.exit_without_save)
                    .setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mContext.finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            finish();
        }
    }

    private final class ApplyBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            ImageEditInte currentMode = editFactory.getCurrentMode();
            currentMode.appleEdit(null);
        }
    }

    public final class SaveBtnClick implements OnClickListener {
        private final Boolean isSaveImageToLocal;
        private final SaveCompletedInte inte;
        private SaveMode.EditMode[] modes;
        private int modeIndex;

        public SaveBtnClick(Boolean isSaveImageToLocal, SaveCompletedInte inte) {
            this.isSaveImageToLocal = isSaveImageToLocal;
            this.inte = inte;
        }

        @Override
        public void onClick(View v) {
            modes = SaveMode.EditMode.values();
            modeIndex = 0;
            applyEdit();

//            ImageEditInte currentMode = editFactory.getCurrentMode();
//            if (currentMode != null) {
//                currentMode.appleEdit(new SaveCompletedInte() {
//                    @Override
//                    public void completed() {
//                        if (mOpTimes == 0) {//并未修改图片
//                            onSaveTaskDone();
//                        } else {
//                            doSaveImage();
//                        }
//                    }
//                });
//            } else {
//                if (mOpTimes == 0) {//并未修改图片
//                    onSaveTaskDone();
//                } else {
//                    doSaveImage();
//                }
//            }

        }

        private void applyEdit() {
            if (modes[modeIndex] == SaveMode.EditMode.NONE || modes[modeIndex] == SaveMode.EditMode.CROP) {
                modeIndex++;
                if (modeIndex < modes.length) {
                    applyEdit();
                } else {
                    if (isSaveImageToLocal) {
                        if (mOpTimes == 0) {
                            onSaveTaskDone();
                        } else {
                            doSaveImage();
                        }
                    }
                    if (inte != null) {
                        inte.completed();
                    }
                }
                return;
            }
            ImageEditInte fragment = (ImageEditInte) editFactory.getFragment(modes[modeIndex++]);
            fragment.appleEdit(new SaveCompletedInte() {
                @Override
                public void completed() {
                    if (modeIndex < modes.length) {
                        applyEdit();
                    } else {
                        if (isSaveImageToLocal) {
                            if (mOpTimes == 0) {//并未修改图片
                                onSaveTaskDone();
                            } else {
                                doSaveImage();
                            }
                        }
                        if (inte != null) {
                            inte.completed();
                        }
                    }
                }
            });
        }
    }

    protected void doSaveImage() {
        if (mOpTimes <= 0)
            return;

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        mSaveImageTask = new SaveImageTask();
        mSaveImageTask.execute(mainBitmap);
    }

    public void changeMainBitmap(Bitmap newBit) {
        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {// 回收
                mainBitmap.recycle();
            }
        }
        mainBitmap = newBit;
        mainImage.setImageBitmap(mainBitmap);
        mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        increaseOpTimes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }
    }

    public void increaseOpTimes() {
        mOpTimes++;
        isBeenSaved = false;
    }

    public void resetOpTimes() {
        isBeenSaved = true;
    }

    public boolean canAutoExit() {
        return isBeenSaved || mOpTimes == 0;
    }

    protected void onSaveTaskDone() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SAVE_FILE_PATH, saveFilePath);
        returnIntent.putExtra(IMAGE_IS_EDIT, mOpTimes > 0);

        FileUtils.ablumUpdate(this, saveFilePath);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private final class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        private Dialog dialog;

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(saveFilePath))
                return false;

            return BitmapUtils.saveBitmap(params[0], saveFilePath);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = EditImageActivity.getLoadingDialog(mContext, R.string.saving_image, false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result) {
                resetOpTimes();
                onSaveTaskDone();
            } else {
                Toast.makeText(mContext, R.string.save_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
