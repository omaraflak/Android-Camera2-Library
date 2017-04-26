package me.aflak.libraries;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import java.io.File;

/**
 * Created by Omar on 25/02/2017.
 */

public class DisplayActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        findViewById(R.id.returnButton).setOnClickListener(this);

        String filename = getIntent().getExtras().getString("filename", "image.jpg");
        File file = new File(getFilesDir(), filename);
        Glide.with(this).load(file).into(imageView);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
