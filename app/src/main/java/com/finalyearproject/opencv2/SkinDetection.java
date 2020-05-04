package com.finalyearproject.opencv2;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class SkinDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    JavaCameraView javaCameraView;
    Mat m1,m2,mask,temp1,temp2;
    Scalar scalarLow,scalarHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);
        OpenCVLoader.initDebug();

        javaCameraView = (JavaCameraView) findViewById(R.id.camview);
        javaCameraView.setCameraIndex(0);

        scalarLow = new Scalar(0,133,77);
        scalarHigh = new Scalar(235,173,127);

        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        m1 = new Mat(width,height, CvType.CV_8UC4);
        m2 = new Mat(width,height, CvType.CV_8UC4);
        mask = new Mat(width,height, CvType.CV_8UC4);
        temp1 = new Mat(width,height, CvType.CV_8UC4);
        temp2 = new Mat(width,height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Imgproc.resize(m1,m1,new Size(120,120), Imgproc.INTER_LINEAR);
        Imgproc.cvtColor(inputFrame.rgba(),m1, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(m1,m1, Imgproc.COLOR_RGB2BGR);
        Imgproc.cvtColor(m1,mask, Imgproc.COLOR_BGR2YCrCb);  //Save the mask
        Core.inRange(mask,scalarLow,scalarHigh,m2);  //Get the hsv ranges
        Core.bitwise_and(m1,m1,m2,temp2);  //Bitwise-And into m2 with temp2 mask

        Imgproc.GaussianBlur(m2,m2,new Size(3,3),3);  //Gaussian Blur with m2 frame and 3x3 matrix

        Core.flip(m2.t(),m2,1);
        Core.divide(m2, new Scalar(255.0), temp1);

        return m2;

        //String m2Dump = m2.dump();
        //String temp1Dump = temp1.dump();
        //Log.d("content", m2Dump);
        //Log.d("content", temp1Dump);

        //Imgproc.cvtColor(inputFrame.rgba(),m1,Imgproc.COLOR_BGR2HSV);
        //Imgproc.cvtColor(m1,m1,Imgproc.COLOR_RGB2HSV_FULL);
        //Imgproc.resize(m2,m2,m1.size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        javaCameraView.enableView();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        javaCameraView.disableView();
    }

}
