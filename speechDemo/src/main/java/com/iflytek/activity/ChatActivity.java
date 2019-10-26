package com.iflytek.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.iflytek.adapter.ChatAdapter;
import com.iflytek.bean.AudioMsgBody;
import com.iflytek.bean.FileMsgBody;
import com.iflytek.bean.ImageMsgBody;
import com.iflytek.bean.Message;
import com.iflytek.bean.MsgSendStatus;
import com.iflytek.bean.MsgType;
import com.iflytek.bean.TextMsgBody;
import com.iflytek.bean.VideoMsgBody;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.speech.setting.ChatSettings;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.util.ChatUiHelper;
import com.iflytek.util.FileUtils;
import com.iflytek.util.LogUtil;
import com.iflytek.util.PictureFileUtil;
import com.iflytek.voicedemo.MainActivity;
import com.iflytek.voicedemo.R;
import com.iflytek.voicedemo.TtsDemo;
import com.iflytek.widget.MediaManager;
import com.iflytek.widget.RecordButton;
import com.iflytek.widget.StateButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    String TAG = "FLOP";

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//表情,添加底部布局
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//发送按钮
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//录音图片
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//录音按钮
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//表情布局
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//添加布局
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//下拉刷新
    private ChatAdapter mAdapter;
    public static final String mSenderId = "right";
    public static final String mTargetId = "left";
    public static final int REQUEST_CODE_IMAGE = 0000;
    public static final int REQUEST_CODE_VEDIO = 1111;
    public static final int REQUEST_CODE_FILE = 2222;


    TtsDemo ttsDemo = new TtsDemo();

    /***********************设置页面***********************/
    @OnClick(R.id.common_toolbar_settings)
    void settings() {
        startActivity(new Intent(this, ChatSettings.class));
    }

    @OnLongClick(R.id.common_toolbar_settings)
    boolean longClick() {
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    /***********************语音合成***********************/

    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 默认发音人
    private String voicer = "xiaoyan";

    String texts = "";

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    // 当前正在播放的音频发送方
    public String audioSender = "";

    // 云端/本地单选按钮
    private RadioGroup mRadioGroup;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private SharedPreferences mSharedPreferences;

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "onSpeakBegin: 开始播放音频");
            //开始语音合成时停止其他正在播放的音频
            if (ivAudio != null) {
                if (audioSender.equals("left")) {
                    ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                } else {
                    ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                }
                ivAudio = null;
                MediaManager.reset();
            }
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "onSpeakPaused: ");
        }

        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "onSpeakResumed: ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            Log.i(TAG, "onBufferProgress: " + String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            Log.d(TAG, "onSpeakProgress: " + String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                completeAudioMessage(mTts.getParameter(SpeechConstant.TTS_AUDIO_PATH));
                Log.i(TAG, "onCompleted: 播放完成");
            } else if (error != null) {
                Log.i(TAG, "onCompleted: " + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
        }
    };

    private void setTtsParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //支持实时音频返回，仅在synthesizeToUri条件下支持
            //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");

        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // pcm格式可能会打开失败
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/wav/" + System.currentTimeMillis() + ".wav");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.i(TAG, "语音合成初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /***********************语音听写***********************/
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String resultType = "json";
    private boolean mTranslateEnable = false;
    int ret = 0; // 函数调用返回值
    //服务器接口地址
    private String targetUrl = "http://v9wjgq.natappfree.cc/android/";

    private android.os.Message message;

    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 200) {
                texts = String.valueOf(msg.obj).trim();
                synthesizeAudio(texts);
            } else {
                texts = "网络 " + msg.what + " 错误: " + String.valueOf(msg.obj);
                synthesizeAudio(texts);
                Log.i(TAG, texts);
            }
        }
    };

    private void synthesizeAudio(String text) {
        receiveTextMsg(text);
        //设置语音合成的相关参数
        setTtsParam();
        String path = mTts.getParameter(SpeechConstant.TTS_AUDIO_PATH);
        //合成语音并进行播放
        int code = mTts.startSpeaking(text, mTtsListener);
        //只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
        // int code = mTts.synthesizeToUri(text, path, mTtsListener);
        if (code == ErrorCode.SUCCESS) {
            // completeAudioMessage(path);
            Log.i(TAG, "语音合成成功: ");
        } else {
            Log.i(TAG, "语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    private void postToDjango(String text) {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("text", text);//传递键值对参数
        Request request = new Request.Builder()//创建Request 对象。
                .url(targetUrl)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //response.body().string()只能调用一次
                String obj = response.body().string();
                Log.i(TAG, "onResponse: " + response);
                if (response.body() != null) {
                    Log.i(TAG, "response.body().string: " + obj);
                    message = handler.obtainMessage();
                    message.what = response.code();
                    message.obj = obj;
                    handler.sendMessage(message);
                }
            }
        });
    }

    void startAudio() {
        Log.i(TAG, "startAudio: ");
        //开始语音合成时停止其他正在播放的音频
        if (ivAudio != null) {
            if (audioSender.equals("left")) {
                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
            } else {
                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
            }
            ivAudio = null;
            MediaManager.reset();
        }
        setParam();
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.i(TAG, "听写失败,错误码" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        } else {
            Log.i(TAG, "请开始说话");
        }
    }

    void endAudio() {
        mIat.stopListening();
        Log.i(TAG, "endAudio: ");
    }

    void cancelAudio() {
        mIat.cancel();
        Log.i(TAG, "cancelAudio: ");
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        this.mTranslateEnable = mSharedPreferences.getBoolean(this.getString(R.string.pref_key_translate), false);
        if (mTranslateEnable) {
            Log.i(TAG, "translate enable");
            mIat.setParameter(SpeechConstant.ASR_SCH, "1");
            mIat.setParameter(SpeechConstant.ADD_CAP, "translate");
            mIat.setParameter(SpeechConstant.TRS_SRC, "its");
        }

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "en");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "cn");
            }
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "cn");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "en");
            }
        }
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "10000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "10000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/wav/" + System.currentTimeMillis() + ".wav");
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.i(TAG, "onBeginOfSpeech: ");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.i(TAG, "onError: 您没有说话");
            if (mTranslateEnable && error.getErrorCode() == 14002) {
                Log.i(TAG, "onError: mTranslateEnable && error.getErrorCode() == 14002");
            }
            Toast.makeText(ChatActivity.this, "您没有说话", Toast.LENGTH_SHORT).show();
            mBtnAudio.cancelRecord();
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.i(TAG, "结束说话:");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String resultString = getResult(results);
            if (isLast) {
                Log.i(TAG, "语音识别结束: isLast");
                Log.i(TAG, "音频保存路径: " + mIat.getParameter(SpeechConstant.ASR_AUDIO_PATH));
                endAudioMessage(mIat.getParameter(SpeechConstant.ASR_AUDIO_PATH), resultString);
                analyseSendMessage(resultString);

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void analyseSendMessage(String sendString) {
        if (sendString.contains("启动") || sendString.contains("打开")) {
            if (sendString.contains("QQ") || sendString.contains("qq")) {
                openApp("com.tencent.mobileqq", "QQ");
                synthesizeAudio("正在为您启动QQ");
            } else if (sendString.contains("微信")) {
                openApp("com.tencent.mm", "微信");
                synthesizeAudio("正在为您启动微信");
            } else {
                postToDjango(sendString);
            }
        } else if (sendString.contains("帅哥") ||
                (sendString.contains("好友") && (sendString.contains("QQ") || sendString.contains("qq")))) {
            try {
                //跳转到添加好友，如果qq号是好友了，直接聊天
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=1535691300";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                synthesizeAudio("正在为您跳转到QQ聊天界面");
            } catch (Exception e) {
                e.printStackTrace();
                synthesizeAudio("QQ启动失败,请检查是否已安装QQ");
            }
        } else {
            postToDjango(sendString);
        }
    }

    private void openApp(String packageName, String appName) {
        Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mIntent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        for (ResolveInfo res : resolveInfos) {
            String pkg = res.activityInfo.packageName;
            String cls = res.activityInfo.name;
            if (pkg.equals(packageName)) {
                ComponentName componentName = new ComponentName(pkg, cls);
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplication().startActivity(intent);
                return;
            }
        }
        synthesizeAudio(appName + "启动失败,请检查是否已安装该应用");
    }

    private String getResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        return resultBuffer.toString();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.i(TAG, "onInit: 初始化失败，错误码：" + code);
            }
        }
    };


    public void endAudioMessage(String path, String results) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);  //recordingFilePath（）为音频文件的路径
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //向上取整，最短语音时长为1s
        int duration = player.getDuration() / 1000 + 1;//获取音频的时间
        player.release();//记得释放资源
        Log.i(TAG, "音频时长: " + duration + "秒");

        File file = new File(path);
        if (file.exists()) {
            sendAudioMessage(path, duration);
            if (mSharedPreferences.getBoolean("show_audio_results", true))
                sendTextMsg("识别：" + results);
        }

    }

    public void completeAudioMessage(String path) {
        Log.i(TAG, "completeAudioMessage: " + path);
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);  //recordingFilePath（）为音频文件的路径
            player.prepare();
        } catch (IOException e) {
            Log.i(TAG, "completeAudioMessage: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.i(TAG, "completeAudioMessage: " + e.getMessage());
            e.printStackTrace();
        }
        //向上取整，最短语音时长为1s
        int duration = player.getDuration() / 1000 + 1;//获取音频的时间
        player.release();//记得释放资源
        Log.i(TAG, "合成音频时长: " + duration + "秒");

        File file = new File(path);
        if (file.exists()) {
            if (mSharedPreferences.getBoolean("show_audio_synthesize", true))
                receiveAudioMessage(path, duration);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }

        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0x0010);
                }

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        requestPermissions();
        // 初始化语音听写对象
        mIat = SpeechRecognizer.createRecognizer(ChatActivity.this, mInitListener);
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(ChatActivity.this, mTtsInitListener);
        initContent();
    }

    private ImageView ivAudio;

    protected void initContent() {
        ButterKnife.bind(this);
        Log.i(TAG, "initContent: ");
        mAdapter = new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Message message = (Message) adapter.getData().get(position);
                MsgType msgType = message.getMsgType();
                //判断是发送语音消息还是接收语音消息
                final String sender = message.getSenderId();
                Log.i(TAG, "onItemChildClick: " + adapter.getItemCount());
                if (msgType == MsgType.AUDIO) {
                    mTts.pauseSpeaking();
                    //用户点击语音消息
                    if (ivAudio != null) {
                        if (ivAudio == view.findViewById(R.id.ivAudio)) {
                            if (sender.equals("left")) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            }
                            ivAudio = null;
                            MediaManager.reset();
                            return;
                        } else {
                            if (audioSender.equals("left")) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            }
                            ivAudio = null;
                            MediaManager.reset();
                        }
                    }
                    audioSender = sender;
                    ivAudio = view.findViewById(R.id.ivAudio);
                    MediaManager.reset();
                    if (sender.equals("left")) {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                    } else {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                    }
                    AnimationDrawable drawable = (AnimationDrawable) ivAudio.getBackground();
                    drawable.start();
                    MediaManager.playSound(com.iflytek.activity.ChatActivity.this, ((AudioMsgBody) mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            LogUtil.d("开始播放结束");
                            if (sender.equals("left")) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            }
                            ivAudio = null;
                            MediaManager.release();
                        }
                    });

                } else if (adapter.getItemCount() - 1 == position && msgType == MsgType.TEXT
                        && mPercentForPlaying != 100 && sender.equals("left")) {
                    //用户点击文本消息，判断是否需要停止播放语音
                    mTts.pauseSpeaking();
                }
            }
        });

    }


    @Override
    public void onRefresh() {
        // //下拉刷新模拟获取历史消息
        // List<Message> mReceiveMsgList = new ArrayList<Message>();
        // //构建文本消息
        // Message mMessgaeText = getBaseReceiveMessage(MsgType.TEXT);
        // TextMsgBody mTextMsgBody = new TextMsgBody();
        // mTextMsgBody.setMessage("收到的消息");
        // mMessgaeText.setBody(mTextMsgBody);
        // mReceiveMsgList.add(mMessgaeText);
        // //构建图片消息
        // Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
        // ImageMsgBody mImageMsgBody = new ImageMsgBody();
        // mImageMsgBody.setThumbUrl("http://pic19.nipic.com/20120323/9248108_173720311160_2.jpg");
        // mMessgaeImage.setBody(mImageMsgBody);
        // mReceiveMsgList.add(mMessgaeImage);
        // //构建文件消息
        // Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
        // FileMsgBody mFileMsgBody = new FileMsgBody();
        // mFileMsgBody.setDisplayName("收到的文件");
        // mFileMsgBody.setSize(12);
        // mMessgaeFile.setBody(mFileMsgBody);
        // mReceiveMsgList.add(mMessgaeFile);
        // mAdapter.addData(0, mReceiveMsgList);
        mSwipeRefresh.setRefreshing(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initChatUi() {
        //mBtnAudio
        final ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio);
        // .bindEmojiData();
        //底部布局弹出,聊天列表上滑
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });
        //点击空白区域关闭键盘
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });
        // 录音结束回调
        mBtnAudio.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                Log.i(TAG, "录音结束回调: ");
                endAudio();
                File file = new File(audioPath);
                if (file.exists()) {
                    sendAudioMessage(audioPath, time);
                }
            }
        });
        //录音开始回调
        mBtnAudio.setOnStartRecordListener(new RecordButton.OnStartRecordListener() {
            @Override
            public void onStartRecord() {
                Log.i(TAG, "录音开始回调: ");
                startAudio();
            }
        });
        //录音取消回调
        mBtnAudio.setOnCancelRecordListener(new RecordButton.OnCancelRecordListener() {
            @Override
            public void onCancelRecord() {
                Log.i(TAG, "录音取消回调: ");
                cancelAudio();
            }
        });

    }

    @OnClick({R.id.btn_send, R.id.rlPhoto, R.id.rlVideo, R.id.rlLocation, R.id.rlFile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (!("").contentEquals(mEtContent.getText())) {
                    sendTextMsg(mEtContent.getText().toString());
                    analyseSendMessage(mEtContent.getText().toString());
                    mEtContent.setText("");
                }
                break;
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(ChatActivity.this, REQUEST_CODE_IMAGE);
                break;
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(ChatActivity.this, REQUEST_CODE_VEDIO);
                break;
            case R.id.rlFile:
                PictureFileUtil.openFile(ChatActivity.this, REQUEST_CODE_FILE);
                break;
            case R.id.rlLocation:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:" + filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("获取图片路径成功:" + media.getPath());
                        sendImageMessage(media);
                    }
                    break;
                case REQUEST_CODE_VEDIO:
                    // 视频选择结果回调
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
                        LogUtil.d("获取视频路径成功:" + media.getPath());
                        sendVedioMessage(media);
                    }
                    break;
            }
        }
    }


    //发送文本消息
    private void sendTextMsg(String hello) {
        final Message mMessgae = getBaseSendMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody = new TextMsgBody();
        mTextMsgBody.setMessage(hello.trim());
        mMessgae.setBody(mTextMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }

    //收到文本消息
    private void receiveTextMsg(String hello) {
        hello = hello.trim();
        final Message mMessgae = getBaseReceiveMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody = new TextMsgBody();
        mTextMsgBody.setMessage(hello.trim());
        mMessgae.setBody(mTextMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    //图片消息
    private void sendImageMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath());
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    //视频消息
    private void sendVedioMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.VIDEO);
        //生成缩略图路径
        String vedioPath = media.getPath();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(vedioPath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        String imgname = System.currentTimeMillis() + ".jpg";
        String urlpath = Environment.getExternalStorageDirectory() + "/" + imgname;
        File f = new File(urlpath);
        try {
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LogUtil.d("视频缩略图路径获取失败：" + e.toString());
            e.printStackTrace();
        }
        VideoMsgBody mImageMsgBody = new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //文件消息
    private void sendFileMessage(String from, String to, final String path) {
        final Message mMessgae = getBaseSendMessage(MsgType.FILE);
        FileMsgBody mFileMsgBody = new FileMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);

    }

    //发送的语音消息
    private void sendAudioMessage(final String path, int time) {
        final Message mMessgae = getBaseSendMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody = new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }

    //收到的语音消息
    private void receiveAudioMessage(final String path, int time) {
        final Message mMessgae = getBaseReceiveMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody = new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    private Message getBaseSendMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mTargetId);
        mMessgae.setTargetId(mSenderId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) {
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        //模拟1秒后发送成功
        new Handler().postDelayed(new Runnable() {
            public void run() {
                int position = 0;
                mMessgae.setSentStatus(MsgSendStatus.SENT);
                //更新单个子条目
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    Message mAdapterMessage = mAdapter.getData().get(i);
                    if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())) {
                        position = i;
                    }
                }
                mAdapter.notifyItemChanged(position);
            }
        }, 500);

    }


}
