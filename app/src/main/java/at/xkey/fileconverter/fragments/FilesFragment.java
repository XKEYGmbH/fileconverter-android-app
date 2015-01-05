package at.xkey.fileconverter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.norbsoft.typefacehelper.TypefaceHelper;

import java.io.File;
import java.util.ArrayList;

import at.xkey.fileconverter.R;
import at.xkey.fileconverter.adapter.FilesAdapter;

/**
 * Created by Admin on 05.01.2015.
 */
public class FilesFragment extends Fragment {
    private View view;
    private ListView lv;
    ArrayList<String> FilesInFolder;
    FilesAdapter adapter;
    public FilesFragment() {

    }

    public interface FileAction {
        public void openFile(File file);
    }

    private FileAction mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (FileAction) activity;
        } catch (ClassCastException e) {
            e.getStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);


        lv = (ListView)view.findViewById(R.id.filelist);
        FilesInFolder = GetFiles(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/fileconverter");
        /*adapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(),
                R.layout.file_item, FilesInFolder);*/


        adapter = new FilesAdapter(this.getActivity().getApplicationContext(), FilesInFolder);

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/fileconverter/" + adapter.getItem(position));
                mCallback.openFile(file);
            }
        });



        TypefaceHelper.typeface(view);
        return view;
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {

        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return MyFiles;
        else {
            for (int i=0; i<files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }

    public void RefreshFiles() {
        FilesInFolder = GetFiles(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/fileconverter");
        adapter.addAll(FilesInFolder);
    }
}
