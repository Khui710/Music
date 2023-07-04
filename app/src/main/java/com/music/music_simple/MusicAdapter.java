package com.music.music_simple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.music.music_simple.databinding.ItemMusicBinding;

import java.util.ArrayList;
import java.util.List;
//列表的Adapter
public class MusicAdapter extends BaseAdapter {
    private List<Music> data = new ArrayList<>();

    public List<Music> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Music getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemMusicBinding binding;
        if (convertView == null) {
            binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ItemMusicBinding) convertView.getTag();
        }
        Music music = getItem(position);
        binding.tvDuration.setText(music.duration);
        binding.tvName.setText(music.name);
        binding.tvSinger.setText(music.singer);
        binding.tvPosition.setText((position + 1) + "");
        return convertView;
    }
}
