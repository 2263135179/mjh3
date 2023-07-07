package com.zamcenter.app.musicplayer.ServiceImpl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.zamcenter.app.musicplayer.Service.MusicChangedListener;
import com.zamcenter.app.musicplayer.Service.MusicPlayingChangedListener;
import com.zamcenter.app.musicplayer.Service.MusicService;
import com.zamcenter.app.musicplayer.Service.SongSheetService;
import com.zamcenter.app.musicplayer.entity.SongBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MusicServiceImpl implements MusicService {
    private static final String TAG = "MusicServiceImpl";
    private static MusicServiceImpl musicServiceImpl = null;

    private MediaPlayer mediaPlayer;
    private String[] musicNames, randomNames;
    private int currentPosition, currentIndex, order = PLAY_ORDER;
    private AssetManager assetManager;
    private String currentMusicName;
    private MusicChangedListener musicChangedListener;
    private MusicPlayingChangedListener musicPlayingChangedListener;

    private MusicServiceImpl(Context context) {
        mediaPlayer = new MediaPlayer();
        assetManager = context.getAssets();
        try {
/*            String result = "";
            File 123 = new File(getExternalCacheDir(),);
            File[] files = new File(getExternalCacheDir).listFiles();
            for (File file : files) {
                if (file.getName().indexOf(".mp3") >= 0) {

                    result += file.getPath() + "\n";
                }
            }
            if (result.equals("")){
                Log.d(TAG, "music无文件！");
            }*/
            musicNames = assetManager.list("music");    //获取assets/music下所有文件
            if (musicNames != null) {
                randomNames = new String[musicNames.length];
                SongSheetService songSheetService = new SongSheetServiceImpl();
                //遍历musicNames数组
                for (int i = 0; i < musicNames.length; i++) {
                    randomNames[i] = musicNames[i];
                    //运行一次即可，用于将本地音乐存入SongBean
                    SongBean songBean = new SongBean(musicNames[i], songSheetService.findAll().get(0).getId());
                    if (songBean.save()) {
                        Log.d(TAG, "MusicServiceImpl: save " + musicNames[i] + "successfully!");
                    } else {
                        Log.d(TAG, "MusicServiceImpl: default!");
                    }
                }
                currentMusicName = musicNames[0];
                loadMusic(currentMusicName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MusicService getInstance(Context context) {
        if (musicServiceImpl == null) {
            synchronized (MusicServiceImpl.class) {
                if (musicServiceImpl == null) {
                    musicServiceImpl = new MusicServiceImpl(context);//创建一个新的MusicServiceImpl对象，并将其赋值给musicServiceImpl变量
                }
            }
        }
        return musicServiceImpl;
    }



    @Override
    public void loadMusic(String musicName){
        currentMusicName = musicName;//当前音乐名称（currentMusicName）设置为传入的音乐名称。
        try {
            //重置
            mediaPlayer.reset();
            AssetFileDescriptor afd = assetManager.openFd("music/" + currentMusicName);
            //获取指定音乐文件的AssetFileDescriptor对象（afd），该对象用于获取音乐文件的文件描述符、起始位置和长度信息。
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            //设置音乐文件的数据源为afd对象所描述的音乐文件
            mediaPlayer.prepare();//准备音乐文件进行播放
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void play(String musicName) {
        if (musicName == null) {
            if (mediaPlayer.isPlaying()) {
                onPause();//暂停播放
            } else {
                if (currentPosition > 0) {
                    onResume();//表示之前有暂停过，调用onResume方法恢复播放
                } else {
                    start();//表示还未开始播放，调用start方法开始播放。
                }
            }
        } else if (!currentMusicName.equals(musicName)){
            loadMusic(musicName);//调用loadMusic方法加载新的音乐
            this.musicChangedListener.refresh();//触发musicChangedListener刷新操作
            start();//开始播放该音乐
        } else {
            if (!mediaPlayer.isPlaying()) {//判断mediaPlayer是否正在播放
                start();//开始播放该音乐
            }
        }
    }

    private void start() {
        mediaPlayer.start();//开始播放音乐
        this.musicPlayingChangedListener.afterChanged();//通知播放状态已经改变
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });
        //监听音乐播放完成事件
    }

    @Override
    public void onPause() {//暂停音乐的播放
        if (mediaPlayer.isPlaying()) {
            currentPosition = mediaPlayer.getCurrentPosition();
            //获取当前播放位置,将其保存到currentPosition变量中。
            mediaPlayer.pause();//暂停音乐的播放
            this.musicPlayingChangedListener.afterChanged();//通知播放状态已经改变
        }
    }

    @Override
    //将音乐的播放位置跳转到指定的值。如果音乐当前没有在播放，则会先调用play方法开始播放音乐，然后再进行跳转操作。
    public void seekTo(int progress) {//跳转到指定的播放位置
        if (!mediaPlayer.isPlaying()){
            play(null);//开始播放音乐,play方法传入null作为参数，表示继续播放当前的音乐。
        }
        mediaPlayer.seekTo(progress);//将播放位置跳转到指定的progress位置
    }

    @Override
    public void onResume() {//恢复音乐的播放
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();//开始音乐的播放
            this.musicPlayingChangedListener.afterChanged();//通知播放状态已经改变
            mediaPlayer.seekTo(currentPosition);//将播放位置跳转到之前暂停的位置，即currentPosition。
            currentPosition = 0;//表示播放位置已回到起始位置。
        }
    }

    @Override
    public void setPlayOrder(int i) {
        if (i == PLAY_ORDER) {
            order = PLAY_ORDER;//播放顺序为顺序播放
            currentIndex = Arrays.binarySearch(musicNames, currentMusicName);
            //二分法查找当前播放音乐的索引
            Log.d(TAG, "setPlayOrder: currentIndex order " + currentIndex);
            //通过Log输出当前的播放顺序模式和currentIndex的值
        } else {
            order = PLAY_RANDOM;//播放顺序为随机播放
            shuffleCard(musicNames);//对musicNames数组进行打乱顺序操作。
            currentIndex = search(randomNames, currentMusicName);    //二分法查找当前播放音乐的索引
            Log.d(TAG, "setPlayOrder: currentIndex random " + currentIndex);
        }
    }

    @Override
    public void next() {//播放下一首音乐
        if (order == PLAY_ORDER) {  //顺序播放
            if (currentIndex < musicNames.length - 1) {
                play(musicNames[++currentIndex]);//调用play方法播放下一首音乐
            } else {//表示已经播放到了最后一首音乐
                currentIndex = 0;//将currentIndex重置为0
                play(musicNames[currentIndex]);//播放音乐列表中的第一首音乐
            }
        } else {    //随机播放
            if (currentIndex < randomNames.length - 1) {
                play(randomNames[++currentIndex]);
            } else {
                currentIndex = 0;
                play(randomNames[currentIndex]);//randomNames数组来获取随机播放的音乐
            }
        }
        this.musicChangedListener.refresh();//通知音乐已经发生了改变
    }

    @Override
    public void last() {//播放上一首音乐
        if (order == PLAY_ORDER) {  //顺序播放
            if (currentIndex > 0) {
                play(musicNames[--currentIndex]);
            } else {
                currentIndex = musicNames.length - 1;
                play(musicNames[currentIndex]);
            }
        } else {    //随机播放
            if (currentIndex > 0) {
                play(randomNames[--currentIndex]);
            } else {
                currentIndex = randomNames.length - 1;
                play(randomNames[currentIndex]);
            }
        }
        this.musicChangedListener.refresh();
    }

    @Override
    public int getCurrentProgress() {//获取当前音乐的播放进度
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();//获取当前音乐的播放位置（进度），并返回该值。
        }
        return 0;//当前进度为0
    }

    @Override
    public void onDestroy() {
        assetManager.close();//释放 assetManager 对象所持有的资源，如打开的文件或流。
        mediaPlayer.release();//释放 mediaPlayer 对象所占用的系统资源，如音频解码器等。
    }

    /**
     * 洗牌算法
     * @param names 顺序播放的musicNames
     */
    //将数组 names 中的元素随机打乱顺序
    private void shuffleCard(String[] names) {
        int len = names.length;
        Random r = new Random();//生成随机数
        for (int i = 0; i < len; i++) {
            int index = r.nextInt(len);
            //通过 r.nextInt(len) 生成一个 0 到 len-1 之间的随机数，作为交换的目标索引。
            String temp = randomNames[i];
            randomNames[i] = randomNames[index];
            randomNames[index] = temp;
        }
        for (int i = 0; i < len; i++) {
            Log.d(TAG, "shuffleCard: random " + randomNames[i]);
        }
    }

    @Override
    //获取当前的播放顺序模式
    public int getPlayOrder() {
        return order;
    }

    @Override
    //获取当前播放音乐的信息
    public String getCurrentMusicInfo() {
        String str = currentMusicName.substring(0, currentMusicName.length()-4);
        //截取音乐名称的一部分内容
        String[] info = str.split(" - ");
        //根据截取的音乐名称内容，通过调用 split 方法将其分割为一个字符串数组，使用 " - " 作为分割符。
        return info[1] + "\n" + info[0];
    }

    @Override
    //获取当前播放音乐的总时长
    public int getDuration() {
        return mediaPlayer.getDuration();//返回获取到的总时长值
    }

    @Override
    public String[] getMusicNames() {//返回音乐的名称数组
        return musicNames;//返回musicNames数组，该数组存储着音乐的名称信息
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    //设置音乐变化的监听器
    public void setMusicChangedListener(MusicChangedListener musicChangedListener) {
        this.musicChangedListener = musicChangedListener;
    }

    private int search(String[] randomNames, String a) {
        for (int i = 0; i < randomNames.length; i ++) {
            if (a.equals(randomNames[i])) {
                return i;
            }
        }
        return 0;
    }

    @Override
    //设置音乐播放状态改变的监听器
    public void setMusicPlayingChangedListener(MusicPlayingChangedListener musicPlayingChangedListener) {
        this.musicPlayingChangedListener = musicPlayingChangedListener;
    }

}
