package cn.hdu.edu.goldmusic;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SongItemAdapter extends ArrayAdapter<Song> {
    public SongItemAdapter(Activity context, ArrayList<Song> words) {
        super(context, 0, words);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.music_item, parent, false);
        }
        Song currentSong = getItem(position);

        TextView songTitleView = listItemView.findViewById(R.id.songTitle);
        songTitleView.setText(currentSong.getSongTitle());

        TextView songCreatorView = listItemView.findViewById(R.id.songCreator);
        songCreatorView.setText(currentSong.getSongCreator());

        return listItemView;
    }

}
