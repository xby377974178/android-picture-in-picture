package com.tencent.trtc.apiexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.basic.TRTCBaseActivity;
import com.tencent.trtc.live.LiveEnterActivity;

/**
 * TRTC API-Example 主页面
 *
 * 其中包含
 * 基础功能模块如下：
 * - 视频互动直播模块{@link LiveEnterActivity}
 * 进阶功能模块如下：
 */
public class MainActivity extends TRTCBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToolKitService.start(this);
        getSupportActionBar().hide();

        checkPermission();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.launch_view).setVisibility(View.GONE);
            }
        }, 1000);

//        findViewById(R.id.ll_audio_call).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, AudioCallingEnterActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_video_call).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, VideoCallingEnterActivity.class);
//                startActivity(intent);
//            }
//        });

        findViewById(R.id.ll_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LiveEnterActivity.class);
                startActivity(intent);
            }
        });

//        findViewById(R.id.ll_voice_chat_room).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, VoiceChatRoomEnterActivity.class);
//                startActivity(intent);
//
//            }
//        });
//
//        findViewById(R.id.ll_screen_share).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ScreenEntranceActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_string_room_id).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, StringRoomIdActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_video_quality).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SetVideoQualityActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_audio_quality).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SetAudioQualityActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_render_params).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SetRenderParamsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_speed_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SpeedTestActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_pushcdn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, PushCDNSelectRoleActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_custom_camera).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, CustomCameraActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_audio_effect).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SetAudioEffectActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_audio_bgm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SetBGMActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_local_video_share).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, LocalVideoShareActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_local_record).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, LocalRecordActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_join_multiple_room).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, JoinMultipleRoomActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_sei_message).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SendAndReceiveSEIMessageActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_switch_room).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, SwitchRoomActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_connect_other_room).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, RoomPKActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.ll_third_beauty).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ThirdBeautyEnterActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    protected void onPermissionGranted() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
