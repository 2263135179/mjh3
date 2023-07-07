package com.zamcenter.app.musicplayer.adater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zamcenter.app.musicplayer.R;
import com.zamcenter.app.musicplayer.entity.SongSheetBean;

import org.litepal.LitePal;

import java.util.List;

public class MenuSheetAdapter extends BaseAdapter {
    private static final String TAG = "MenuSheetAdapter";
    private List<SongSheetBean> songSheetBeanList;
    private Context mContext;
    public MenuSheetAdapter(Context context) {
        songSheetBeanList = LitePal.where("id != 1").find(SongSheetBean.class);
        mContext = context;
    }
    @Override
    public int getCount() {
        return songSheetBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return songSheetBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {//获取每个Item的视图,i表示Item的位置，view表示Item的视图，viewGroup表示Item的父容器。
        SongSheetBean songSheetBean = (SongSheetBean) getItem(i);//获取指定位置i处的SongSheetBean对象songSheetBean。
        String name = songSheetBean.getName();//从songSheetBean中获取名称name
        ViewHolder viewHolder;//保存视图中的各个控件,存储Item中的控件
        View contentView;//存储Item的布局视图
        if (view == null) {
            contentView = LayoutInflater.from(mContext).inflate(R.layout.item_menu_list, null);
            //通过LayoutInflater从上下文mContext中加载指定布局资源R.layout.item_menu_list，得到contentView。
            viewHolder = new ViewHolder();
            //创建一个新的ViewHolder对象，将该对象中的textView实例化为布局中的控件R.id.item_dialog_name。
            viewHolder.textView = contentView.findViewById(R.id.item_dialog_name);
            //将ViewHolder对象存储在contentView的Tag属性中，以便在下次重用时能够找到对应的控件
            contentView.setTag(viewHolder);
        } else {
            contentView = view;//将view复制给contentView
            viewHolder = (ViewHolder) contentView.getTag();
            //从contentView的Tag属性中获取之前存储的ViewHolder对象。
        }
        viewHolder.textView.setText(name);//将name设置为viewHolder中的textView的文本内容
        return contentView;//返回contentView作为Item的视图
    }

//定义了一个私有的内部类ViewHolder，用于保存Item视图中的控件。
    private class ViewHolder {
        TextView textView;//ViewHolder类中只包含一个TextView类型的成员变量textView。
    }
}

//通过定义ViewHolder类，可以方便地在getView方法中存储和获取Item视图中的控件，避免了每次获取控件时都需要进行findViewById操作的重复工作。
