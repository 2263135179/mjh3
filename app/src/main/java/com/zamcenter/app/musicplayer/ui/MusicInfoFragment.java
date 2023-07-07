package com.zamcenter.app.musicplayer.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.Service.MusicChangedListener;
import com.zamcenter.app.musicplayer.Service.MusicPlayingChangedListener;
import com.zamcenter.app.musicplayer.Service.MusicService;
import com.zamcenter.app.musicplayer.ServiceImpl.MusicServiceImpl;

import java.lang.ref.WeakReference;
/*
* 歌曲播放界面
* */
public class MusicInfoFragment extends Fragment {
    private static final String TAG = "MusicInfoFragment";
    private static final int REFRESH_SEEKBAR_PROGRESS = 1;
    private static final int REFRESH_SEEKBAR_MAX = 2;
    private static final int REFRESH_HEADER = 3;
    private static final int REFRESH_PLAY = 4;
    private static MusicInfoFragment musicInfoFragment = null;

    private MusicService musicService;
    private View view;
    private SeekBarHandler seekBarHandler;
    private TextView textView_title;
    private SeekBar seekBar;

    public static MusicInfoFragment getInstance() {
        if (musicInfoFragment == null) {
            synchronized (MusicInfoFragment.class) {
                if (musicInfoFragment == null) {
                    musicInfoFragment = new MusicInfoFragment();
                }
            }
        }
        return musicInfoFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        seekBarHandler = new SeekBarHandler(this);
        view = inflater.inflate(R.layout.fragment_musicinfo, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView() {
        musicService = MusicServiceImpl.getInstance(getContext());
        textView_title = view.findViewById(R.id.info_head_title);
        seekBar = view.findViewById(R.id.info_seekBar);
        ImageView imageView_last = view.findViewById(R.id.info_last);
        final ImageView imageView_play = view.findViewById(R.id.info_play);
        ImageView imageView_next = view.findViewById(R.id.info_next);

        musicService.setMusicChangedListener(new MusicChangedListener() {
            @Override
            public void refresh() {
                refreshHeader();
            }
        });
        musicService.setMusicPlayingChangedListener(new MusicPlayingChangedListener() {
            @Override
            public void afterChanged() {
                refreshImgPlay();
                if (musicService.isPlaying()) {
                    send();
                } else {
                    seekBarHandler.removeCallbacksAndMessages(null);
                }
            }
        });

        textView_title.setText(musicService.getCurrentMusicInfo());


        imageView_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.last();
            }
        });

        imageView_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.next();
            }
        });

        refreshImgPlay();
        if (musicService.isPlaying()) {
            send();
        }
        imageView_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService.isPlaying()) {
                    musicService.onPause();
                } else {
                    musicService.play(null);
                }
                refreshImgPlay();
            }
        });
        seekBar.setMax(musicService.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress());
            }
        });

        ImageView imageView_back = view.findViewById(R.id.info_head_back);
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(TAG, "onHiddenChanged: ");
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initView();
        }
    }

    private void refreshImgPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                seekBarHandler.sendEmptyMessage(REFRESH_PLAY);
            }
        }).start();
    }

    private void refreshHeader() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                seekBarHandler.sendEmptyMessage(REFRESH_HEADER);
            }
        }).start();
    }

    /**
     * 新建线程，通过handler定时刷新SeekBar的ui
     */
    private void send() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                seekBarHandler.sendEmptyMessage(REFRESH_SEEKBAR_MAX);
            }
        }).start();
    }

    /**
     * 内部类，避免Handler发生内存泄漏
     * 由handleMessage处理
     */
    private static class SeekBarHandler extends Handler {
        private static final String TAG = "SeekBarHandler";
        WeakReference<MusicInfoFragment> musicInfoFragment;
        private SeekBarHandler(MusicInfoFragment musicInfoFragment) {
            this.musicInfoFragment = new WeakReference<>(musicInfoFragment);
        }

        @Override
        //重写了handleMessage方法，用于处理接收到的消息。对象msg包含了发送的消息的信息。
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);//调用super.handleMessage(msg)方法，执行父类的handleMessage方法，默认为空。
            MusicInfoFragment activity = musicInfoFragment.get();
            //获取弱引用musicInfoFragment对象的实例activity，使用musicInfoFragment.get()方法获取其引用
            switch (msg.what) {
                case REFRESH_HEADER:
                    activity.textView_title.setText(activity.musicService.getCurrentMusicInfo());
                    //设置activity中的textView_title的文本内容为当前正在播放的音乐信息，通过调用musicService的getCurrentMusicInfo方法。
                    break;
                case REFRESH_PLAY:
                    FragmentActivity fragmentActivity = activity.getActivity();
                    //获取activity所关联的FragmentActivity对象fragmentActivity，通过activity的getActivity方法获取。
                    if (fragmentActivity != null) {
                        ImageView imageView = fragmentActivity.findViewById(R.id.info_play);
                        //获取fragmentActivity中的info_play的ImageView
                        //根据musicService的isPlaying方法的返回值，设置图片资源为播放按钮或暂停按钮
                        if (activity.musicService.isPlaying()) {
                            imageView.setImageResource(R.drawable.ic_pause_black);
                        } else {
                            imageView.setImageResource(R.drawable.ic_play_black);
                        }
                    }
                    break;
                case REFRESH_SEEKBAR_MAX:
                    activity.seekBar.setMax(activity.musicService.getDuration());
                    //设置activity中的seekBar的最大值为musicService的音乐时长，通过调用getDuration方法
                case REFRESH_SEEKBAR_PROGRESS:
                    if (activity.seekBar.getMax() != activity.musicService.getDuration()) {
                        activity.seekBarHandler.removeCallbacksAndMessages(null);//移除seekBarHandler中之前的延迟消息
                        activity.send();//调用activity的send方法，用于发送消息延迟更新seekBar的进度。
                    } else {
                        activity.seekBar.setProgress(activity.musicService.getCurrentProgress());
                        //设置seekBar的进度为musicService的当前进度
                        sendEmptyMessageDelayed(REFRESH_SEEKBAR_PROGRESS, 1000);
                        //延迟一定时间发送下一次更新进度的消息。
                    }
                    break;
            }
        }
    }

}
