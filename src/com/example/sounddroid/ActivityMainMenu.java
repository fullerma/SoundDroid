package com.example.sounddroid;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
 
import com.example.sounddroid.ImageTextListAdapter.ImageTextListItem;
import com.example.sounddroid.ActivitySongs;
 
import java.util.ArrayList;
import java.util.List;
 
public class ActivityMainMenu extends ListActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
 
        // Create our list of menu items
        List<ImageTextListItem> menuItems = new ArrayList<ImageTextListItem>();
        menuItems.add(new MainMenuListItem(R.string.menu_playlists, R.drawable.ic_action_storage, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_artists, R.drawable.ic_action_person, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_albums, R.drawable.ic_action_brightness_high, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_songs, R.drawable.ic_action_headphones, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_genres, R.drawable.ic_action_gamepad, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_moods, R.drawable.ic_action_group, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_eras, R.drawable.ic_action_time, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_favorites, R.drawable.ic_action_favorite, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_top_rated, R.drawable.ic_action_important, ActivitySongs.class));
        menuItems.add(new MainMenuListItem(R.string.menu_search, R.drawable.ic_action_search, ActivitySongs.class));
 
        setListAdapter(new ImageTextListAdapter(this, menuItems));
    }
 
    @Override
    public void onListItemClick(ListView parent, View v, int position, long id)
    {
        MainMenuListItem selectedListItem = (MainMenuListItem) getListView().getItemAtPosition(position);
        startActivity(new Intent(this, selectedListItem.getActivityClass()));
    }
 
    private static class MainMenuListItem extends ImageTextListItem
    {
        private Class<? extends Activity> mActivityClass;
 
        public MainMenuListItem(int textResourceId, int imageResourceId, Class<? extends Activity> activityClass)
        {
            super(textResourceId, imageResourceId);
 
            setActivityClass(activityClass);
        }
 
        public Class<? extends Activity> getActivityClass()
        {
            return mActivityClass;
        }
 
        public void setActivityClass(Class<? extends Activity> activityClass)
        {
            mActivityClass = activityClass;
        }
    }
}

