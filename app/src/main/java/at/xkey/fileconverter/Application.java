package at.xkey.fileconverter;


import android.graphics.Typeface;

import com.norbsoft.typefacehelper.TypefaceCollection;
import com.norbsoft.typefacehelper.TypefaceHelper;

/**
 * Created by Admin on 02.01.2015.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceCollection typeface = new TypefaceCollection.Builder()
                .set(Typeface.NORMAL, Typeface.createFromAsset(getAssets(), "fonts/ostrich-regular.ttf"))
                .set(Typeface.BOLD, Typeface.createFromAsset(getAssets(), "fonts/ostrich-bold.ttf"))
                .create();
        TypefaceHelper.init(typeface);
    }
}
