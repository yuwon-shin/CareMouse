package cs584.team5.caremouse.activitySkeleton;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import cs584.team5.caremouse.activitySkeleton.sensorProc.DataInstance;
import cs584.team5.caremouse.activitySkeleton.sensorProc.DataInstanceList;
import cs584.team5.caremouse.activitySkeleton.sensorProc.SlidingWindow;


/**
 * Created by mtjddnr on 2016. 5. 18..
 */
public class SensorDataHandler {

    final static String TAG = "SensorDataHandler";

    private Context context;
    private SensorManager sensorManager;

    StringBuilder stringBuilder = new StringBuilder();

    private boolean running;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private String classLabel; // Optional value, only necessary for data collection
    private DataInstanceList dlAcc, dlGyro = new DataInstanceList(); // For raw data save purpose
    private SlidingWindow slidingWindowAcc, slidingWindowGyro; // For extracting samples by window

    public SensorDataHandler(Context context) {
        this.context = context;
        // Don't have to receive sensor data from SensorManager
        // this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);


        this.dlAcc = new DataInstanceList();
        this.dlGyro = new DataInstanceList();

        // Sliding window
        slidingWindowAcc = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);
        slidingWindowGyro = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);

        //Background Thread
        sensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    public String getClassLabel() {
        return this.classLabel;
    }
    public void setClassLabel(final String classLabel) {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                SensorDataHandler.this.classLabel = classLabel;
            }
        });
    }

    public void clearData() {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                dlAcc = new DataInstanceList();
                dlGyro = new DataInstanceList();
            }
        });
    }

    public void start() {
        if (running) return;
        running = true;

        // No data from sensorManager
        // sensorManager.registerListener(eventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), Constants.SENSOR_DELAY, sensorHandler);
        // sensorManager.registerListener(eventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), Constants.SENSOR_DELAY, sensorHandler);
    }

    public void stop() {
        if (!running) return;
        running = false;
        // sensorManager.unregisterListener(eventListener);
    }

    public void saveRawDataToCSV() {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {

                Log.v("saveRawDataToCSV", "saveRawDataToCSV start!");

                String fileNameAcc = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + classLabel + "_acc.txt";
                dlAcc.saveToCsvFile(fileNameAcc);

                String fileNameGyro = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + classLabel + "_gyro.txt";
                dlGyro.saveToCsvFile(fileNameGyro);

                Log.v("saveRawDataToCSV", "saveRawDataToCSV end!");
            }
        });
    }

    public void onSensorChanged(String data){
        // assert (Looper.getMainLooper().getThread() != Thread.currentThread()) : "Should not run on main thread";

        System.out.println("Parsing: status: " + running);
        if (running == false){
            return;
        }
        stringBuilder.append(data);
        int endOfIndex = stringBuilder.indexOf("~"); //파싱 시작 #(데이터)~
        if (endOfIndex > 0) {
            String sensorDataPrint = stringBuilder.substring(0, endOfIndex);
            if (sensorDataPrint.contains("#")) {
                try {
                    System.out.println("Parsing: Data Received = "+sensorDataPrint);
                    int lengthOfData = sensorDataPrint.length();
                    System.out.println("Parsing: Length of data = "+String.valueOf(lengthOfData));

                    int startOfIndex = sensorDataPrint.indexOf("#");
                    String startString = sensorDataPrint.substring(startOfIndex+1);

                    String[] tempString = startString.split(":");

                    float[] acc_values = new float[3];

                    String[] accRawValues = tempString[0].split(",");
                    acc_values[0] = Float.parseFloat(accRawValues[0]);
                    acc_values[1] = Float.parseFloat(accRawValues[1]);
                    acc_values[2] = Float.parseFloat(accRawValues[2]);

                    float acc_x = acc_values[0];
                    float acc_y = acc_values[1];
                    float acc_z = acc_values[2];

                    DataInstance diAcc = new DataInstance(System.currentTimeMillis(), acc_values);
                    diAcc.setLabel(classLabel); // Optional field
                    dlAcc.add(diAcc); // Save for raw data backup
                    slidingWindowAcc.input(diAcc);

                    Log.d(TAG, "Acc : " + acc_x + "," + acc_y + "," + acc_z);

                    float[] gyro_values = new float[3];

                    String[] gyroRawValues = tempString[1].split(",");
                    gyro_values[0] = Float.parseFloat(gyroRawValues[0]);
                    gyro_values[1] = Float.parseFloat(gyroRawValues[1]);
                    gyro_values[2] = Float.parseFloat(gyroRawValues[2]);

                    float gyro_x = gyro_values[0];
                    float gyro_y = gyro_values[1];
                    float gyro_z = gyro_values[2];

                    DataInstance diGyro = new DataInstance(System.currentTimeMillis(), gyro_values);
                    diGyro.setLabel(classLabel); // Optional field
                    dlGyro.add(diGyro); // Save for raw data backup
                    slidingWindowGyro.input(diGyro);

                    Log.d(TAG, "Gyro : " + gyro_x + "," + gyro_y + "," + gyro_z);

                } catch (Exception e) {

                }
            }
            stringBuilder .delete(0,endOfIndex+1);
        }
        processWindowBuffer();
    }

    /*
    private SensorEventListener eventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            assert (Looper.getMainLooper().getThread() != Thread.currentThread()) : "Should not run on main thread";

            if (running == false) return;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: {
                    float[] values = event.values;
                    float x = values[0];
                    float y = values[1];
                    float z = values[2];

                    DataInstance diAcc = new DataInstance(System.currentTimeMillis(), values);
                    diAcc.setLabel(classLabel); // Optional field
                    dlAcc.add(diAcc); // Save for raw data backup
                    slidingWindowAcc.input(diAcc);

                    //Log.d(TAG, "Acc : " + x + "," + y + "," + z);
                    break;
                }
                case Sensor.TYPE_GYROSCOPE: {
                    float[] values = event.values;
                    float x = values[0];
                    float y = values[1];
                    float z = values[2];

                    DataInstance diGyro = new DataInstance(System.currentTimeMillis(), values);
                    diGyro.setLabel(classLabel); // Optional field
                    dlGyro.add(diGyro); // Save for raw data backup
                    slidingWindowGyro.input(diGyro);

                    //Log.d(TAG, "Gyro : " + x + "," + y + "," + z);
                    break;
                }
            }
            processWindowBuffer();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
     */

    public interface DataAdaptor {
        public void slidingWindowData(String classLabel, DataInstanceList dlAcc, DataInstanceList dlGyro);
    }

    public DataAdaptor dataAdaptor;

    private void processWindowBuffer() {
        if (slidingWindowAcc.getHeadTimeId() != slidingWindowGyro.getHeadTimeId()) {
            if (slidingWindowAcc.getHeadTimeId() < slidingWindowGyro.getHeadTimeId()) {
                slidingWindowAcc.removeFirst();
            } else {
                slidingWindowGyro.removeFirst();
            }
        }
        if (!slidingWindowAcc.isBufferReady() || !slidingWindowGyro.isBufferReady()) return;

        // Fetching a slices of sliding window
        DataInstanceList dlAcc = slidingWindowAcc.output();
        DataInstanceList dlGyro = slidingWindowGyro.output();

        if (dlAcc == null || dlGyro == null) return;

        Log.i(TAG, dlAcc.getTimeId() + ", " + dlGyro.getTimeId());

        if (dlAcc.getTimeId() != dlGyro.getTimeId()) {
            Log.e(TAG, "Sample are not synced!"); // Issue : What if not synced (Very rare case) => Ignored
            return;
        }

        if (dataAdaptor == null) return;
        dataAdaptor.slidingWindowData(this.classLabel, dlAcc, dlGyro);
    }
}
