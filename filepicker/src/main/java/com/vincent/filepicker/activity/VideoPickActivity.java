package com.vincent.filepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vincent.filepicker.Constant;
import com.vincent.filepicker.DividerGridItemDecoration;
import com.vincent.filepicker.R;
import com.vincent.filepicker.adapter.OnSelectStateListener;
import com.vincent.filepicker.adapter.VideoPickAdapter;
import com.vincent.filepicker.filter.FileFilter;
import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.Directory;
import com.vincent.filepicker.filter.entity.VideoFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2016/10/21
 * Time: 14:02
 */

public class VideoPickActivity extends BaseActivity {

    //=== request code
    public final static int REQUEST_VIDEO = 99;

    public static final String IS_NEED_CAMERA = "IsNeedCamera";

    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final int COLUMN_NUMBER = 3;
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private Toolbar mTbImagePick;
    private RecyclerView mRecyclerView;
    private VideoPickAdapter mAdapter;
    private boolean isNeedCamera;
    private ArrayList<VideoFile> mSelectedList = new ArrayList<>();

    public final static String EXTRA_MAX_SELECT_NUM = "MaxSelectNum";

    public final static String EXTRA_SHOW_CAMERA = "ShowCamera";

    @Override
    void permissionGranted() {
        loadData();
    }

    /**
     * 启动视频选择
     *
     * @param activity
     * @param maxSelectNum 最大选择数量
     * @param isShow       是否展示摄像头
     * @param requestCode  请求码
     */
    public static void start(Activity activity, int maxSelectNum, boolean isShow, int requestCode) {
        Intent intent = new Intent(activity, VideoPickActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_SHOW_CAMERA, isShow);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动视频选择
     * @param fragment
     * @param maxSelectNum
     * @param isShow
     * @param requestCode
     */
    public static void start(Fragment fragment, int maxSelectNum, boolean isShow, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), VideoPickActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_SHOW_CAMERA, isShow);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.filepicker_activity_video_pick);

        mMaxNumber = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, DEFAULT_MAX_NUMBER);
        isNeedCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);

        initView();
        super.onCreate(savedInstanceState);

    }

    private void initView() {
        mTbImagePick = (Toolbar) findViewById(R.id.tb_video_pick);
        mTbImagePick.setTitle(mCurrentNumber + "/" + mMaxNumber);
        setSupportActionBar(mTbImagePick);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_video_pick);
        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMN_NUMBER);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));

        mAdapter = new VideoPickAdapter(this, isNeedCamera, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<VideoFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, VideoFile file) {
                if (state) {
                    mSelectedList.add(file);
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    mCurrentNumber--;
                }
                mTbImagePick.setTitle(mCurrentNumber + "/" + mMaxNumber);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.REQUEST_CODE_TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(mAdapter.mVideoPath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);

                    loadData();
                }
                break;
        }
    }

    private void loadData() {
        FileFilter.getVideos(this, new FilterResultCallback<VideoFile>() {
            @Override
            public void onResult(List<Directory<VideoFile>> directories) {
                List<VideoFile> list = new ArrayList<>();
                for (Directory<VideoFile> directory : directories) {
                    list.addAll(directory.getFiles());
                }

                for (VideoFile file : mSelectedList) {
                    int index = list.indexOf(file);
                    if (index != -1) {
                        list.get(index).setSelected(true);
                    }
                }

                mAdapter.refresh(list);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filepicker_menu_image_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_VIDEO, mSelectedList);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
