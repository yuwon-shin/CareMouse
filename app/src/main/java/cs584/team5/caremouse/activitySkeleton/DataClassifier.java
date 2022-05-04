package cs584.team5.caremouse.activitySkeleton;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cs584.team5.caremouse.activitySkeleton.classifierWrapper.ClassifierWrapper;
import cs584.team5.caremouse.activitySkeleton.classifierWrapper.J48Wrapper;
import cs584.team5.caremouse.activitySkeleton.sensorProc.DataInstanceList;
import cs584.team5.caremouse.activitySkeleton.sensorProc.FeatureGenerator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Created by mtjddnr on 2016. 5. 18..
 */
public class DataClassifier implements SensorDataHandler.DataAdaptor {

    final static String TAG = "DataClassifier";


    private Instances instancesForDataCollection = FeatureGenerator.createEmptyInstances(Constants.LIST_FEATURES, true); // makeClassLabel);

    private ClassifierWrapper classifier = null;
    private ArrayList<String> classificationResultList;


    @Override //check
    // 데이터 입력 받고, 판별하고 프린트까지 해주는 역할
    public void slidingWindowData(String classLabel, DataInstanceList dlAcc, DataInstanceList dlGyro) {
        //윈도우 단위로 데이터를 입력 받고 판별함.

        // Calculate features (without class label)
        HashMap<String, Float> featureMapAcc = FeatureGenerator.processAcc(dlAcc);
        HashMap<String, Float> featureMapGyro = FeatureGenerator.processGyro(dlGyro);

        // Generating header
        if (instancesForDataCollection == null) {
            instancesForDataCollection = FeatureGenerator.createEmptyInstances(Constants.LIST_FEATURES, true); // makeClassLabel);
        }

        // Aggregate features in single Weka instance
        int attributeSize = featureMapAcc.size() + featureMapAcc.size() + 1;
             Instance instance = new Instance(attributeSize); // including class classLabel

        // Filling features for accelerometer
        for (String feature : featureMapAcc.keySet()) {
                float value = featureMapAcc.get(feature);
                Attribute attr = instancesForDataCollection.attribute(feature);
                instance.setValue(attr, value);
        }

        // Filling features for gyroscope
        for(String feature : featureMapGyro.keySet()){
            float value = featureMapGyro.get(feature);
            Attribute attr = instancesForDataCollection.attribute(feature);
            instance.setValue(attr, value);
        }

        // Adding class attribute
        Attribute attrClass = instancesForDataCollection.attribute(Constants.HEADER_CLASS_LABEL);
        instancesForDataCollection.setClass(attrClass);
        if (classLabel != null) {
            instance.setValue(attrClass, classLabel); // Only for labeled data when collecting data
        }

        // Add generated Instance
        instancesForDataCollection.add(instance); // Final calculated feature set
        Log.i(TAG, "Instance added : " + instancesForDataCollection.numInstances());

        // Classify a instance if classifier is ready by setClassifier()
        if (classifier == null) return;

        try {
            // Store the result in ArrayList
            if (classificationResultList == null) {
                classificationResultList = new ArrayList<String>();
            }

            // Use header information of data collection
            instance.setDataset(instancesForDataCollection);
            //Log.e(TAG, "ClassifierWrapper : " + classifier);

            final String resultClass = classifier.predict(instance); //예측

            classificationResultList.add(/*instance + "," + */resultClass);

            Log.i(TAG, "Classified as : " + resultClass);

            if (eventHandler != null) {
                eventHandler.onClassified(resultClass); //결과 프린트.. 날짜 :  class 이름
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Classification error : " + e.getMessage());

            classificationResultList.add("Classification error");
        }
    }

    public String getResultTest(){
        StringBuilder sb = new StringBuilder();
        sb.append("********** Test Result **********\n");

        for(String result : classificationResultList){
            sb.append(result + "\n");
        }

        return sb.toString();

    }

    public Instances getInstances() {
        return instancesForDataCollection;
    }

    public void saveInstancesToArff(Instances instances, String fileName) {

        try {
            String dirPath = Environment.getExternalStorageDirectory() + "/" + Constants.WORKING_DIR_NAME;
            String filePath = dirPath + "/" + fileName;

            File dirFile = new File(dirPath);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }
            /*
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            saver.setFile(new File(filePath));
            saver.writeBatch();
            Log.i(TAG, "Arff saved : " + filePath);
             */
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(instances.toString());
            writer.flush();
            writer.close();
            Log.i(TAG, "Arff saved : " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "saveInstancesToArff() error : " + e.getMessage());
        }
    }

    public void setClassifier(String[] arffFileNames) throws FileNotFoundException {
        // Load training data
        Instances instancesForTraining = ClassifierWrapper.loadInstancesFromArffFile(arffFileNames[0]);
        Log.e("arffFilenames[0] : ", arffFileNames[0]);
        Log.e("arffFilenames.length : ", String.valueOf(arffFileNames.length));
        for (int i = 1; i < arffFileNames.length; i++) { // From second Instances
            Instances instances = ClassifierWrapper.loadInstancesFromArffFile(arffFileNames[i]);
            Log.e("checkpoint","1");
            Log.e("numofInstances", String.valueOf(instances.numInstances()));
            for (int j = 0; j < instances.numInstances(); j++) {
                Instance instance = instances.instance(j);
                //Log.e("instance : ", String.valueOf(instance));
                instancesForTraining.add(instance);
            }
        }
        Log.e("Result", "training instances"+String.valueOf(instancesForTraining));

        // Build a classifier and set it to global classifier variable
        {
            ClassifierWrapper classifierTmp = new J48Wrapper();
            //ClassifierWrapper classifierTmp = new LibLinearWrapper();
            classifierTmp.train(instancesForTraining);
            classifier = classifierTmp;
        }
    }

    public void clear() {
        classifier = null;
        classificationResultList = null;
        instancesForDataCollection = FeatureGenerator.createEmptyInstances(Constants.LIST_FEATURES, true);
    }

    public interface EventHandler {
        public void onClassified(String resultClass);
    }
    public EventHandler eventHandler;
}
