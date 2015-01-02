package at.xkey.fileconverter;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.devpaul.filepickerlibrary.FilePickerActivity;
import com.devpaul.filepickerlibrary.enums.FileScopeType;
import com.norbsoft.typefacehelper.TypefaceHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;

import at.xkey.fileconverter.client.FileConverterClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ConvertFragment())
                    .commit();
        }

        TypefaceHelper.typeface(this);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ConvertFragment extends Fragment {
        private View view;
        private SharedPreferences sharedPref;
        private boolean readConfig() {
            String serverUrl = sharedPref.getString(SettingsActivity.KEY_SERVER_URL, ""),
                    password = sharedPref.getString(SettingsActivity.KEY_PASSWORD, ""),
                    username = sharedPref.getString(SettingsActivity.KEY_USERNAME, "");

            if (!serverUrl.isEmpty()) {
                FileConverterClient.setURL(serverUrl);
                FileConverterClient.setUsername(username);
                FileConverterClient.setPassword(password);
                try {
                    FileConverterClient.setConnectionTimeout(5);
                    FileConverterClient.getInstance().Auth();
                    return true;
                }
                catch (RetrofitError e) {
                    return false;
                }

            }
            return false;
        }

        public ConvertFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_main, container, false);

            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.OnSharedPreferenceChangeListener listener =
                    new SharedPreferences.OnSharedPreferenceChangeListener() {
                        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                            init();
                        }
                    };
            sharedPref.registerOnSharedPreferenceChangeListener(listener);

            init();
            TypefaceHelper.typeface(view);
            return view;
        }

        private void init() {
            final ImageButton button = (ImageButton) view.findViewById(R.id.convert_btn);
            final Button startBtn = (Button) view.findViewById(R.id.start_btn);
            final TextView textView = (TextView) view.findViewById(R.id.progess);
            profileSpinner = (Spinner)view.findViewById(R.id.spinner);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startAnimation();
                }
            });


            startBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openFileDialog();
                }
            });

            if (!readConfig()) {
                button.setEnabled(false);
                startBtn.setEnabled(false);
                restoreState();
                textView.setText("Please configure the connection!");
            }
            else {
                button.setEnabled(true);
                startBtn.setEnabled(true);
                fillDropDown();
                textView.setText("Click to start");
            }
        }


        private Spinner profileSpinner;
        private SettingsAdapter profileAdapter;
        private void fillDropDown() {
            FileConverterClient.FileConverter.GetSettingsCollectionResult settingsResult = FileConverterClient.getInstance().GetSettingsCollection();
            List<FileConverterClient.FileConverter.SettingCollection> settings = settingsResult.GetSettingsCollectionResult;

            profileAdapter = new SettingsAdapter(getActivity(),
                    android.R.layout.simple_spinner_item,
                    settings);

            profileSpinner.setAdapter(profileAdapter);

        }

        private Animation myLogoRotation;
        private void startProgress() {
            final ImageView myImage = (ImageView)view.findViewById(R.id.convert_btn);
            myImage.post(new Runnable() {
                @Override
                public void run() {
                    myLogoRotation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotator);
                    myImage.startAnimation(myLogoRotation);
                }
            });

        }

        private void stopProgress() {
            if (myLogoRotation != null) {
                myLogoRotation.setRepeatCount(0);
            }
            else {
                final ImageView myImage = (ImageView)view.findViewById(R.id.convert_btn);
                myImage.post(new Runnable() {
                    @Override
                    public void run() {
                        myImage.clearAnimation();
                    }
                });
            }
            final TextView textView = (TextView) view.findViewById(R.id.progess);
            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText("");
                }
            });
        }

        private void openFileDialog() {
            Intent filePicker = new Intent(getActivity(), FilePickerActivity.class);
            filePicker.putExtra(FilePickerActivity.SCOPE_TYPE, FileScopeType.ALL);
            filePicker.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_FILE);
            filePicker.putExtra(FilePickerActivity.INTENT_EXTRA_COLOR_ID, R.color.orange);
            startActivityForResult(filePicker, FilePickerActivity.REQUEST_FILE);
        }

        private void restoreState() {
            if (view.getHeight() > view.findViewById(R.id.button_container).getHeight()) {
                ResizeAnimation resizeAnimation = new ResizeAnimation(view.findViewById(R.id.button_container), view.getHeight());
                resizeAnimation.setDuration(600);
                view.startAnimation(resizeAnimation);
            }
        }

        private void startAnimation() {
            ResizeAnimation resizeAnimation = new ResizeAnimation(view.findViewById(R.id.button_container), view.getHeight()/2);
            resizeAnimation.setDuration(600);
            view.startAnimation(resizeAnimation);
        }

        public String getMimeType(String url)
        {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension != null) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                type = mime.getMimeTypeFromExtension(extension);
            }
            return type;
        }

        private void ProgressToText(FileConverterClient.FileConverter.UploadWaitCallback.Stats stat) {
            final TextView textView = (TextView) view.findViewById(R.id.progess);
            String text = "";
            boolean change = true;
            switch (stat) {
                case DOWNLOADING:
                    text = "Downloading...";
                    break;
                case CONVERTING:
                    text = "Converting...";
                    break;
                case UPLOADING:
                    text = "Uploading...";
                    break;
                case UPLOADED:
                    text = "Uploaded...";
                    break;
                default:
                    change = false;
                    break;
            }

            if (change) {
                final String finalText = text;
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(finalText);
                    }
                });
            }
        }

        private void Upload(String path) {
            String profile = "default";
            if (profileSpinner != null) {
                int profilePos = profileSpinner.getSelectedItemPosition();
                FileConverterClient.FileConverter.SettingCollection setting = profileAdapter.getItem(profilePos);
                if (setting != null) {
                    profile = setting.SettingsName;
                }
            }
            FileConverterClient.UploadAndWaitFor(new TypedFile(getMimeType(path), new File(path)), profile, new FileConverterClient.FileConverter.UploadWaitCallback() {
                @Override
                public void success(FileConverterClient.FileConverter.FileResult fileResult, Response response) {

                    String savePath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + fileResult.filename;
                    try {
                        File file = new File(savePath);
                        //file.createNewFile();


                        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                        //FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(fileResult.bytes);
                        outputStream.flush();
                        outputStream.close();

                        stopProgress();


                        try {
                            PackageManager packageManager = getActivity().getPackageManager();
                            Intent testIntent = new Intent(Intent.ACTION_VIEW);
                            testIntent.setType("application/pdf");
                            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri = Uri.fromFile(file);
                            intent.setDataAndType(uri, "application/pdf");
                            startActivity(intent);
                        } catch (Exception ex) {
                            Toast.makeText(getActivity().getApplicationContext(), "Downloaded File to " + savePath, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }

                @Override
                public void failure(Exception e) {

                }

                @Override
                public void onStart() {
                    startProgress();
                }

                @Override
                public void onProgress(Stats step) {
                    ProgressToText(step);
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == FilePickerActivity.REQUEST_FILE && resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "File Selected: " + data
                                .getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH),
                        Toast.LENGTH_LONG).show();


                final String path = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Upload(path);

                        }
                        catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();
            }
            super.onActivityResult(requestCode, resultCode, data);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.convert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
