package com.zamcenter.app.musicplayer.adater;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.Service.SongSheetService;
import com.zamcenter.app.musicplayer.ServiceImpl.SongSheetServiceImpl;
import com.zamcenter.app.musicplayer.dto.SongDto;
import com.zamcenter.app.musicplayer.entity.SongBean;
import com.zamcenter.app.musicplayer.entity.SongSheetBean;

import org.litepal.LitePal;

public class SongAdapter extends BaseAdapter {
    private static final String TAG = "SongAdapter";
    private SongDto songDto;
    private Context mContext;

    public SongAdapter(Context context, SongDto songDto){
        this.songDto = songDto;
        this.mContext = context;
    }

    public SongAdapter(Context context){
        this.songDto = new SongDto(LitePal.findFirst(SongSheetBean.class),LitePal.findAll(SongBean.class));
        this.mContext = context;
    }

    @Override
    public int getCount() {//重写了getCount方法，返回歌曲列表中的元素个数
        return songDto.getSongBeanList().size();//通过songDto.getSongBeanList().size()获取歌曲列表songBeanList的大小。
    }

    @Override
    public Object getItem(int i) {//重写了getItem方法，返回指定位置的歌曲对象。
        return songDto.getSongBeanList().get(i);//通过songDto.getSongBeanList().get(i)获取歌曲列表songBeanList中指定位置i的歌曲对象。
    }

    @Override
    public long getItemId(int i) {//重写了getItemId方法，返回指定位置的歌曲对象的ID。
        return i;//直接将位置i作为ID返回，可以根据需要进行更改
    }

    @Override
    //定义了一个公共的方法getView，该方法用于获取每个Item的视图。i表示Item的位置，view表示Item的视图，viewGroup表示Item的父容器
    public View getView(int i, View view, ViewGroup viewGroup) {
        SongBean songBean = (SongBean) getItem(i);//获取指定位置i处的SongBean对象songBean。
        View contentView;//存储Item的布局视图。
        ViewHolder viewHolder;//存储Item中的控件
        if (view == null) {
            contentView = LayoutInflater.from(mContext).inflate(R.layout.item_song, null);
            //通过LayoutInflater从上下文mContext中加载指定布局资源R.layout.item_song，得到contentView。
            viewHolder = new ViewHolder();
            //创建一个新的ViewHolder对象，将该对象中的textView实例化为布局中的控件R.id.item_song_info。
            viewHolder.textView = contentView.findViewById(R.id.item_song_info);
            viewHolder.menu = contentView.findViewById(R.id.item_song_menu);
            //将该对象中的menu实例化为布局中的控件R.id.item_song_menu。
            contentView.setTag(viewHolder);//将ViewHolder对象存储在contentView的Tag属性中，以便在下次重用时能够找到对应的控件。
        } else {//如果view不为空，表示可以重用旧视图
            contentView = view;//将view复制给contentView
            viewHolder = (ViewHolder) contentView.getTag();
            //从contentView的Tag属性中获取之前存储的ViewHolder对象。
        }
        viewHolder.textView.setText(songBean.getName());//将songBean的名称设置为viewHolder中的textView的文本内容
        viewHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        showPopMenu(view);
                    }
                });//为viewHolder中的menu设置点击事件监听器，点击时会执行showPopMenu方法来显示弹出菜单。
            }
        });
        viewHolder.menu.setTag(songBean);//为viewHolder中的menu设置Tag，用于标记该菜单所对应的歌曲对象。
        return contentView;//返回contentView作为Item的视图。
    }

    private class ViewHolder {//保存Item视图中的控件。
        private TextView textView;//显示歌曲信息
        private ImageView menu;//菜单按钮
    }

    private void showPopMenu(final View view) {
        final View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_addtosheet, null);
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        final SongBean songBean = (SongBean) view.getTag();
        final SongAdapter songAdapter = this;
        popupMenu.getMenuInflater().inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_song_download:

                        Toast.makeText(mContext, "暂未实现！", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

}
