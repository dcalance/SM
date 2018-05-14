package com.yjing.imageeditlibrary.editimage.contorl;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.yjing.imageeditlibrary.editimage.EditImageActivity;
import com.yjing.imageeditlibrary.editimage.fragment.AddTextFragment;
import com.yjing.imageeditlibrary.editimage.fragment.CropFragment;
import com.yjing.imageeditlibrary.editimage.fragment.FliterListFragment;
import com.yjing.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.yjing.imageeditlibrary.editimage.fragment.MosaicFragment;
import com.yjing.imageeditlibrary.editimage.fragment.PaintFragment;
import com.yjing.imageeditlibrary.editimage.fragment.RotateFragment;
import com.yjing.imageeditlibrary.editimage.fragment.StirckerFragment;
import com.yjing.imageeditlibrary.editimage.inter.ImageEditInte;

import java.util.List;

public class SaveMode {

    private static SaveMode saveMode = new SaveMode();
    private EditMode currentMode = EditMode.NONE;

    public static SaveMode getInstant() {
        return saveMode;
    }

    public void setMode(EditMode mode) {
        this.currentMode = mode;

    }

    public EditMode getMode() {
        return this.currentMode;
    }

    public enum EditMode {
        NONE,
        MOSAIC,
        PAINT,
        STICKERS,
        TEXT,
        CROP
    }

    /**
     * Created by wangyanjing on 2017/4/13.
     */

    public static class EditFactory {

        private final View fl_edit_bottom_height;
        private final View bottomView;
        private final View aboveView;
        public StirckerFragment mStirckerFragment;
        public FliterListFragment mFliterListFragment;
        public CropFragment mCropFragment;
        public RotateFragment mRotateFragment;
        public AddTextFragment mAddTextFragment;
        public PaintFragment mPaintFragment;
        public MosaicFragment mMosaicFragment;

        private final FragmentManager supportFragmentManager;
        private EditMode currentMode = EditMode.NONE;

        public EditFactory(EditImageActivity activity, View fl_edit_bottom_height, View bottomView, View aboveView) {
            supportFragmentManager = activity.getSupportFragmentManager();
            this.fl_edit_bottom_height = fl_edit_bottom_height;
            this.bottomView = bottomView;
            this.aboveView = aboveView;

            mStirckerFragment = StirckerFragment.newInstance(activity);
            mFliterListFragment = FliterListFragment.newInstance(activity);
            mCropFragment = CropFragment.newInstance(activity);
            mRotateFragment = RotateFragment.newInstance(activity);
            mAddTextFragment = AddTextFragment.newInstance(activity);
            mPaintFragment = PaintFragment.newInstance(activity);
            mMosaicFragment = MosaicFragment.newInstance(activity);

            supportFragmentManager.beginTransaction()
                    .add(bottomView.getId(), mAddTextFragment).hide(mAddTextFragment)
                    .add(fl_edit_bottom_height.getId(), mStirckerFragment).hide(mStirckerFragment)
                    .add(aboveView.getId(), mFliterListFragment).hide(mFliterListFragment)
                    .add(fl_edit_bottom_height.getId(), mCropFragment).hide(mCropFragment)
                    .add(aboveView.getId(), mRotateFragment).hide(mRotateFragment)
                    .add(aboveView.getId(), mPaintFragment).hide(mPaintFragment)
                    .add(aboveView.getId(), mMosaicFragment).hide(mMosaicFragment).commit();

        }

        public void setCurrentEditMode(EditMode mode) {
            currentMode = mode;
            Fragment x = getFragment(currentMode);
            if (x == null) {
                aboveView.setVisibility(View.GONE);
                bottomView.setVisibility(View.GONE);
                fl_edit_bottom_height.setVisibility(View.GONE);
                return;
            }
            hideFragment(x);
            if (x instanceof AddTextFragment) {
                bottomView.setVisibility(View.VISIBLE);
                fl_edit_bottom_height.setVisibility(View.GONE);
                aboveView.setVisibility(View.GONE);
            } else if (x instanceof CropFragment || x instanceof StirckerFragment) {
                fl_edit_bottom_height.setVisibility(View.VISIBLE);
                aboveView.setVisibility(View.GONE);
                bottomView.setVisibility(View.GONE);
            } else {
                aboveView.setVisibility(View.VISIBLE);
                fl_edit_bottom_height.setVisibility(View.GONE);
                bottomView.setVisibility(View.GONE);
            }
            supportFragmentManager.beginTransaction().show(x).commit();
        }


        private void hideFragment(Fragment x) {
            List<Fragment> fragments = supportFragmentManager.getFragments();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            for (Fragment fragment : fragments) {

                if ((x == null || fragment != x) && !fragment.isHidden() && !(fragment instanceof MainMenuFragment)) {
                    fragmentTransaction.hide(fragment);
                }
            }
            fragmentTransaction.commit();
        }


        public ImageEditInte getCurrentMode() {
            return (ImageEditInte) getFragment(currentMode);
        }

        public Fragment getFragment(EditMode mode) {
            switch (mode) {
                case STICKERS:
                    return mStirckerFragment;
                case CROP:
                    return mCropFragment;
                case TEXT:
                    return mAddTextFragment;
                case PAINT:
                    return mPaintFragment;
                case MOSAIC:
                    return mMosaicFragment;
                case NONE:
                    break;
            }//end switch
            return null;
        }

        public void setContainerVisiable(Fragment fragment, int visiable) {
            if (fragment instanceof AddTextFragment) {
                bottomView.setVisibility(visiable);
            } else if (fragment instanceof CropFragment || fragment instanceof StirckerFragment) {
                fl_edit_bottom_height.setVisibility(visiable);
            } else {
                aboveView.setVisibility(visiable);
            }
        }
    }
}
