package com.zamcenter.app.musicplayer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.Service.MusicService;
import com.zamcenter.app.musicplayer.ServiceImpl.MusicServiceImpl;
import com.zamcenter.app.musicplayer.adater.SongAdapter;
import com.zamcenter.app.musicplayer.dto.SongDto;
import com.zamcenter.app.musicplayer.entity.SongBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/*歌单主界面*/

public class SongContentFragment extends Fragment {
    private static final String TAG = "SongContentFragment";//声明了一个私有的静态常量字符串TAG，用于标识日志输出时的标签
    private static SongContentFragment songContentFragment;//保存SongContentFragment的实例

    private View view;//存储Fragment的布局视图。
    private MusicService musicService;//播放音乐的服务。
    private SongDto songDto;//存储歌曲的数据。

    //获取SongContentFragment的单例实例。
    public static SongContentFragment getInstance() {
        if (songContentFragment == null) {
            synchronized (SongContentFragment.class) {
                if (songContentFragment == null) {
                    songContentFragment = new SongContentFragment();
                    //创建一个新的SongContentFragment对象，并赋值给songContentFragment。
                }
            }
        }
        return songContentFragment;
    }

    @Nullable
    @Override
    //重写了onCreateView方法，用于创建Fragment的视图。
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        //通过调用EventBus.getDefault().register(this)方法，注册当前的Fragment为EventBus的订阅者，以接收事件消息。
        view = inflater.inflate(R.layout.fragment_main_song, container, false);
        //使用inflater对象将指定的布局资源R.layout.fragment_main_song转换为View对象，并赋值给view。
        initView();//初始化视图界面和相关操作
        return view;//返回view作为Fragment的视图
    }

    @Override
    //重写了onDestroyView方法，在销毁Fragment的视图时执行一些清理操作
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        //通过调用EventBus.getDefault().unregister(this)方法，取消注册当前Fragment作为EventBus的订阅者，以停止接收事件消息。
        super.onDestroyView();//执行父类的销毁视图操作。
    }

    @Override
    //当Fragment的可见状态发生变化时调用,boolean类型的hidden表示Fragment当前的可见状态，true表示隐藏，false表示显示
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);//调用super.onHiddenChanged(hidden)方法，执行父类的onHiddenChanged方法
        if (!hidden) {
            initView();//用于初始化视图界面和相关操作。
        }
    }

    //初始化视图界面和相关操作。
    private void initView() {
        musicService = MusicServiceImpl.getInstance(getContext());
        //使用MusicServiceImpl类的getInstance方法获取MusicService的实例，音乐播放的服务。
        ListView listView = view.findViewById(R.id.song_list);
        //从当前视图view中获取布局资源R.id.song_list对应的ListView，并将其赋值给变量listView。
        TextView textView_title = view.findViewById(R.id.song_title);
        //从当前视图view中获取布局资源R.id.song_title对应的TextView，并将其赋值给变量textView_title。
        textView_title.setText(songDto.getSongSheetBean().getName());
        //从当前视图view中获取布局资源R.id.song_title对应的TextView，并将其赋值给变量textView_title。
        if (songDto.isLocal()) {//如果songDto的isLocal方法返回true，即歌单是本地歌单：
            listView.setAdapter(new SongAdapter(getContext()));
            //通过设置适配器SongAdapter创建ListView的适配器，适配器构造函数中传入了一个Context参数。
        } else {
            listView.setAdapter(new SongAdapter(getContext(), songDto));
            //通过设置适配器SongAdapter创建ListView的适配器，适配器构造函数中传入了一个Context和songDto参数。
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //设置listView的点击事件监听器，当点击ListView中的某个Item时，执行onItemClick方法。
                SongBean songBean = (SongBean) adapterView.getItemAtPosition(i);
                musicService.play(songBean.getName());
                //调用musicService的play方法，传入歌曲的名称，用于播放对应的歌曲。
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    //使用注解@Subscribe标注了一个公共方法onGetMessage，用于接收发送的事件消息。参数threadMode设置为ThreadMode.ASYNC，表示该方法在异步线程中执行。sticky设置为true，表示该方法接收粘性事件，即在注册之前发送的事件也会被接收到。
    public void onGetMessage(SongDto songDto) {//接收传递的歌曲Dto对象
        if (songDto != null) {
            this.songDto = songDto;//将传递的歌曲Dto对象赋值给成员变量this.songDto。
        }
    }
}
