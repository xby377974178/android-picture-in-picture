package com.tencent.trtc.live;

import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_LIVE;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.basic.TRTCBaseActivity;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.trtc.debug.Constant;
import com.tencent.trtc.debug.GenerateTestUserSig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * TRTC 观众视角下的RTC视频互动直播房间页面
 */
public class FloatingWindowActivity extends TRTCBaseActivity implements View.OnClickListener {
    private static final String TAG = "LiveAudienceActivity";
    private TXCloudVideoView live_cloud_view_1;
    private TXCloudVideoView live_cloud_view_2;
    private LinearLayout ll_title_group;
    private LinearLayout ll_button_group;
    private Button mButtonMuteAudio;
    private TRTCCloud mTRTCCloud;
    private String mRoomId;
    private String mUserId;
    private boolean mMuteAudioFlag = false;

    LinkedList<TXCloudVideoView> txSubVideoViews;

    LinkedHashMap<String, TXCloudVideoView> videoViewMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_activity_floating_window);
        getSupportActionBar().hide();
        handleIntent();

        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.getDeviceManager().setSystemVolumeType(TXDeviceManager.TXSystemVolumeType.TXSystemVolumeTypeMedia);
        mTRTCCloud.setListener(new TRTCCloudImplListener(this));

        if (checkPermission()) {
            initView();
            enterRoom();
        }

        // 开启画中画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Rational aspectRatio = new Rational(5, 5);// 设置画中画窗口比例
            setPictureInPictureParams(new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .setAutoEnterEnabled(true)
                    .build());
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.getStringExtra(Constant.USER_ID) != null) {
                mUserId = intent.getStringExtra(Constant.USER_ID);
            }
            if (intent.getStringExtra(Constant.ROOM_ID) != null) {
                mRoomId = intent.getStringExtra(Constant.ROOM_ID);
            }
        }
    }

    protected void initView() {
        videoViewMap = new LinkedHashMap<>();
        txSubVideoViews = new LinkedList<>();
        live_cloud_view_1 = findViewById(R.id.live_cloud_view_1);
        live_cloud_view_2 = findViewById(R.id.live_cloud_view_2);
        txSubVideoViews.add(live_cloud_view_1);
        txSubVideoViews.add(live_cloud_view_2);

        ll_title_group = findViewById(R.id.ll_title_group);
        ll_button_group = findViewById(R.id.ll_button_group);

        ImageView mImageBack = findViewById(R.id.iv_back);
        TextView mTextTitle = findViewById(R.id.tv_room_number);
        mButtonMuteAudio = findViewById(R.id.btn_remote_mute_audio);

        ((TextView) findViewById(R.id.tv_room_number)).setText(mRoomId);

        if (!TextUtils.isEmpty(mRoomId)) {
            mTextTitle.setText(getString(R.string.live_roomid) + mRoomId);
        }
        mImageBack.setOnClickListener(this);
        mButtonMuteAudio.setOnClickListener(this);
    }

    protected void enterRoom() {
        TRTCCloudDef.TRTCParams mTRTCParams = new TRTCCloudDef.TRTCParams();
        mTRTCParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        mTRTCParams.userId = mUserId;
        mTRTCParams.roomId = Integer.parseInt(mRoomId);
//        mTRTCParams.strRoomId = mRoomId;
        mTRTCParams.userSig = GenerateTestUserSig.genTestUserSig(mTRTCParams.userId);
        mTRTCParams.role = TRTCCloudDef.TRTCRoleAudience;

        Log.e("xbo", "enterRoom: mTRTCParams = " + mTRTCParams);
        Log.e("xbo", "enterRoom: sdkAppId = " + mTRTCParams.sdkAppId);
        Log.e("xbo", "enterRoom: roomId = " + mTRTCParams.roomId);
        Log.e("xbo", "enterRoom: strRoomId = " + mTRTCParams.strRoomId);
        Log.e("xbo", "enterRoom: userId = " + mTRTCParams.userId);
        Log.e("xbo", "enterRoom: userSig = " + mTRTCParams.userSig);
        Log.e("xbo", "enterRoom: role = " + mTRTCParams.role);

        mTRTCCloud.enterRoom(mTRTCParams, TRTC_APP_SCENE_LIVE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_remote_mute_audio) {
            muteAudio();
        } else if (id == R.id.iv_back) {
            finish();
        }
    }

    private void muteAudio() {
        mMuteAudioFlag = !mMuteAudioFlag;
        if (mMuteAudioFlag) {
            mTRTCCloud.muteAllRemoteAudio(true);
            mButtonMuteAudio.setText(getString(R.string.live_close_mute_audio));
        } else {
            mTRTCCloud.muteAllRemoteAudio(false);
            mButtonMuteAudio.setText(getString(R.string.live_mute));
        }
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 检查是否支持画中画模式
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // 创建画中画模式的参数构建器
//            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
//            // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
//            // 下面的10/5=2，表示画中画窗口的宽度是高度的两倍
//            Rational aspectRatio = new Rational(5, 10);
//            // 设置画中画窗口的宽高比例
//            builder.setAspectRatio(aspectRatio);
//            // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
//            enterPictureInPictureMode(builder.build());
//        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            // 进入画中画模式，隐藏不必要的 UI 组件
            hideUIComponents();
        } else {
            // 退出画中画模式，恢复UI 组件
            showUIComponents();
        }
    }

    private void hideUIComponents() {
        // 隐藏 UI 组件的实现
        Log.e("xbo", "hideUIComponents: ");
        ll_title_group.setVisibility(View.GONE);
        ll_button_group.setVisibility(View.GONE);
    }

    private void showUIComponents() {
        // 显示 UI 组件的实现
        Log.e("xbo", "showUIComponents: ");
        ll_title_group.setVisibility(View.VISIBLE);
        ll_button_group.setVisibility(View.VISIBLE);
    }

    protected class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<FloatingWindowActivity> mContext;

        public TRTCCloudImplListener(FloatingWindowActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            Log.e("xbo", "onRemoteUserEnterRoom: userId = " + userId);
        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            Log.e("xbo", "onRemoteUserLeaveRoom: userId = " + userId + " reason = " + reason);
        }


        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
            // Log.e("xbo", "onUserVoiceVolume: totalVolume = " + totalVolume);
            for (TRTCCloudDef.TRTCVolumeInfo info : userVolumes) {
                // Log.e("xbo", "onUserVoiceVolume: info = " + info.userId + "," + info.volume + "," + info.vad);
            }
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            Log.e("xbo", "onUserVideoAvailable  available " + available + " userId " + userId);

            Set<String> set = videoViewMap.keySet();
            TXCloudVideoView videoView;

            if (available) {
                if (set.contains(userId)) {
                    videoView = videoViewMap.get(userId);
                } else {
                    videoView = txSubVideoViews.poll();
                    if (videoView == null) return;  // 如果房间人数超过预览人数则不处理
                    videoViewMap.put(userId, videoView);
                }
                TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
                params.fillMode = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
                mTRTCCloud.setRemoteRenderParams(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, params);
                mTRTCCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, videoView);
            } else {
                if (set.contains(userId)) {
                    TXCloudVideoView remove = videoViewMap.remove(userId);
                    if (null != remove) {
                        txSubVideoViews.addFirst(remove);
                    }
                }
                mTRTCCloud.stopRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            }
        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.e("xbo", "onError: errCode = " + errCode + " errMsg = " + errMsg);
            FloatingWindowActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }

    protected void exitRoom() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopLocalAudio();
            mTRTCCloud.stopLocalPreview();
            mTRTCCloud.exitRoom();
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("xbo", "onDestroy");
        exitRoom();
    }

    @Override
    protected void onPermissionGranted() {
        initView();
        enterRoom();
    }
}
