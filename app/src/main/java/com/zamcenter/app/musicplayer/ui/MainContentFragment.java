package com.zamcenter.app.musicplayer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zamcenter.app.musicplayer.MainActivity;
import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.Service.SongSheetService;
import com.zamcenter.app.musicplayer.ServiceImpl.SongSheetServiceImpl;
import com.zamcenter.app.musicplayer.adater.SongSheetAdapter;
import com.zamcenter.app.musicplayer.dto.SongDto;
import com.zamcenter.app.musicplayer.entity.SongBean;
import com.zamcenter.app.musicplayer.entity.SongSheetBean;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/*歌单列表*/

public class MainContentFragment extends Fragment {
    private static final String TAG = "MainContentFragment";

    private View view;//存储Fragment的布局视图。
    private static MainContentFragment mainContentFragment;//保存MainContentFragment的单例实例
    private SongSheetService songSheetService;//用于处理歌单的相关操作

    public static MainContentFragment getInstance() {
        if (mainContentFragment == null) {
            synchronized (MainContentFragment.class) {
                if (mainContentFragment == null) {
                    mainContentFragment = new MainContentFragment();
                }
            }
        }
        return mainContentFragment;//获取MainContentFragment的单例实例
    }

    @Nullable
    @Override
    //创建Fragment的视图
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_content, container, false);
        songSheetService = new SongSheetServiceImpl();//用于处理歌单的相关操作
        initView();//初始化视图界面和相关操作。
        return view;//返回view作为Fragment的视图
    }

    @Override
    //处理Fragment的可见状态变化的事件。hidden表示Fragment当前的可见状态，true表示隐藏，false表示显示。
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Log.d(TAG, "onHiddenChanged: show");
            initView();//调用initView方法，初始化视图和相关操作，进行页面更新。
        }
    }

    private void initView() {
        //查歌单
        ListView listView = view.findViewById(R.id.main_listView_songSheet);
        //获取布局中的ListView控件，将其赋值给变量listView。
        final List<SongSheetBean> data = songSheetService.findAll();
        //调用songSheetService的findAll方法，查询所有的歌单，并将结果保存在List集合data中。
        final SongSheetAdapter songSheetAdapter = new SongSheetAdapter(getContext(), data);
        //创建一个SongSheetAdapter适配器，传入上下文和歌单数据data
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //创建一个AlertDialog.Builder对象builder，用于构建对话框。
        final View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_sheet, null);
        //通过LayoutInflater加载布局资源R.layout.dialog_create_sheet，得到view1作为对话框的视图
        listView.setAdapter(songSheetAdapter);//将songSheetAdapter设置为listView的适配器。
        //为listView设置点击事件监听器，当点击列表项时触发
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity mainActivity = (MainActivity) getActivity();
                //获取MainActivity实例将其赋值给变量mainActivity。
                if (mainActivity != null) {
                    SongSheetBean songSheetBean = (SongSheetBean) adapterView.getItemAtPosition(i);
                    //取点击位置i处的歌单对象songSheetBean。
                    List<SongBean> songBeanList = songSheetService.findSongBeanBySongSheetId(songSheetBean.getId());
                    //根据歌单ID获取对应的歌曲列表songBeanList
                    if (i!=0) {
                        EventBus.getDefault().postSticky(new SongDto(songSheetBean, songBeanList));
                        //发送粘性事件SongDto给EventBus，默认isLocal属性为false。
                    } else {
                        EventBus.getDefault().postSticky(new SongDto(songSheetBean, songBeanList, true));
                    //发送粘性事件SongDto给EventBus，设置isLocal属性为true，表示本地歌单。
                    }
                    mainActivity.enterSongContentFragment();//进入歌曲内容Fragment页面。
                }
            }
        });

    }

}
