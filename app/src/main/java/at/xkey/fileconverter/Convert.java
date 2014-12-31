package at.xkey.fileconverter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.devpaul.filepickerlibrary.FilePickerActivity;
import com.devpaul.filepickerlibrary.enums.FileScopeType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import at.xkey.fileconverter.client.FileConverterClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;


public class Convert extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_convert);

        final Button button = (Button) findViewById(R.id.convert_btn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openFileDialog();
                //StartConverting("");
            }
        });
    }

    private void openFileDialog() {
        Intent filePicker  = new Intent(this, FilePickerActivity.class);
        filePicker.putExtra(FilePickerActivity.SCOPE_TYPE, FileScopeType.ALL);
        filePicker.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_FILE);
        filePicker.putExtra(FilePickerActivity.INTENT_EXTRA_COLOR_ID, android.R.color.holo_orange_dark);
        startActivityForResult(filePicker, FilePickerActivity.REQUEST_FILE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FilePickerActivity.REQUEST_FILE && resultCode == RESULT_OK) {
            Toast.makeText(this, "File Selected: " + data
                            .getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH),
                    Toast.LENGTH_LONG).show();


            String path = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            FileConverterClient.UploadAndWaitFor(new TypedFile(getMimeType(path), new File(path)), new Callback<FileConverterClient.FileConverter.FileResult>() {
                @Override
                public void success(FileConverterClient.FileConverter.FileResult fileResult, Response response) {
                    String savePath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + fileResult.filename;
                    try {
                        File file = new File(savePath);
                        //file.createNewFile();

                        FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(fileResult.bytes);
                        outputStream.close();

                        try {
                            PackageManager packageManager = getPackageManager();
                            Intent testIntent = new Intent(Intent.ACTION_VIEW);
                            testIntent.setType("application/pdf");
                            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri = Uri.fromFile(file);
                            intent.setDataAndType(uri, "application/pdf");
                            startActivity(intent);
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Downloaded File to "+savePath, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e) {
                        e.getStackTrace();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void StartConverting(String path) {


        FileConverterClient.FileConverter.GetJobResult response = FileConverterClient.getInstance().GetJob("4caf2155-c39a-440b-9229-c8bab36cd1ce");

        /*Response fileresp = FileConverterClient.getInstance().DownloadResult("cb71ef60-1643-46b2-a414-9817d9e40ba0",0);
        try {

            byte[] bytes = getBytesFromStream(fileresp.getBody().in());
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/test.pdf");
            //file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();

            PackageManager packageManager = getPackageManager();
            Intent testIntent = new Intent(Intent.ACTION_VIEW);
            testIntent.setType("application/pdf");
            List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            startActivity(intent);
        } catch (Exception e) {
            e.getStackTrace();
        }*/


        FileConverterClient.getInstance().GetResultEx("92a1a7cc-7437-4386-9ca4-a00cc35c6564", 0, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try {

                    byte[] bytes = FileConverterClient.getBytesFromStream(response.getBody().in());
                    File file = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/test.pdf");
                    //file.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(bytes);
                    outputStream.close();

                    try {
                        PackageManager packageManager = getPackageManager();
                        Intent testIntent = new Intent(Intent.ACTION_VIEW);
                        testIntent.setType("application/pdf");
                        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri uri = Uri.fromFile(file);
                        intent.setDataAndType(uri, "application/pdf");
                        startActivity(intent);
                    }
                    catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), "Downloaded File to ", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
