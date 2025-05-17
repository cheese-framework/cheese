package coco.cheese.mlkit;

import com.google.mlkit.vision.text.Text;

public interface MlkitCallback {
    void onSuccess(Text result);
    void onFailure(Exception e);
}
