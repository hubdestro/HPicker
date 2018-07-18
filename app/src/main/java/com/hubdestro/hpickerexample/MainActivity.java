package com.hubdestro.hpickerexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.hubdestro.hpicker.HPicker;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImage = findViewById(R.id.ivImage);
    }


    public void onClick(View v) {
        HPicker.selectImage(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        HPicker.onActivityResult(this, requestCode, resultCode, data,
                new HPicker.OnImagePicked() {
                    @Override
                    public void onSuccess(File imageFile, Bitmap bm) {
                        ivImage.setImageBitmap(bm);
                    }
                });
    }
}
