package at.xkey.fileconverter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.norbsoft.typefacehelper.TypefaceHelper;

import java.util.ArrayList;

import at.xkey.fileconverter.R;

/**
 * Created by Admin on 05.01.2015.
 */
public class FilesAdapter extends BaseAdapter {
    private Context mContext;
    public ArrayList<String> list;
    public FilesAdapter(Context context, ArrayList<String> items) {
        mContext = context;
        list = items;
    }

    public void addAll(ArrayList<String> items) {
        if(list==null){
            list = new ArrayList<String>();
        }
        list.clear();
        list.addAll(items);
        notifyDataSetChanged();
    }

    @Override public int getCount() {
        return list.size();
    }
    @Override public String getItem(int position) {
        return list.get(position);
    }
    @Override public long getItemId(int position) {
        return list.get(position).hashCode();
    }
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.file_item, parent, false);

            TypefaceHelper.typeface(convertView);
        }

        ((TextView)convertView.findViewById(R.id.file_name)).setText(list.get(position));
        return convertView;
    }
}
