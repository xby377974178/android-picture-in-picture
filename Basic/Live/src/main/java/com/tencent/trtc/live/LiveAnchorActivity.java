package com.tencent.trtc.live;

import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_LIVE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_PublishBigStream_ToCdn;
import static com.tencent.trtc.TRTCCloudDef.TRTC_SNAPSHOT_SOURCE_TYPE_STREAM;
import static com.tencent.trtc.TRTCCloudDef.TRTC_SNAPSHOT_SOURCE_TYPE_VIEW;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.example.basic.TRTCBaseActivity;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.base.util.LiteavLog;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.trtc.debug.Constant;
import com.tencent.trtc.debug.GenerateTestUserSig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * TRTC 主播视角下的RTC视频互动直播房间页面
 * <p>
 * 包含如下简单功能：
 * - 进入直播房间{@link LiveAnchorActivity#enterRoom()}
 * - 退出直播房间{@link LiveAnchorActivity#exitRoom()}
 * - 切换前置/后置摄像头{@link LiveAnchorActivity#switchCamera()}
 * - 打开/关闭摄像头{@link LiveAnchorActivity#muteVideo()}
 * - 关闭/打开麦克风{@link LiveAnchorActivity#muteAudio()}
 * <p>
 * 详见接入文档{https://cloud.tencent.com/document/product/647/43182}
 * <p>
 * Room View of Interactive Live Video Streaming for Anchor
 * <p>
 * Features:
 * - Enter a room: {@link LiveAnchorActivity#enterRoom()}
 * - Exit a room: {@link LiveAnchorActivity#exitRoom()}
 * - Switch between the front and rear cameras: {@link LiveAnchorActivity#switchCamera()}
 * - Turn on/off the camera: {@link LiveAnchorActivity#muteVideo()}
 * - Turn on/off the mic: {@link LiveAnchorActivity#muteAudio()}
 * <p>
 * For more information, please see the integration document {https://cloud.tencent.com/document/product/647/43182}.
 */
public class LiveAnchorActivity extends TRTCBaseActivity implements View.OnClickListener {
    private static final String TAG = "LiveBaseActivity";
    private static final int DEFAULT_CAPACITY = 5;

    private TXCloudVideoView mTxcvvAnchorPreviewView;
    private Button mButtonSwitchCamera;
    private Button mButtonMuteVideo;
    private Button mButtonMuteAudio;
    private ImageView mButtonBack;
    private TextView mTextTitle;
    private Button btn_start_publish;

    private TRTCCloud mTRTCCloud;
    private TXDeviceManager mTXDeviceManager;
    private TRTCCloudDef.TRTCParams mTRTCParams;
    private boolean mIsFrontCamera = true;
    private String mRoomId;
    private String mUserId;
    private List<String> mRemoteUidList;
    private List<LiveSubVideoView> mRemoteViewList;
    private boolean mMuteVideoFlag = true;
    private boolean mMuteAudioFlag = true;
    private String mRemoteUserid;
    private String mRemoteRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_activity_anchor);
        getSupportActionBar().hide();
        handleIntent();

        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTXDeviceManager = mTRTCCloud.getDeviceManager();
        mTRTCCloud.setListener(new TRTCCloudImplListener(LiveAnchorActivity.this));


        String model = Build.MODEL;
        String brand = Build.BRAND;

        Log.e("xbo", "onCreate: model = " + model);
        Log.e("xbo", "onCreate: brand = " + brand);


        if (checkPermission()) {
            initView();
            enterRoom();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitRoom();
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
        mRemoteUserid = "1001".equals(mUserId) ? "1002" : mUserId;
        mRemoteRoomId = "1001".equals(mRoomId) ? "1002" : mRoomId;
    }

    protected void initView() {
        mButtonBack = findViewById(R.id.iv_back);
        mTextTitle = findViewById(R.id.tv_room_number);
        mButtonMuteVideo = findViewById(R.id.btn_mute_video);
        mButtonMuteAudio = findViewById(R.id.btn_mute_audio);
        mButtonSwitchCamera = findViewById(R.id.live_btn_switch_camera);

        mTxcvvAnchorPreviewView = findViewById(R.id.live_cloud_view_main);
        if (!TextUtils.isEmpty(mRoomId)) {
            mTextTitle.setText(getString(R.string.live_roomid) + mRoomId);
        }

        mRemoteUidList = new ArrayList<>(DEFAULT_CAPACITY);
        mRemoteViewList = new ArrayList<>(DEFAULT_CAPACITY);

        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_1));
        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_2));
        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_3));
        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_4));
        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_5));
        mRemoteViewList.add((LiveSubVideoView) findViewById(R.id.live_cloud_view_6));

        mButtonBack.setOnClickListener(this);
        mTRTCCloud.setListener(new TRTCCloudImplListener(LiveAnchorActivity.this));
        for (int index = 0; index < mRemoteViewList.size(); index++) {
            mRemoteViewList.get(index).setLiveSubViewListener(new LiveSubViewListenerImpl(index));
        }
        mButtonMuteVideo.setOnClickListener(this);
        mButtonMuteAudio.setOnClickListener(this);
        mButtonSwitchCamera.setOnClickListener(this);

        btn_start_publish = findViewById(R.id.btn_start_publish);
        btn_start_publish.setOnClickListener(this);
    }

    public void enterRoom() {
        mTRTCParams = new TRTCCloudDef.TRTCParams();
        mTRTCParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        mTRTCParams.userId = mUserId;
        mTRTCParams.roomId = Integer.parseInt(mRoomId);
//        mTRTCParams.strRoomId = mRoomId;
        mTRTCParams.userSig = GenerateTestUserSig.genTestUserSig(mTRTCParams.userId);
        mTRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
        Log.e("trtc_api", "enterRoom: sdkAppId = " + mTRTCParams.sdkAppId );
        Log.e("trtc_api", "enterRoom: userId = " + mTRTCParams.userId );
        Log.e("trtc_api", "enterRoom: userSig = " + mTRTCParams.userSig );

        TRTCCloudDef.TRTCVideoEncParam trtcVideoEncParam = new TRTCCloudDef.TRTCVideoEncParam();
        trtcVideoEncParam.videoBitrate = 1200;
        trtcVideoEncParam.videoFps = 15;
        trtcVideoEncParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
        trtcVideoEncParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
        mTRTCCloud.setVideoEncoderParam(trtcVideoEncParam);
        mTRTCCloud.startLocalPreview(mIsFrontCamera, mTxcvvAnchorPreviewView);
        mTRTCCloud.enableAudioVolumeEvaluation(1000,true);
        mTRTCCloud.getDeviceManager().setSystemVolumeType(TXDeviceManager.TXSystemVolumeType.TXSystemVolumeTypeMedia);
        mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        mTRTCCloud.enterRoom(mTRTCParams, TRTC_APP_SCENE_LIVE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_mute_video) {
            muteVideo();
        } else if (id == R.id.btn_mute_audio) {
            muteAudio();
        } else if (id == R.id.live_btn_switch_camera) {
            switchCamera();
        } else if (id == R.id.iv_back) {
            finish();
        }else if (id == R.id.btn_start_publish) {
            startMcuMixPublish();
        }
    }

    private void startMcuMixPublish(){

        int width = 720;
        int height = 640;

        TRTCCloudDef.TRTCTranscodingConfig config = new TRTCCloudDef.TRTCTranscodingConfig();
        config.mode = TRTCCloudDef.TRTC_TranscodingConfigMode_Manual; // 全手动模式

        config.videoBitrate = 1800;
        config.videoFramerate = 20;
        config.videoWidth = width;
        config.videoHeight = height;
        config.videoGOP = 2;

        config.audioBitrate = 64;
        config.audioSampleRate = 48000;
        config.audioChannels = 1;
        config.audioCodec = 0;
        config.streamId = mRoomId;

        config.mixUsers = new ArrayList<>();
        TRTCCloudDef.TRTCMixUser localTrtcMixUser = new TRTCCloudDef.TRTCMixUser();
        localTrtcMixUser.x = 0 ;
        localTrtcMixUser.y = 0 ;
        localTrtcMixUser.width = width / 2;
        localTrtcMixUser.height = height;
        localTrtcMixUser.userId = mUserId;
        localTrtcMixUser.roomId = mRoomId;
        localTrtcMixUser.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
        localTrtcMixUser.renderMode = 0; // 默认值：视频流默认为0。0为裁剪，1为缩放，2为缩放并显示黑底。
        localTrtcMixUser.zOrder = 0;

        TRTCCloudDef.TRTCMixUser remoteTrtcMixUser = new TRTCCloudDef.TRTCMixUser();
        remoteTrtcMixUser.x = width / 2;
        remoteTrtcMixUser.y = 0 ;
        remoteTrtcMixUser.width = width / 2;
        remoteTrtcMixUser.height = height;
        remoteTrtcMixUser.userId = mRemoteUserid;
        remoteTrtcMixUser.roomId = mRemoteRoomId;
        remoteTrtcMixUser.streamType = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
        remoteTrtcMixUser.renderMode = 0; // 默认值：视频流默认为0。0为裁剪，1为缩放，2为缩放并显示黑底。
        remoteTrtcMixUser.zOrder = 1;

        config.mixUsers.add(localTrtcMixUser);
        config.mixUsers.add(remoteTrtcMixUser);
        Log.e("xbo", "startMcuMixPublish: config: " + config.toString());
        mTRTCCloud.setMixTranscodingConfig(config);
    }

    protected void switchCamera() {
        if (mIsFrontCamera) {
            mIsFrontCamera = false;
            mButtonSwitchCamera.setText(getString(R.string.live_user_front_camera));
        } else {
            mIsFrontCamera = true;
            mButtonSwitchCamera.setText(getString(R.string.live_user_back_camera));
        }
        mTXDeviceManager.switchCamera(mIsFrontCamera);
    }

    private void muteVideo() {
        if (mMuteVideoFlag) {
            mMuteVideoFlag = false;
            mTRTCCloud.switchRole(TRTCCloudDef.TRTCRoleAudience);
            mButtonMuteVideo.setText(getString(R.string.live_open_camera));
        } else {
            mMuteVideoFlag = true;
            mTRTCCloud.switchRole(TRTCCloudDef.TRTCRoleAnchor);
            mButtonMuteVideo.setText(getString(R.string.live_close_camera));
        }
    }

    private void muteAudio() {
        if (mMuteAudioFlag) {
            mMuteAudioFlag = false;
            mTRTCCloud.muteLocalAudio(true);
            mTRTCCloud.setAudioCaptureVolume(0);
            Log.e("xbo", "muteAudio: setAudioCaptureVolume = 0");
            mButtonMuteAudio.setText(getString(R.string.live_open_mic));
        } else {
            mMuteAudioFlag = true;
            mTRTCCloud.muteLocalAudio(false);
            mTRTCCloud.setAudioCaptureVolume(100);
            Log.e("xbo", "muteAudio: setAudioCaptureVolume = 100");
            mButtonMuteAudio.setText(getString(R.string.live_close_mic));
        }
    }

    protected class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<LiveAnchorActivity> mContext;

        @Override
        public void onEnterRoom(long result) {
        }


        public TRTCCloudImplListener(LiveAnchorActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {

        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {

        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            int index = mRemoteUidList.indexOf(userId);
            Log.d(TAG, "onUserVideoAvailable index " + index + ", available " + available + " userId " + userId);
            if (available) {
                if (index == -1 && !userId.equals(mUserId)) {
                    mRemoteUidList.add(userId);
                    refreshRemoteVideoViews();
                }
            } else {
                if (index != -1 && !userId.equals(mUserId)) {
                    TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
                    trtcRenderParams.fillMode = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
                    mTRTCCloud.setRemoteRenderParams(userId , TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG ,trtcRenderParams);
                    mTRTCCloud.stopRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                    mRemoteUidList.remove(index);
                    refreshRemoteVideoViews();
                }
            }
        }

        @Override
        public void onRecvSEIMsg(String userId, byte[] data) {
            super.onRecvSEIMsg(userId, data);
            String s = new String(data);
            Log.e("xbo", "onRecvSEIMsg: s = " + s + " userid："+ userId);
            Toast.makeText(LiveAnchorActivity.this,s , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onError");
            Log.e("xbo", "onError errCode = " +  errCode + " errMsg = " + errMsg);
            LiveAnchorActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }

        private void refreshRemoteVideoViews() {
            for (int i = 0; i < mRemoteViewList.size(); i++) {
                  if (i < mRemoteUidList.size()) {
                    String remoteUid = mRemoteUidList.get(i);
                     mRemoteViewList.get(i).setVisibility(View.VISIBLE);
                      // 开始显示用户userId的视频画面
                    mTRTCCloud.startRemoteView(remoteUid, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, mRemoteViewList.get(i).getVideoView());
                } else {
                    mRemoteViewList.get(i).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onSwitchRole(int errCode, String errMsg) {
            Log.e("xbo", "onSwitchRole: errCode = " + errCode + " errMsg = " + errMsg );
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
        }
    }

    protected class LiveSubViewListenerImpl implements LiveSubVideoView.LiveSubViewListener {

        private int mIndex;

        public LiveSubViewListenerImpl(int index) {
            mIndex = index;
        }

        @Override
        public void onMuteRemoteAudioClicked(View view) {
            boolean isSelected = view.isSelected();
            if (!isSelected) {
//                mTRTCCloud.muteRemoteAudio(mRemoteUidList.get(mIndex), true);
                view.setBackground(getResources().getDrawable(R.mipmap.live_subview_sound_mute));
            } else {
//                mTRTCCloud.muteRemoteAudio(mRemoteUidList.get(mIndex), false);
                view.setBackground(getResources().getDrawable(R.mipmap.live_subview_sound_unmute));
            }
            view.setSelected(!isSelected);
        }

        @Override
        public void onMuteRemoteVideoClicked(View view) {
            boolean isSelected = view.isSelected();
            if (!isSelected) {
                mTRTCCloud.stopRemoteView(mRemoteUidList.get(mIndex), TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                mRemoteViewList.get(mIndex).getMuteVideoDefault().setVisibility(View.VISIBLE);
                view.setBackground(getResources().getDrawable(R.mipmap.live_subview_video_mute));
            } else {

                TRTCCloudDef.TRTCRenderParams params = new TRTCCloudDef.TRTCRenderParams();
                params.fillMode = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;
                mTRTCCloud.setRemoteRenderParams(mRemoteUidList.get(mIndex), TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, params);

                mTRTCCloud.startRemoteView(mRemoteUidList.get(mIndex), TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                        mRemoteViewList.get(mIndex).getVideoView());

                view.setBackground(getResources().getDrawable(R.mipmap.live_subview_video_unmute));
                mRemoteViewList.get(mIndex).getMuteVideoDefault().setVisibility(View.GONE);
            }
            view.setSelected(!isSelected);
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
    protected void onPermissionGranted() {
        initView();
        enterRoom();
    }
}
