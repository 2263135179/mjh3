package com.zamcenter.app.musicplayer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.ui.MainContentFragment;
import com.zamcenter.app.musicplayer.ui.MainFragment;
import com.zamcenter.app.musicplayer.ui.MusicInfoFragment;
import com.zamcenter.app.musicplayer.ui.SongContentFragment;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化数据库
        LitePal.initialize(this);
        //隐藏最上方工具栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //将碎片和activity绑定
        initMainFragment(savedInstanceState);
        //避免点击editText时，软键盘遮挡输入框
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    //初始化主要的Fragment组件
    private void initMainFragment(Bundle bundle) {
        if (bundle == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            //获取FragmentManager对象，用于管理Fragment的添加、替换和移除等操作。
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //开始一个Fragment事务，通过调用FragmentManager的beginTransaction方法获取FragmentTransaction对象
            fragmentTransaction//将实例添加到容器视图R.id.fragment_host中。
                    .add(R.id.fragment_host, MainFragment.getInstance(), MainFragment.class.getName())
                    .add(R.id.fragment_host, MusicInfoFragment.getInstance(), MusicInfoFragment.class.getName())
                    .add(R.id.main_content, MainContentFragment.getInstance(), MainContentFragment.class.getName())
                    .hide(MusicInfoFragment.getInstance())
                    .commit();
        }
    }

    //初始化footer
    public void enterMusicInfoFragment() {//进入音乐信息Fragment。
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .hide(MainFragment.getInstance())//隐藏MainFragment实例。
                .show(MusicInfoFragment.getInstance())//显示MusicInfoFragment实例。
                .addToBackStack(null)//将事务添加到返回栈中，以支持返回操作。
                .commit();
    }

    //初始化歌单列表碎片,用于进入歌曲内容Fragment
    public void enterSongContentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //获取FragmentManager对象，用于管理Fragment的添加、替换和移除等操作。
        Fragment fragment = fragmentManager.findFragmentByTag(SongContentFragment.class.getName());
        //根据标签SongContentFragment.class.getName()查找已存在的SongContentFragment。
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //开始一个Fragment事务，通过调用FragmentManager的beginTransaction方法获取FragmentTransaction对象。
        fragmentTransaction
                .hide(MainContentFragment.getInstance());
        //调用fragmentTransaction的hide方法，隐藏MainContentFragment实例。
        if (fragment == null) {
            fragmentTransaction
                    .add(R.id.main_content, SongContentFragment.getInstance(), SongContentFragment.class.getName());
            //调用fragmentTransaction的add方法添加SongContentFragment实例到容器视图R.id.main_content中，使用SongContentFragment.class.getName()作为标签。
        } else {
            fragmentTransaction
                    .show(fragment);//调用fragmentTransaction的show方法显示SongContentFragment实例。
        }
        fragmentTransaction
                .addToBackStack(null)//将事务添加到返回栈中，以支持返回操作。
                .commit();//提交事务
    }

    //设置返回键方法
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //获取FragmentManager对象，用于管理Fragment的添加、替换和移除等操作
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.d(TAG, "onBackPressed: " + fragmentManager.getBackStackEntryCount());
            //使用Log.d方法打印日志，输出FragmentManager返回栈中Fragment事务的数量。
            fragmentManager.popBackStack();
            //将栈顶的事务出栈，并根据事务的操作来恢复或移除Fragment。
        }
    }

}
