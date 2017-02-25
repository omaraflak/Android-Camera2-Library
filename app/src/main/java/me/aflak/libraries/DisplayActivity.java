package me.aflak.libraries;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Omar on 25/02/2017.
 */

public class DisplayActivity extends Activity {
    @BindView(R.id.activity_display_image) ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        ButterKnife.bind(this);

        final String filename = getIntent().getExtras().getString("filename", "image.jpg");
        final File file = new File(getFilesDir(), filename);
        Glide.with(this).load(file).into(imageView);
    }

    @OnClick(R.id.activity_display_return)
    public void onClick(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
