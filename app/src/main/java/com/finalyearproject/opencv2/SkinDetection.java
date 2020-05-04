package com.finalyearproject.opencv2;

import android.os.Bundle;

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
        m1 = new Mat(width,height, CvType.CV_8UC3);
        m2 = new Mat(width,height, CvType.CV_8UC3);
//        mask = new Mat(width,height, CvType.CV_8UC3);
//        temp1 = new Mat(width,height, CvType.CV_8UC3);
//        temp2 = new Mat(width,height, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Imgproc.resize(m1,m1,new Size(120,120), Imgproc.INTER_LINEAR);
        //m2.empty();
        Imgproc.cvtColor(inputFrame.rgba(),m1, Imgproc.COLOR_RGBA2RGB);
//        Imgproc.cvtColor(m1,m1, Imgproc.COLOR_RGB2BGR);
//        Imgproc.cvtColor(m1,mask, Imgproc.COLOR_BGR2YCrCb);  //Save the mask
//        Core.inRange(mask,scalarLow,scalarHigh,temp1);  //Get the hsv ranges
//        Core.bitwise_and(m1,m1,temp2,temp1);  //Bitwise-And into m2 with temp2 mask
//        Imgproc.GaussianBlur(temp2,m2,new Size(3,3),3);  //Gaussian Blur with m2 frame and 3x3 matrix
//
//        Core.flip(m2.t(),m2,1);

        //Core.divide(m2, new Scalar(255.0), temp1); //down-scaling for normalization purposes

//        return m2;

        //String m2Dump = m2.dump();
        //String temp1Dump = temp1.dump();
        //Log.d("content", m2Dump);
        //Log.d("content", temp1Dump);

        //Imgproc.cvtColor(inputFrame.rgba(),m1,Imgproc.COLOR_BGR2HSV);
        //Imgproc.cvtColor(m1,m1,Imgproc.COLOR_RGB2HSV_FULL);
        //Imgproc.resize(m2,m2,m1.size());
        m2 = skinDetection(m1);
        return m2;
    }

    private Mat skinDetection(Mat src) {
        // define the upper and lower boundaries of the HSV pixel

        // Convert to HSV
        Mat rgbFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src,rgbFrame, Imgproc.COLOR_RGB2BGR,3);

        //Convert to YCRb
        Mat ycrFrame = new Mat(rgbFrame.rows(), rgbFrame.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(rgbFrame,ycrFrame, Imgproc.COLOR_BGR2YCrCb,3);  //Save the mask

        // Mask the image for skin colors
        Mat skinMask = new Mat(ycrFrame.rows(), ycrFrame.cols(), CvType.CV_8U, new Scalar(3));
        Core.inRange(ycrFrame, scalarLow, scalarHigh, skinMask);

        //Bitwise function
        Mat skin = new Mat(skinMask.rows(), skinMask.cols(), CvType.CV_8U, new Scalar(3));
        Core.bitwise_and(src, src, skin, skinMask);

        // blur the mask to help remove noise
        final Size ksize = new Size(3, 3);
        Imgproc.GaussianBlur(skin, skin, ksize, 3);

        Core.flip(skin.t(),skin,1);

        return skin;
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
