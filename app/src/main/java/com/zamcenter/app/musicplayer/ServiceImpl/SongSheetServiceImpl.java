package com.zamcenter.app.musicplayer.ServiceImpl;

import com.zamcenter.app.musicplayer.Service.SongSheetService;
import com.zamcenter.app.musicplayer.entity.SongBean;
import com.zamcenter.app.musicplayer.entity.SongSheetBean;

import org.litepal.LitePal;

import java.util.List;

public class SongSheetServiceImpl implements SongSheetService {
    private static final String TAG = "SongSheetServiceImpl";
    @Override
    public boolean add(String name, String imgAddress) {
        SongSheetBean songSheet = new SongSheetBean(name, imgAddress);
        return songSheet.save();
    }

    @Override
    public int delete(SongSheetBean songSheet) {
        return songSheet.delete();
    }

    @Override
    public List<SongSheetBean> findAll() {
        //如果歌单数为0，创建新歌单 将assert中的music存入SongBean
        if(LitePal.findAll(SongSheetBean.class).size() == 0){
            SongSheetBean songSheet = new SongSheetBean("所有歌曲", null);
            songSheet.save();
            return LitePal.findAll(SongSheetBean.class);
        }else{
            return LitePal.findAll(SongSheetBean.class);
        }
    }

    @Override
    public List<SongBean> findSongBeanBySongSheetId(int songSheetId) {
        return LitePal.where("songSheetId = ?", songSheetId+"").find(SongBean.class);
    }

    @Override
    //向歌单中添加歌曲
    public boolean addSongBean(SongBean songBean, SongSheetBean songSheetBean) {
        songBean.setSongSheetId(songSheetBean.getId());
        songBean.save();//歌曲对象保存到数据库中
        return songBean.getSongSheetId() == songSheetBean.getId();
        //返回songBean的歌单ID与songSheetBean的ID进行比较的结果。如果相等，则说明歌曲成功添加到歌单；否则，说明添加失败。
    }
}
