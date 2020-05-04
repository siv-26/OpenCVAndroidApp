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
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Imgproc.cvtColor(inputFrame.rgba(),m1, Imgproc.COLOR_RGBA2RGB);

        //Resizing the image to 120x120
        Size newSize = new Size(120, 120);
        Mat fit = new Mat(newSize, CvType.CV_8UC3);
        Imgproc.resize(m1,fit,newSize,Imgproc.INTER_LINEAR);

        //calling pre-processing method
        m2 = skinDetection(m1);

        //Resizing it back to original size cause android doesn't support returning resized image
        Mat resized = inputFrame.rgba();
        Imgproc.resize(m2,m2,resized.size());

        //flipping the image
        Core.flip(m2.t(),m2,1);

        return m2;
    }

    private Mat skinDetection(Mat src) {

        // Convert to BGR
        Mat rgbFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src,rgbFrame, Imgproc.COLOR_RGB2BGR,3);

        //Convert to YCrCb
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
