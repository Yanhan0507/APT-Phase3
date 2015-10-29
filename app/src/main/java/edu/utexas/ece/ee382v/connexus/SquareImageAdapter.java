package edu.utexas.ece.ee382v.connexus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.utexas.ece.ee382v.connexus.connexus.R;

/**
 * Created by ChenguangLiu on 10/28/15.
 */
public class SquareImageAdapter extends BaseAdapter
{
    private List<Item> items = new ArrayList<Item>();
    private Context mContext;
    private LayoutInflater inflater;

    public SquareImageAdapter(Context context, ArrayList<String> imageURLs, ArrayList<String> infos)
    {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        for (int i = 0; i < imageURLs.size(); i++) {
            items.add(new Item(imageURLs.get(i), infos.get(i)));
        }
//        items.add(new Item("Image 1", R.drawable.nature1));
//        items.add(new Item("Image 2", R.drawable.nature2));
//        items.add(new Item("Image 3", R.drawable.tree1));
//        items.add(new Item("Image 4", R.drawable.nature3));
//        items.add(new Item("Image 5", R.drawable.tree2));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i)
    {
        return items.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        View v = view;
        ImageView picture;
        TextView name;

        if(v == null)
        {
            v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        picture = (ImageView)v.getTag(R.id.picture);
        name = (TextView)v.getTag(R.id.text);

        Item item = (Item)getItem(i);

        Picasso.with(mContext).load(item.url).into(picture);
        name.setText(item.info);
        if (item.info.equals("no caption"))
            name.setVisibility(View.INVISIBLE);

        return v;
    }

    private class Item
    {
        final String url;
        final String info;

        Item(String url, String info)
        {
            this.url = url;
            this.info = info;
        }
    }
}