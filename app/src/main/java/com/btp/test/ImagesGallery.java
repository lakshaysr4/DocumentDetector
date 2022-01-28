package com.btp.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

import com.btp.test.ml.Model11;

import org.apache.commons.lang3.RandomStringUtils;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;

public class ImagesGallery {

    public static ArrayList<String> listOfImages(Context context) {
        ArrayList<String> listOfAllImages = new ArrayList<>();
        SharedPreferences sharedPreferences = null;
        Date latestFileDate = null, systemDate = null;
        Uri uri;
        Cursor cursor;
        int columnIndexData, columnIndexFolderName;
        String absolutePathOfImage;

        //Scanning for new images
        Model11 model = null;
        try {
            model = Model11.newInstance(context);
        } catch (IOException e) {
            e.printStackTrace();
        }


        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.BUCKET_DISPLAY_NAME};
        String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int i = 1;
        while (cursor.moveToNext() && i++<25) {
            absolutePathOfImage = cursor.getString(columnIndexData);
            File imageFile = new File(absolutePathOfImage);
            if(i==2)
            {
               latestFileDate = new Date(imageFile.lastModified());
            }
            sharedPreferences =  context.getSharedPreferences("MySharedPref",context.MODE_PRIVATE);
            if(sharedPreferences.getLong(Constants.LAST_MODIFIED_DATE,-1)!=-1) {
                Date fileDate = new Date(imageFile.lastModified());
                systemDate = new Date(sharedPreferences.getLong(Constants.LAST_MODIFIED_DATE,-1));
                if(systemDate.compareTo(fileDate)>=0) {
                    break;
                }
            }
            BitmapFactory.Options bmOptions  = new BitmapFactory.Options();
            Bitmap bp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
            bp = Bitmap.createScaledBitmap(bp, 256, 256, true);

            tensorImage.load(bp);
            ByteBuffer byteBuffer = tensorImage.getBuffer();
            inputFeature0.loadBuffer(byteBuffer);
            Model11.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            if(outputFeature0.getFloatArray()[0] >= 0.7f)
            {
                //listOfAllImages.add(absolutePathOfImage);
                final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),Constants.appDirectoryName);
                final File duplicateImageFile = new File(imageRoot, RandomStringUtils.random(10,true,true)+".jpg");
                FileChannel source = null;
                FileChannel destination = null;
                try {
                    source = new FileInputStream(imageFile).getChannel();
                    destination = new FileOutputStream(duplicateImageFile).getChannel();
                    if(source!=null && destination!=null) {
                        destination.transferFrom(source,0,source.size());
                    }
                    if(source!=null) {
                        source.close();
                    }
                    if(destination!=null) {
                        destination.close();
                    }
                }
                catch (Exception e) {
                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }
        model.close();
        SharedPreferences.Editor myEditor = sharedPreferences.edit();
        if(systemDate==null && latestFileDate!=null) {
            myEditor.putLong(Constants.LAST_MODIFIED_DATE,latestFileDate.getTime());

        }
        if(systemDate!=null) {
            if(systemDate.compareTo(latestFileDate) < 0)
            {
                myEditor.putLong(Constants.LAST_MODIFIED_DATE,latestFileDate.getTime());
            }
        }
        myEditor.commit();


//        //Getting Images from the document folder

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/DocumentDetector/");
        File[] allFiles = folder.listFiles();

        for(File f : allFiles) {
            listOfAllImages.add(f.getAbsolutePath());
        }

        return listOfAllImages;
    }
}
