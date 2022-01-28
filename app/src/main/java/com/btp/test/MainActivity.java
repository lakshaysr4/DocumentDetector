package com.btp.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.btp.test.ml.Model;
import com.btp.test.ml.Model11;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private ImageView ivDisplay;
    private Button btnChoose;
    private Button btnPredict;
    private Button btnOcr;
    private static final int SELECT_PICTURE = 100;
    private Bitmap bp;
    public static String abc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
        setUpListeners();

        final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),Constants.appDirectoryName);
        imageRoot.mkdirs();

    }

    private void setUpListeners() {
        btnChoose.setOnClickListener(v -> startScanning());
        ivDisplay.setOnClickListener(v -> chooseImage());
        btnPredict.setOnClickListener(v -> predictImage());
        btnOcr.setOnClickListener(v -> predictText());
    }

    private void chooseImage() {
        btnOcr.setVisibility(View.INVISIBLE);
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    private void predictImage() {
        if(bp==null) {
            Toast.makeText(MainActivity.this,"Please select an image first",Toast.LENGTH_LONG).show();
        } else {
            bp = Bitmap.createScaledBitmap(bp, 256, 256, true);


            try {
                Model11 model = Model11.newInstance(MainActivity.this);

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(bp);

                ByteBuffer byteBuffer = tensorImage.getBuffer();

                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                Model11.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                if(outputFeature0.getFloatArray()[0]>=0.7f) {
                    Toast.makeText(MainActivity.this, "Document", Toast.LENGTH_LONG).show();
                    btnOcr.setVisibility(View.VISIBLE);
                } else
                    Toast.makeText(MainActivity.this,  "Not a Document", Toast.LENGTH_LONG).show();
                // Releases model resources if no longer used.
                model.close();
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }

    }

    private void startScanning() {
         Intent intent = new Intent(MainActivity.this,GalleryActivity.class);
          startActivity(intent);
    }

    private void initUi() {
        ivDisplay = findViewById(R.id.ivDisplay);
        btnChoose = findViewById(R.id.btnScanAllImages);
        btnPredict = findViewById(R.id.btnPredict);
        btnOcr = findViewById(R.id.btnOcr);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    ivDisplay.setImageURI(selectedImageUri);
                }
                try {
                    bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void predictText() {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bp, 0);

        Task<Text> result =
                recognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> {
                            String resultText = visionText.getText();
                            Toast.makeText(MainActivity.this, resultText, Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(
                                e -> Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show());
    }
}