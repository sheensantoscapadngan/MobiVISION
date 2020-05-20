package com.example.android.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private static final int REQUEST_CAMERA = 1;
    private MainActivity mainActivity;
    public static ScannerActivity scannerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerActivity = this;
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);


    }
    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    public static ScannerActivity getInstance(){
        return scannerActivity;
    }

    @Override
    public void handleResult(Result result) {
        String scanResult = result.getText();

        Intent intent = new Intent();
        intent.putExtra("RESULT_STRING",scanResult);
        setResult(RESULT_OK,intent);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();

            }
        }
    }
}
