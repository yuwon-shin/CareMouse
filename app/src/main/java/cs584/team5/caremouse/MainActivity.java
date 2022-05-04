package cs584.team5.caremouse;

// import android.support.v7.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.*;


import cs584.team5.caremouse.activitySkeleton.Constants;
import cs584.team5.caremouse.activitySkeleton.DataClassifier;
import cs584.team5.caremouse.activitySkeleton.SensorDataHandler;
import cs584.team5.caremouse.blunobasicdemo.BlunoLibrary;

public class MainActivity extends BlunoLibrary {

    public void checkVerify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) { }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    final static String TAG = "MainActivity";

    // UI elements
    private Button btnStartStretch,btnStartTrain, btnHome;
    private Button btnStart, btnEnd, btnNext;
    private Button btnStartCollectingData, btnFinishCollectingData;
    private Button btnStartTestingModel, btnFinishTestingModel;
    private EditText etClassLabelForModel, etOutputFileName;
    //private TextView tvDataSource, tvLog;
    //private ScrollView scrollViewForLog;

    private SensorDataHandler sensorDataHandler;
    private DataClassifier sensorDataClassifier;

    private ImageButton buttonScan, btnGohome;
    private Button buttonSerialSend;
    private EditText serialSendText;
    private TextView serialReceivedText;
    private ProgressBar progressbar;
    private Vibrator vibrator;


    //**added
    private boolean train = false;
    private final double stretchThreshold = 10.0;
    private final int interval = (int) stretchThreshold/5;
    private int stretch1Index = 0;
    private int stretch2Index  = 0;
    private int stretch3Index  = 0;
    private int stretch4Index  = 0;
    private int stretch5Index  = 0;
    private int prev_StretchIndex;
    private Handler handler3;
    public double percentage_value =100.0;
    public int trainPageNum = 0;

    private LinearLayout prgressbtns;
    private RelativeLayout initLayout, fixedLayout, endStretchLayout, trainfixedLayout, backspace;
    private RelativeLayout stretch1Layout,stretch2Layout,stretch3Layout,stretch4Layout,stretch5Layout;
    private RelativeLayout tr1Layout, tr2Layout, tr3Layout,tr4Layout,tr5Layout;
    private ImageView progressbtn1,progressbtn2,progressbtn3,progressbtn4,progressbtn5;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkVerify();


        //*added
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);


        //기본 레이아웃
        initLayout = (RelativeLayout) findViewById(R.id.initLayout);
        backspace = (RelativeLayout) findViewById(R.id.backspace);
        btnGohome = (ImageButton) findViewById(R.id.btnGohome);

        stretch1Layout = (RelativeLayout) findViewById(R.id.stretch1Layout);
        stretch2Layout = (RelativeLayout) findViewById(R.id.stretch2Layout);
        stretch3Layout = (RelativeLayout) findViewById(R.id.stretch3Layout);
        stretch4Layout = (RelativeLayout) findViewById(R.id.stretch4Layout);
        stretch5Layout = (RelativeLayout) findViewById(R.id.stretch5Layout);
        fixedLayout = (RelativeLayout) findViewById(R.id.fixedlayout);
        endStretchLayout = (RelativeLayout) findViewById(R.id.endStretchLayout);

        prgressbtns = (LinearLayout) findViewById(R.id.prgressbtns) ;
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        progressbtn1 = (ImageView) findViewById(R.id.progressbtn1);
        progressbtn2 = (ImageView) findViewById(R.id.progressbtn2);
        progressbtn3 = (ImageView) findViewById(R.id.progressbtn3);
        progressbtn4 = (ImageView) findViewById(R.id.progressbtn4);
        progressbtn5 = (ImageView) findViewById(R.id.progressbtn5);

        //초기화면에서 보이지 않도록 설정
        backspace.setVisibility(View.INVISIBLE);
        stretch1Layout.setVisibility(View.INVISIBLE);
        stretch2Layout.setVisibility(View.INVISIBLE);
        stretch3Layout.setVisibility(View.INVISIBLE);
        stretch4Layout.setVisibility(View.INVISIBLE);
        stretch5Layout.setVisibility(View.INVISIBLE);
        fixedLayout.setVisibility(View.INVISIBLE);
        progressbar.setVisibility(View.INVISIBLE); //revised
        endStretchLayout.setVisibility(View.INVISIBLE);

        btnStartStretch = (Button) findViewById(R.id.btnStretchStart);  //added
        btnHome = (Button) findViewById(R.id.homebtn);
        btnStartTrain = (Button) findViewById(R.id.btnStartTrain);

        //TRAIN 레이아웃

        tr1Layout = (RelativeLayout) findViewById(R.id.tr1Layout);
        tr2Layout = (RelativeLayout) findViewById(R.id.tr2Layout);
        tr3Layout = (RelativeLayout) findViewById(R.id.tr3Layout);
        tr4Layout = (RelativeLayout) findViewById(R.id.tr4Layout);
        tr5Layout = (RelativeLayout) findViewById(R.id.tr5Layout);
        trainfixedLayout = (RelativeLayout) findViewById(R.id.trainfixedlayout);
        trainfixedLayout.setVisibility(View.INVISIBLE);


        tr1Layout.setVisibility(View.INVISIBLE);
        tr2Layout.setVisibility(View.INVISIBLE);
        tr3Layout.setVisibility(View.INVISIBLE);
        tr4Layout.setVisibility(View.INVISIBLE);
        tr5Layout.setVisibility(View.INVISIBLE);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnEnd.setEnabled(false);
        btnNext.setEnabled(false);


        btnStartCollectingData = (Button) findViewById(R.id.btnStartCollectingData);
        btnFinishCollectingData = (Button) findViewById(R.id.btnFinishCollectingData);
        btnFinishCollectingData.setEnabled(false);

        btnStartTestingModel = (Button) findViewById(R.id.btnStartTestingModel);
        btnFinishTestingModel = (Button) findViewById(R.id.btnFinishTestingModel);
        btnFinishTestingModel.setEnabled(false);

        btnStartCollectingData.setVisibility(View.INVISIBLE);
        btnFinishCollectingData.setVisibility(View.INVISIBLE);
        btnStartTestingModel.setVisibility(View.INVISIBLE);
        btnFinishTestingModel.setVisibility(View.INVISIBLE);

        //tvLog = (TextView) findViewById(R.id.tvLog);
        //scrollViewForLog = (ScrollView) findViewById(R.id.scrollViewForLog);

        etClassLabelForModel = (EditText) findViewById(R.id.etClassLabel);
        etClassLabelForModel.setVisibility(View.INVISIBLE);
        {
            // Class label indicator
            StringBuilder sb = new StringBuilder();
            for(String classLabel : Constants.CLASS_LABELS){
                sb.append(classLabel + ",");
            }
            etClassLabelForModel.setHint(sb.deleteCharAt(sb.length()-1).toString());
        }

        etOutputFileName = (EditText) findViewById(R.id.etOutputFileName);

        //tvDataSource = (TextView) findViewById(R.id.tvDataSource);
        {
            StringBuilder sb = new StringBuilder("[");
            for(String s : Constants.ARFF_FILE_NAMES){
                sb.append(s + ",");
            }
           // tvDataSource.setText(sb.deleteCharAt(sb.length()-1).append("]").toString());
        }


        // Attaching listeners
        btnStartStretch.setOnClickListener(btnStartStretchOnclick);
        btnHome.setOnClickListener(btnHomeOnclick);
        btnGohome.setOnClickListener(btnGohomeOnclick);
        btnStartTrain.setOnClickListener(btnStartTrainOnClick);

        btnStart.setOnClickListener(btnStartCollectingDataOnClick_);
        btnEnd.setOnClickListener(btnFinishCollectingDataOnClick_);
        btnNext.setOnClickListener(btnNextOnlick);

        btnStartCollectingData.setOnClickListener(btnStartCollectingDataOnClick);
        btnFinishCollectingData.setOnClickListener(btnFinishCollectingDataOnClick);
        btnStartTestingModel.setOnClickListener(btnStartTestingModelOnClick);
        btnFinishTestingModel.setOnClickListener(btnFinishTestingModelOnClick);

        this.sensorDataHandler = new SensorDataHandler(this);
        this.sensorDataClassifier = new DataClassifier();
        this.sensorDataHandler.dataAdaptor = this.sensorDataClassifier;

        this.sensorDataClassifier.eventHandler = new DataClassifier.EventHandler() { //이벤트 핸들러 : interface of data adaptor
                @Override
                public void onClassified(final String resultClass) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = sdf.format(cal.getTime());
                        String result = formattedDate + " : " + resultClass;
                        //tvLog.append(result.toString());
                        //tvLog.append("\n");
                        //scrollViewForLog.fullScroll(View.FOCUS_DOWN);
                        //Log.e("Resultclass type", resultClass.getClass().getName());

                        //added 각 동작 횟수 세기!!
                        switch (resultClass) {
                            case "1":
                                stretch1Index++;
                                break;
                            case "2":
                                stretch2Index++;
                                break;
                            case "3":
                                stretch3Index++;
                                break;
                            case "4":
                                stretch4Index++;
                                break;

                            case "5":
                                stretch5Index++;
                                break;
                            default:
                                break;
                        }

                        //added 각 동작 횟수 세기!!
                        Log.e("Result", stretch1Index+", "+stretch2Index+", "+stretch3Index +" , "+stretch4Index+" , "+stretch5Index);



                        //TEST할 떄
                        if(!train){
                            if (stretch1Layout.getVisibility() == View.VISIBLE){ // getVisibility() returns View.VISIBLE or View.INVISIBLE
                                if(stretch1Index > 0 && stretch1Index < stretchThreshold ){ //stretchThreshold : 수행할 스트레칭 횟수 EX-10회
                                    if(prev_StretchIndex != stretch1Index){
                                        percentage_value = percentage_value - (1.0/stretchThreshold)*100;
                                        progressbar.setProgress((int)percentage_value);
                                        //Log.e("Result","percent : "+percentage_value);
                                        prev_StretchIndex = stretch1Index;
                                    }
                                }
                                else if(stretch1Index == stretchThreshold){ //스트레칭 동작 다했을 때!
                                    Log.e("Result","stretch 1 finished");
                                    progressbar.setProgress(0); //프로그레스 바 0으로 설정
                                    stretch1Index = stretch2Index = stretch3Index = stretch4Index= stretch5Index =0; //각 스트레칭 동작 count 초기화
                                    prev_StretchIndex = 0;
                                    percentage_value = 100;
                                    stretch1Layout.setVisibility(View.INVISIBLE);

                                    //toast message
                                    Toast.makeText(MainActivity.this, "Stretch 1 finished! ", Toast.LENGTH_SHORT).show();
                                    vibrator.vibrate(500);


                                    //time.sleep
                                    stretch2Layout.setVisibility(View.VISIBLE); // 스트레칭 동작 2 화면이 사용자에게 보이게 설정
                                    progressbar.setProgress(100); //프로그래스 바 초기화

                                    //fixedLayout의 버튼 색깔 변경
                                    progressbtn1.setImageResource(R.drawable.blackbtn1);
                                    progressbtn2.setImageResource(R.drawable.bluebtn2);
                                    /**
                                     //데이터 저장
                                     Log.i(TAG, "finishTestingModel()");
                                     Log.i(TAG, "etOutputFileName : "+etOutputFileName);
                                     //String outputFileName = etOutputFileName.getText().toString();
                                     String outputFileName = "test"; // 수정
                                     finishModelTest(outputFileName); **/
                                }
                            }
                            //stretch2 화면일때
                            if (stretch2Layout.getVisibility() == View.VISIBLE){ // getVisibility() returns View.VISIBLE or View.INVISIBLE
                                if(stretch2Index > 0 && stretch2Index < stretchThreshold ){ //stretchThreshold : 수행할 스트레칭 횟수 EX-10회
                                    if(prev_StretchIndex != stretch2Index){
                                        percentage_value = percentage_value - (1.0/stretchThreshold)*100;
                                        progressbar.setProgress((int)percentage_value);
                                        //Log.e("Result","percent : "+percentage_value);
                                        prev_StretchIndex = stretch2Index;
                                    }
                                }
                                else if(stretch2Index == stretchThreshold){ //스트레칭 동작 다했을 때!
                                    Log.e("Result","stretch 2 finished");
                                    progressbar.setProgress(0); //프로그레스 바 0으로 설정
                                    stretch1Index = stretch2Index = stretch3Index = stretch4Index= stretch5Index =0; //각 스트레칭 동작 count 초기화
                                    prev_StretchIndex = 0;
                                    stretch2Layout.setVisibility(View.INVISIBLE);

                                    //toast message
                                    Toast.makeText(MainActivity.this, "Stretch 2 finished! ", Toast.LENGTH_SHORT).show();
                                    vibrator.vibrate(500);

                                    stretch3Layout.setVisibility(View.VISIBLE); // 다음 화면이 사용자에게 보이게 설정
                                    ImageView stretchImg3 = (ImageView) findViewById(R.id.stretchImg3);
                                    Glide.with(MainActivity.this).load(R.raw.stretch3).into(stretchImg3);
                                    progressbar.setProgress(100); //프로그래스 바 초기화
                                    percentage_value = 100;

                                    //fixedLayout의 버튼 색깔 변경
                                    progressbtn2.setImageResource(R.drawable.blackbtn2);
                                    progressbtn3.setImageResource(R.drawable.bluebtn3);
                                    /**
                                     //데이터 저장
                                     Log.i(TAG, "finishTestingModel()");
                                     Log.i(TAG, "etOutputFileName : "+etOutputFileName);
                                     //String outputFileName = etOutputFileName.getText().toString();
                                     String outputFileName = "test"; // 파일 이름 수정,
                                     finishModelTest(outputFileName); **/
                                }
                            }
                            //stretch3 화면 일때
                            if (stretch3Layout.getVisibility() == View.VISIBLE){ // getVisibility() returns View.VISIBLE or View.INVISIBLE
                                if(stretch3Index > 0 && stretch3Index < stretchThreshold ){ //stretchThreshold : 수행할 스트레칭 횟수 EX-10회
                                    if(prev_StretchIndex != stretch3Index){
                                        percentage_value = percentage_value - (1.0/stretchThreshold)*100;
                                        progressbar.setProgress((int)percentage_value);
                                        //Log.e("Result","percent : "+percentage_value);
                                        prev_StretchIndex = stretch3Index;
                                    }
                                }
                                else if(stretch3Index == stretchThreshold){ //스트레칭 동작 다했을 때!
                                    Log.e("Result","stretch 3 finished");
                                    progressbar.setProgress(0); //프로그레스 바 0으로 설정
                                    stretch1Index = stretch2Index = stretch3Index = stretch4Index= stretch5Index =0; //각 스트레칭 동작 count 초기화
                                    prev_StretchIndex = 0;
                                    stretch3Layout.setVisibility(View.INVISIBLE);
                                    percentage_value = 100;

                                    //toast message
                                    Toast.makeText(MainActivity.this, "Stretch 3 finished! ", Toast.LENGTH_SHORT).show();
                                    vibrator.vibrate(500);

                                    stretch4Layout.setVisibility(View.VISIBLE); // 다음 화면이 사용자에게 보이게 설정
                                    ImageView stretchImg4 = (ImageView) findViewById(R.id.stretchImg4);
                                    Glide.with(MainActivity.this).load(R.raw.stretch4).into(stretchImg4);
                                    progressbar.setProgress(100); //프로그래스 바 초기화

                                    //fixedLayout의 버튼 색깔 변경
                                    progressbtn3.setImageResource(R.drawable.blackbtn3);
                                    progressbtn4.setImageResource(R.drawable.bluebtn4);

                                    /**
                                     //데이터 저장
                                     Log.i(TAG, "finishTestingModel()");
                                     Log.i(TAG, "etOutputFileName : "+etOutputFileName);
                                     //String outputFileName = etOutputFileName.getText().toString();
                                     String outputFileName = "test"; // 파일 저장 언제 해야되나..?? ㅠㅠ 마지막? check
                                     finishModelTest(outputFileName); **/
                                }
                            }
                            //stretch4 화면 일때
                            if (stretch4Layout.getVisibility() == View.VISIBLE){ // getVisibility() returns View.VISIBLE or View.INVISIBLE
                                if(stretch4Index > 0 && stretch4Index < stretchThreshold ){ //stretchThreshold : 수행할 스트레칭 횟수 EX-10회
                                    if(prev_StretchIndex != stretch4Index){
                                        percentage_value = percentage_value - (1.0/stretchThreshold)*100;
                                        progressbar.setProgress((int)percentage_value);
                                        //Log.e("Result","percent : "+percentage_value);
                                        prev_StretchIndex = stretch4Index;
                                    }
                                }
                                else if(stretch4Index == stretchThreshold){ //스트레칭 동작 다했을 때!
                                    Log.e("Result","stretch 4 finished");
                                    progressbar.setProgress(0); //프로그레스 바 0으로 설정
                                    stretch1Index = stretch2Index = stretch3Index = stretch4Index= stretch5Index =0; //각 스트레칭 동작 count 초기화
                                    prev_StretchIndex = 0;
                                    stretch4Layout.setVisibility(View.INVISIBLE);
                                    percentage_value = 100.0;

                                    //toast message
                                    Toast.makeText(MainActivity.this, "Stretch 4 finished! ", Toast.LENGTH_SHORT).show();
                                    vibrator.vibrate(500);

                                    stretch5Layout.setVisibility(View.VISIBLE); // 다음 화면이 사용자에게 보이게 설정
                                    ImageView stretchImg5 = (ImageView) findViewById(R.id.stretchImg5);
                                    Glide.with(MainActivity.this).load(R.raw.stretch5).into(stretchImg5);
                                    progressbar.setProgress(100); //프로그래스 바 초기화

                                    //fixedLayout의 버튼 색깔 변경
                                    progressbtn4.setImageResource(R.drawable.blackbtn4);
                                    progressbtn5.setImageResource(R.drawable.bluebtn5);

                                    /**
                                     //데이터 저장
                                     Log.i(TAG, "finishTestingModel()");
                                     Log.i(TAG, "etOutputFileName : "+etOutputFileName);
                                     //String outputFileName = etOutputFileName.getText().toString();
                                     String outputFileName = "test"; // 파일 저장 언제 해야되나..?? ㅠㅠ 마지막? check
                                     finishModelTest(outputFileName); **/
                                }
                            }
                            //stretch5 화면 일때
                            if (stretch5Layout.getVisibility() == View.VISIBLE){ // getVisibility() returns View.VISIBLE or View.INVISIBLE
                                if(stretch5Index > 0 && stretch5Index < stretchThreshold ){ //stretchThreshold : 수행할 스트레칭 횟수 EX-10회
                                    if(prev_StretchIndex != stretch5Index){ // count가 증가했을 때만 프로그래스 바가 줄어들도록 설정
                                        percentage_value = percentage_value - (1.0/stretchThreshold)*100;
                                        progressbar.setProgress((int)percentage_value);
                                        //Log.e("Result","percent : "+percentage_value);
                                        prev_StretchIndex = stretch5Index;
                                    }
                                }
                                else if(stretch5Index == stretchThreshold){ //스트레칭 동작 다했을 때!
                                    Log.e("Result","stretch 5 finished");
                                    progressbar.setProgress(0); //프로그레스 바 0으로 설정
                                    stretch1Index = stretch2Index = stretch3Index = stretch4Index= stretch5Index =0; // 스트레칭 count 초기화
                                    prev_StretchIndex = 0;
                                    stretch5Layout.setVisibility(View.INVISIBLE);
                                    prgressbtns.setVisibility(View.INVISIBLE);
                                    progressbar.setVisibility(View.INVISIBLE);

                                    //toast message
                                    Toast.makeText(MainActivity.this, "Stretch session finished. Good Job! ", Toast.LENGTH_SHORT).show();
                                    vibrator.vibrate(500);

                                    endStretchLayout.setVisibility(View.VISIBLE); // 스트레칭 세션 완료 화면이 사용자에게 보이게 설정

                                    progressbar.setProgress(100); //프로그래스 바 초기화
                                    percentage_value = 100;

                                    //데이터 저장
                                    Log.i(TAG, "finishTestingModel()");
                                    Log.e(TAG, "etOutputFileName : "+etOutputFileName);
                                    //String outputFileName = etOutputFileName.getText().toString();
                                    String outputFileName = "test"; // 파일 저장 언제 해야되나..?? ㅠㅠ 마지막? check
                                    finishModelTest(outputFileName);
                                }
                            }

                        }

                    }
                });
            }
        };

        request(1000, new OnPermissionsResult() {
@Override
public void OnSuccess() {
        Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();
        }

@Override
public void OnFail(List<String> noPermissions) {
        Toast.makeText(MainActivity.this,"fail",Toast.LENGTH_SHORT).show();
        }
        });

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        /*
        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        // serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
            }
        }); */

        buttonScan = (ImageButton) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                buttonScan.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                buttonScan.setEnabled(false);
                Toast.makeText(getApplicationContext(), "Bluetooth is connected :) ", Toast.LENGTH_LONG).show();
                vibrator.vibrate(500);
                //buttonScan.setText("Connected");
                break;
            case isConnecting:
                //buttonScan.setText("Connecting");
                break;
            case isToScan:
                //buttonScan.setText("Scan");
                break;
            case isScanning:
                //buttonScan.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScan.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                buttonScan.setEnabled(true);
                //buttonScan.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }
    String sensorValue = new String();

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        //serialReceivedText.append(theString);//append the text into the EditText //show
        //The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
        // ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
        System.out.println(theString.toString());
        sensorDataHandler.onSensorChanged(theString); //string 을 sensorDataHandler에 넘긴다.
        // sensorDataHandler : 데이터 전처리한다. (데이터 입력 => 윈도우 생성 => 피쳐 => txt로 저장)
    }

    //UI Event Handler

    //Check point !

    private View.OnClickListener btnStartTrainOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(buttonScan.isEnabled()==true)
                Toast.makeText(getApplicationContext(), "Connect to Bluetooth first! ", Toast.LENGTH_LONG).show();
            else{
                Log.i(TAG, "Train started ()");
                initLayout.setVisibility(View.INVISIBLE);
                backspace.setVisibility(View.VISIBLE);
                tr1Layout.setVisibility(View.VISIBLE);
                trainfixedLayout.setVisibility(View.VISIBLE);
            }
        }
    };


    private View.OnClickListener btnStartStretchOnclick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(buttonScan.isEnabled()==true)
                Toast.makeText(getApplicationContext(), "Connect to Bluetooth first! ", Toast.LENGTH_LONG).show();
            else{
                Log.i("result", "startStretch");
                initLayout.setVisibility(View.INVISIBLE);
                backspace.setVisibility(View.VISIBLE);
                stretch1Layout.setVisibility(View.VISIBLE);
                fixedLayout.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.VISIBLE);

                String outputFileName = "testest";
                if (outputFileName == null || outputFileName.equals("")) {
                    Toast.makeText(MainActivity.this, "Output file name is required for testing a model", Toast.LENGTH_SHORT).show();
                } else {
                    startModelTest(Constants.ARFF_FILE_NAMES, outputFileName);
                    //etOutputFileName.setEnabled(false);
                }
            }
        }

    };

    private View.OnClickListener btnHomeOnclick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.e("result", "btnHome Clicked, goes back to home activity");
            endStretchLayout.setVisibility(View.INVISIBLE);
            initLayout.setVisibility(View.VISIBLE);
            //insert Endmodel

        }

    };

    private View.OnClickListener btnGohomeOnclick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            initLayout.setVisibility(View.VISIBLE);
            stretch1Layout.setVisibility(View.INVISIBLE);
            stretch2Layout.setVisibility(View.INVISIBLE);
            stretch3Layout.setVisibility(View.INVISIBLE);
            stretch4Layout.setVisibility(View.INVISIBLE);
            stretch5Layout.setVisibility(View.INVISIBLE);
            tr1Layout.setVisibility(View.INVISIBLE);
            tr2Layout.setVisibility(View.INVISIBLE);
            tr3Layout.setVisibility(View.INVISIBLE);
            tr4Layout.setVisibility(View.INVISIBLE);
            tr5Layout.setVisibility(View.INVISIBLE);
            fixedLayout.setVisibility(View.INVISIBLE);
            progressbar.setVisibility(View.INVISIBLE);
            trainfixedLayout.setVisibility(View.INVISIBLE);
            backspace.setVisibility(View.INVISIBLE);

        }

    };

    private View.OnClickListener btnStartCollectingDataOnClick_ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.e(TAG, "startBuildingModel() ");
            btnStart.setEnabled(false);
            btnEnd.setEnabled(true);
            btnNext.setEnabled(false);
            //training 처음 단계일 때
            if( trainPageNum == 0 ){
                trainPageNum ++;
            }

            String classLabelInput = Integer.toString(trainPageNum);

            // 유효한 클래스 인지 확인
            boolean isValidClassLabel = false;
            for(String classLabel : Constants.CLASS_LABELS){
                if(classLabel.equals(classLabelInput)){
                    isValidClassLabel = true;
                    break;
                }
            }

            if(isValidClassLabel){
                btnStart.setEnabled(false);
                startDataCollection(classLabelInput);
            } else {
                Toast.makeText(MainActivity.this, "Class label should be one of the 1-5", Toast.LENGTH_SHORT).show();
            }
            Log.e("pagenum", String.valueOf(trainPageNum));
        }
    };

    private View.OnClickListener btnFinishCollectingDataOnClick_ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.e("Listener", "finishBuildingModel()");
            Log.e("pagenum", String.valueOf(trainPageNum));
            btnStart.setEnabled(true);
            btnEnd.setEnabled(false);
            btnNext.setEnabled(true);
            finishDataCollection();
        }
    };

    private View.OnClickListener btnNextOnlick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            Log.e("Listener", "btnNextOnlick");

            //버튼 초기화
            btnStart.setEnabled(true);
            btnEnd.setEnabled(false);
            btnNext.setEnabled(false);

            if (trainPageNum == 0){
                Toast.makeText(getApplicationContext(),"you're not in the train steps!! ", Toast.LENGTH_SHORT);

            }
            else{
                if(trainPageNum < 5){
                    if(trainPageNum == 1){
                        tr1Layout.setVisibility(View.INVISIBLE);
                        tr2Layout.setVisibility(View.VISIBLE);
                    }
                    else if(trainPageNum == 2){
                        tr2Layout.setVisibility(View.INVISIBLE);
                        tr3Layout.setVisibility(View.VISIBLE);
                        ImageView trImg3 = (ImageView) findViewById(R.id.trImg3);
                        Glide.with(MainActivity.this).load(R.raw.stretch3).into(trImg3);

                    }
                    else if(trainPageNum == 3){
                        tr3Layout.setVisibility(View.INVISIBLE);
                        tr4Layout.setVisibility(View.VISIBLE);
                        ImageView trImg4 = (ImageView) findViewById(R.id.trImg4);
                        Glide.with(MainActivity.this).load(R.raw.stretch4).into(trImg4);
                    }
                    else if(trainPageNum == 4){
                        tr4Layout.setVisibility(View.INVISIBLE);
                        tr5Layout.setVisibility(View.VISIBLE);
                        ImageView trImg5 = (ImageView) findViewById(R.id.trImg5);
                        Glide.with(MainActivity.this).load(R.raw.stretch5).into(trImg5);
                    }
                    trainPageNum ++;

                }
                else if(trainPageNum == 5) {
                    tr5Layout.setVisibility(View.INVISIBLE);
                    trainfixedLayout.setVisibility(View.INVISIBLE);
                    //초기화면으로 돌아가기.
                    Toast.makeText(getApplicationContext(),"Train finished! Good Job ", Toast.LENGTH_LONG);
                    initLayout.setVisibility(View.VISIBLE);
                    backspace.setVisibility(View.INVISIBLE);
                    trainPageNum = 0;

                }
            }

            Log.e("pagenum", String.valueOf(trainPageNum));


        }

    };



    private View.OnClickListener btnStartCollectingDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "startBuildingModel()");

            String classLabelInput = etClassLabelForModel.getText().toString();
            if(classLabelInput == null || classLabelInput.equals("")){
                Toast.makeText(MainActivity.this, "Class label is required for collecting data", Toast.LENGTH_SHORT).show();
            } else {
                boolean isValidClassLabel = false;
                for(String classLabel : Constants.CLASS_LABELS){
                    if(classLabel.equals(classLabelInput)){
                        isValidClassLabel = true;
                        break;
                    }
                }

                if(isValidClassLabel){
                    etClassLabelForModel.setEnabled(false);
                    startDataCollection(classLabelInput);
                } else {
                    Toast.makeText(MainActivity.this, "Class label should be one of the predefined classes", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private View.OnClickListener btnFinishCollectingDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "finishBuildingModel()");
            etClassLabelForModel.setEnabled(true);
            finishDataCollection();
        }
    };



    private View.OnClickListener btnStartTestingModelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "startTestingModel()");

            String outputFileName = etOutputFileName.getText().toString();
            if (outputFileName == null || outputFileName.equals("")) {
                Toast.makeText(MainActivity.this, "Output file name is required for testing a model", Toast.LENGTH_SHORT).show();
            } else {
                startModelTest(Constants.ARFF_FILE_NAMES, outputFileName);
            }
        }
    };

    private View.OnClickListener btnFinishTestingModelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "finishTestingModel()");
            String outputFileName = etOutputFileName.getText().toString();
            finishModelTest(outputFileName);
            etOutputFileName.setEnabled(true);
        }
    };

    /*
     * Highlight the button specified in argument, if argument is null, the buttons turn to initial condition
     */
    public void highlightButton(Button btn) {

        if(btn == null){ // Initial states
            btnStartCollectingData.setEnabled(true);
            btnStartTestingModel.setEnabled(true);
            btnFinishCollectingData.setEnabled(false);
            btnFinishTestingModel.setEnabled(false);
        } else { // Highlight
            btnStartCollectingData.setEnabled(false);
            btnStartTestingModel.setEnabled(false);
            btnFinishCollectingData.setEnabled(false);
            btnFinishTestingModel.setEnabled(false);
            btn.setEnabled(true);
        }
    }

    public void startDataCollection(String label){
        //highlightButton(btnFinishCollectingData);
        sensorDataClassifier.clear();
        sensorDataHandler.setClassLabel(label);
        sensorDataHandler.start();
    }

    public void finishDataCollection(){
        //highlightButton(null);
        Log.e("finishDataCollection()","in the area");
        sensorDataHandler.stop();
        // Save raw data
        sensorDataHandler.saveRawDataToCSV();

        String label = sensorDataHandler.getClassLabel();
        //sensorDataClassifier.saveInstancesToArff(sensorDataClassifier.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");
        sensorDataClassifier.saveInstancesToArff(sensorDataClassifier.getInstances(),  label + ".txt");
        Log.e("instances :  ", String.valueOf(sensorDataClassifier.getInstances()));
        Log.e("sensorDataClassifier","file saved");
        // Cleaning
        //btnStartCollectingData.setEnabled(true);
        //btnFinishCollectingData.setEnabled(false);
    }

    public void startModelTest(String[] arffFileNames, String outputFileName) {
        try {
            highlightButton(btnFinishTestingModel);

            sensorDataHandler.setClassLabel(null); //class label 초기화
            sensorDataClassifier.setClassifier(arffFileNames); //해당 동작에 필요한 모델 설정 및 train

            sensorDataHandler.start(); //데이터 수집 시작

            //tvLog.setText("");
        } catch (FileNotFoundException e) {
            sensorDataHandler.stop();
            highlightButton(null);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveTestResultToFile(String outputFileName, String content) { //데이터 저장
        // Writing summary if output is set
        if(outputFileName != null){
            // Set output file for writing result
            String filePath = Environment.getExternalStorageDirectory() + "/" + Constants.WORKING_DIR_NAME + "/" + Constants.PREFIX_RESULT + System.currentTimeMillis() + "_" + outputFileName + ".txt";
            FileWriter fw = null;
            try {
                fw = new FileWriter(filePath);
                Log.i(TAG, "Output file writer is open!");

                fw.write(content);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG, "setOutputFileName() error : " + e.getMessage());
            }
        }
    }

    public void finishModelTest(String outputFileName){
        highlightButton(null);

        sensorDataHandler.stop(); //센서 데이터 입력 실시간으로 멈춤

        // Save raw data
        sensorDataHandler.saveRawDataToCSV(); //각각 gyro, accel 에대해서 txt 파일 저장

        // Save calculated feature sets
        String label = sensorDataHandler.getClassLabel();
        sensorDataClassifier.saveInstancesToArff(sensorDataClassifier.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");


        //String resultCrossValidation = mainRunnable.getResultCrossValidation();
        String resultTest = sensorDataClassifier.getResultTest(); //
        Log.e("Result","file saved!");
        Log.e("Result","outputFileName:" +outputFileName+"resultTest"+resultTest);
        saveTestResultToFile(outputFileName, /*resultCrossValidation + */"\n" + resultTest);

        // Cleaning
        btnStartCollectingData.setEnabled(true);
        btnFinishCollectingData.setEnabled(false);
    }
}