package com.finalyearproject.opencv2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        b1 = (Button) findViewById(R.id.button1);
        b1.setEnabled(true);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGesture();
            }
        });
        b2 = (Button) findViewById(R.id.button2);
        b2.setEnabled(true);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSpeechToText();
            }
        });
    }

    private void checkPermissions() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permissions Not Granted", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(MainActivity.this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }

    private void openGesture(){
        Intent intent1 = new Intent(this,SkinDetection.class);
        startActivity(intent1);
    }

    private void openSpeechToText(){
        Intent intent2 = new Intent(this,SpeechTranslator.class);
        startActivity(intent2);
    }
}
