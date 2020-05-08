package com.finalyearproject.opencv2;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class SkinDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;
    private Mat m1,m2;
    private Scalar scalarLow,scalarHigh;
    protected ByteBuffer imgData = null;
    private float[][] ProbArray = null;
    private Interpreter tflite;
    private List<String> labelList;
    private Character resultString;
    private static final int  DIM_HEIGHT = 120;
    private static final int DIM_WIDTH = 120;
    private static final int BYTES = 12;
    private static int digit = -1;
    private static float  prob = 0.0f;
    private TextView textView;
    private MultiAutoCompleteTextView text1;
    private TextToSpeech textToSpeech;
    private ArrayList<String> words= new ArrayList<String>();
    private ArrayList<Character> cnnOutputArray = new ArrayList<Character>();
    private HashMap<Character, Integer> elementCountMap = new HashMap<Character, Integer>();
    private Set<Map.Entry<Character, Integer>> entrySet;
    private char element;
    private int frequency = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);

        //text to speech part
        textView = findViewById(R.id.textView);
        text1 = findViewById(R.id.mactv);
        text1.requestFocus();

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.ENGLISH);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");


                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        String data="";
        InputStream is = this.getResources().openRawResource(R.raw.input);
        BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

        if(is!=null)
        {
            try{
                while((data=reader.readLine())!=null)
                {
                    words.add(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        text1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length()!=0){
                    char s= editable.charAt(editable.length()-1);
                    if (s=='.') {
                        Log.e(Character.toString(s), "pressed");

                        String sentence = text1.getText().toString();

                        sentence=sentence.substring(sentence.lastIndexOf('\n')+1,sentence.length());

                        int speechStatus = textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);

                        if (speechStatus == TextToSpeech.ERROR) {
                            Log.e("TTS", "Error in converting Text to Speech!");
                        }

                        text1.setText("");
                    }}
            }
        });
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,words);

        text1.setAdapter(adapter);
        text1.setTokenizer(new SpaceTokenizer());

        //ML and skin detection part
        OpenCVLoader.initDebug();

        try{
            tflite = new Interpreter(loadModelFile());
            labelList = loadLabelList();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        imgData = ByteBuffer.allocateDirect(DIM_WIDTH * DIM_HEIGHT  * BYTES);
        imgData.order(ByteOrder.nativeOrder());
        ProbArray = new float[1][26];

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

        //rescaling
        Mat rescaled = new Mat(m2.rows(), m2.cols(), CvType.CV_8U, new Scalar(3));
        Core.divide(m2, new Scalar(255.0), rescaled);

        //converting frame to tflite format
        convertMattoTfLiteInput(rescaled);

        //passing converted image to cnn for classification and gives result
        runModel();

        //Resizing it back to original size cause android doesn't support returning resized image
        Mat resized = inputFrame.rgba();
        Imgproc.resize(m2,m2,resized.size());

        return m2;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private void runModel() {
        if(imgData != null)
            tflite.run(imgData, ProbArray);
        resultString = maxProbIndex(ProbArray[0]).toCharArray()[0];
        cnnOutputArray.add(resultString);
        if(cnnOutputArray.size()==60)
        {
            resultString = mostOccurring(cnnOutputArray);
            textView.setText(resultString.toString());
            runOnUiThread(new Runnable() {
                public void run() {
                    text1.append(resultString.toString());
                }
            });
            Log.d("classify","Classification = "+resultString);
            cnnOutputArray.clear();
        }
    }

    private char mostOccurring(ArrayList<Character> cnnOutputArray) {

        for (Character i : cnnOutputArray)
        {
            if (elementCountMap.containsKey(i))
            {
                //If an element is present, incrementing its count by 1
                elementCountMap.put(i, elementCountMap.get(i)+1);
            }
            else
            {
                //If an element is not present, put that element with 1 as its value
                elementCountMap.put(i, 1);
            }
        }

         entrySet = elementCountMap.entrySet();

        for (Map.Entry<Character, Integer> entry : entrySet)
        {
            if(entry.getValue() > frequency)
            {
                element = entry.getKey();
                frequency = entry.getValue();
            }
        }
        return element;
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

        //flipping the image
        //Core.flip(skin.t(),skin,0);

        return skin;
    }

    private void convertMattoTfLiteInput(Mat mat) {
        imgData.rewind();
        int pixel = 0;
        for (int i = 0; i < DIM_HEIGHT; ++i) {
            for (int j = 0; j < DIM_WIDTH; ++j) {
                imgData.putFloat((float)mat.get(i,j)[0]);
            }
        }
    }

    private String maxProbIndex(float[] probs) {
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIndex = i;
            }
        }
        prob = maxProb;
        digit = maxIndex;
        return labelList.get(maxIndex);
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
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}
