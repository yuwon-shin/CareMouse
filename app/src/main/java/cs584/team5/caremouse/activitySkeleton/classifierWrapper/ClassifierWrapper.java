package cs584.team5.caremouse.activitySkeleton.classifierWrapper;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;

import cs584.team5.caremouse.activitySkeleton.Constants;
import weka.core.Instance;
import weka.core.Instances;

public abstract class ClassifierWrapper {

    protected final static String TAG = "ClassifierWrapper";
    protected Instances instancesForTraining = null;

    public Instances getInstances(){
        return instancesForTraining;
    }

    public abstract void train(Instances instances);

    public abstract String predict(Instance instance);

    public static Instances loadInstancesFromArffFile(String fileName) throws FileNotFoundException {
        String dirPath =
                Environment.getExternalStorageDirectory()
                        + "/"
                        + Constants.WORKING_DIR_NAME;
        String filePath = dirPath + "/" + fileName;

        if (new File(filePath).exists() == false) throw new FileNotFoundException(fileName + " not exists");

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader(filePath));
            Instances data = new Instances(reader);
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);
            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "loadInstancesFromArffFile() error : " + e.getMessage());
        }

        return null;
    }

}
