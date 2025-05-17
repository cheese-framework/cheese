#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2024 THL A29 Limited, a Tencent company. All rights reserved.
//
// Copyright (C) 2024 whyb(https://github.com/whyb). All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

// ReadMe
// Convert yolov8 model to ncnn model workflow:
//
// step 1:
// If you don't want to train the model yourself. You should go to the ultralytics website download the pretrained model file.
// original pretrained model from https://docs.ultralytics.com/models/yolov8/#supported-tasks-and-modes
//
// step 2:
// run this command.
// conda create --name yolov8 python=3.11
// conda activate yolov8
// pip install ultralytics onnx numpy protobuf
//
// step 3:
// save source code file(export_model_to_ncnn.py):
// from ultralytics import YOLO
// detection_models = [
//     ["./Detection-pt/yolov8n.pt", "./Detection-pt/"],
//     ["./Detection-pt/yolov8s.pt", "./Detection-pt/"],
//     ["./Detection-pt/yolov8m.pt", "./Detection-pt/"],
//     ["./Detection-pt/yolov8l.pt", "./Detection-pt/"],
//     ["./Detection-pt/yolov8x.pt", "./Detection-pt/"]
// ]
// for model_dict in detection_models:
//     model = YOLO(model_dict[0])  # load an official pretrained weight model
//     model.export(format="ncnn", dynamic=True, save_dir=model_dict[1], simplify=True)
//
// step 4:
// run command: python export_model_to_ncnn.py


//#include <opencv2/opencv.hpp>
//#include <opencv2/highgui/highgui.hpp>
//#include <opencv2/opencv.hpp>
//
//struct CPoint {
//    double x;
//    double y;
//    double similarity;
//
//    CPoint(double x, double y, double similarity) : x(x), y(y), similarity(similarity) {}
//};

//using namespace cv;
//using namespace std;

//int main(){
//    Mat img=imread("luffy.jpg");
//    Mat temp=imread("luffy_face.png");
//    if(img.empty()||temp.empty()){
//
//        return -1;
//    }
//    Mat result;
//    matchTemplate(img,temp,result,TM_CCOEFF_NORMED);//模板匹配
//    double maxVal,minVal;
//    Point minLoc,maxLoc;
//    //寻找匹配结果中的最大值和最小值以及坐标位置
//    minMaxLoc(result,&minVal,&maxVal,&minLoc,&maxLoc);
//    //回执最佳匹配结果
//    rectangle(img,Rect(maxLoc.x,maxLoc.y,temp.cols,temp.rows),Scalar(0,0,255),2);
//    imshow("原图",img);
//    imshow("模板图像",temp);
//    imshow("result",result);
//    waitKey(0);
//    return 0;
//}
//CPoint* findImgByResize(
//        const cv::Mat& bigImageMat,
//        const cv::Mat& smallImageMat,
//        double similarityThreshold,
//        int width,
//        int height
//) {
//    // 检查图像是否为空
//    if (bigImageMat.empty() || smallImageMat.empty()) {
//        return nullptr;
//    }
//    // 获取大图的原始尺寸
//    int originalHeight = bigImageMat.rows;
//    int originalWidth = bigImageMat.cols;
//
//    // 指定缩放后的分辨率
//    cv::Size dim(width, height);
//
//    // 缩放大图
//    cv::Mat resizedBigImage;
//    cv::resize(bigImageMat, resizedBigImage, dim, 0.0, 0.0, cv::INTER_AREA);
//
//    // 模板匹配
//    cv::Mat result;
//    cv::matchTemplate(resizedBigImage, smallImageMat, result, cv::TM_CCOEFF_NORMED);
//
//    // 获取匹配结果中的最大值及其位置
//    double minVal, maxVal;
//    cv::Point minLoc, maxLoc;
//    cv::minMaxLoc(result, &minVal, &maxVal, &minLoc, &maxLoc);
//
//    if (maxVal < similarityThreshold) return nullptr;
//
//    // 获取小图的尺寸
//    int smallImageHeight = smallImageMat.rows;
//    int smallImageWidth = smallImageMat.cols;
//
//    // 计算匹配位置的右下角坐标
//    cv::Point bottomRight(maxLoc.x + smallImageWidth, maxLoc.y + smallImageHeight);
//
//    // 将坐标转换回原始大图中的坐标
//    double scaleX = static_cast<double>(originalWidth) / width;
//    double scaleY = static_cast<double>(originalHeight) / height;
//
//    // 计算原始大图中的坐标
//    cv::Point originalTopLeft(maxLoc.x * scaleX, maxLoc.y * scaleY);
//    cv::Point originalBottomRight(bottomRight.x * scaleX, bottomRight.y * scaleY);
//
//    // 计算原始大图中匹配区域的中心点
//    cv::Point originalCenter(
//            (originalTopLeft.x + originalBottomRight.x) / 2.0,
//            (originalTopLeft.y + originalBottomRight.y) / 2.0
//    );
//
//    return new CPoint(originalCenter.x, originalCenter.y, maxVal);
//}
//
//
//CPoint* fastFindImg(
//        const cv::Mat& bigImageMat,
//        const cv::Mat& smallImageMat,
//        double similarityThreshold
//) {
//    // 检查尺寸是否有效
//    if (bigImageMat.empty() || smallImageMat.empty()) return nullptr;
//    if (bigImageMat.rows < smallImageMat.rows || bigImageMat.cols < smallImageMat.cols) return nullptr;
//
//    // 模板匹配
//    cv::Mat result;
//    cv::matchTemplate(bigImageMat, smallImageMat, result, cv::TM_CCOEFF_NORMED);
//
//    // 获取匹配结果
//    double minVal, maxVal;
//    cv::Point minLoc, maxLoc;
//    cv::minMaxLoc(result, &minVal, &maxVal, &minLoc, &maxLoc);
//
//    if (maxVal < similarityThreshold) return nullptr;
//
//    // 计算匹配位置
//    double centerX = maxLoc.x + smallImageMat.cols / 2.0;
//    double centerY = maxLoc.y + smallImageMat.rows / 2.0;
//
//    return new CPoint(centerX, centerY, maxVal);
//}


#include <memory>
#include <vector>
#include <algorithm>
#include "layer.h"
#include "net.h"

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <float.h>
#include <stdio.h>

#define MAX_STRIDE 32

struct Object
{
    cv::Rect_<float> rect;
    int label;
    float prob;
};

static inline float intersection_area(const Object& a, const Object& b)
{
    cv::Rect_<float> inter = a.rect & b.rect;
    return inter.area();
}

static void qsort_descent_inplace(std::vector<Object>& objects, int left, int right)
{
    int i = left;
    int j = right;
    float p = objects[(left + right) / 2].prob;

    while (i <= j)
    {
        while (objects[i].prob > p)
            i++;

        while (objects[j].prob < p)
            j--;

        if (i <= j)
        {
            // swap
            std::swap(objects[i], objects[j]);

            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(objects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(objects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector<Object>& objects)
{
    if (objects.empty())
        return;

    qsort_descent_inplace(objects, 0, objects.size() - 1);
}

static void nms_sorted_bboxes(const std::vector<Object>& faceobjects, std::vector<int>& picked, float nms_threshold, bool agnostic = false)
{
    picked.clear();

    const int n = faceobjects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++)
    {
        areas[i] = faceobjects[i].rect.area();
    }

    for (int i = 0; i < n; i++)
    {
        const Object& a = faceobjects[i];

        int keep = 1;
        for (int j = 0; j < (int)picked.size(); j++)
        {
            const Object& b = faceobjects[picked[j]];

            if (!agnostic && a.label != b.label)
                continue;

            // intersection over union
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[picked[j]] - inter_area;
            // float IoU = inter_area / union_area
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.push_back(i);
    }
}

static inline float sigmoid(float x)
{
    return static_cast<float>(1.f / (1.f + exp(-x)));
}

static inline float clampf(float d, float min, float max)
{
    const float t = d < min ? min : d;
    return t > max ? max : t;
}

static void parse_yolov8_detections(
        float* inputs, float confidence_threshold,
        int num_channels, int num_anchors, int num_labels,
        int infer_img_width, int infer_img_height,
        std::vector<Object>& objects)
{
    std::vector<Object> detections;
    cv::Mat output = cv::Mat((int)num_channels, (int)num_anchors, CV_32F, inputs).t();

    for (int i = 0; i < num_anchors; i++)
    {
        const float* row_ptr = output.row(i).ptr<float>();
        const float* bboxes_ptr = row_ptr;
        const float* scores_ptr = row_ptr + 4;
        const float* max_s_ptr = std::max_element(scores_ptr, scores_ptr + num_labels);
        float score = *max_s_ptr;
        if (score > confidence_threshold)
        {
            float x = *bboxes_ptr++;
            float y = *bboxes_ptr++;
            float w = *bboxes_ptr++;
            float h = *bboxes_ptr;

            float x0 = clampf((x - 0.5f * w), 0.f, (float)infer_img_width);
            float y0 = clampf((y - 0.5f * h), 0.f, (float)infer_img_height);
            float x1 = clampf((x + 0.5f * w), 0.f, (float)infer_img_width);
            float y1 = clampf((y + 0.5f * h), 0.f, (float)infer_img_height);

            cv::Rect_<float> bbox;
            bbox.x = x0;
            bbox.y = y0;
            bbox.width = x1 - x0;
            bbox.height = y1 - y0;
            Object object;
            object.label = max_s_ptr - scores_ptr;
            object.prob = score;
            object.rect = bbox;
            detections.push_back(object);
        }
    }
    objects = detections;
}

static int detect_yolov8(const cv::Mat& bgr,const char* name,int num_labels, std::vector<Object>& objects)
{
    ncnn::Net yolov8;

    yolov8.opt.use_vulkan_compute = false; // if you want detect in hardware, then enable it
    char parampath[256];
    char modelpath[256];
    sprintf(parampath, "%s/yolo.param", name);
    sprintf(modelpath, "%s/yolo.bin", name);
    yolov8.load_param(parampath);
    yolov8.load_model(modelpath);

    const int target_size = 640;
    const float prob_threshold = 0.2f;
    const float nms_threshold = 0.45f;

    int img_w = bgr.cols;
    int img_h = bgr.rows;

    // letterbox pad to multiple of MAX_STRIDE
    int w = img_w;
    int h = img_h;
    float scale = 1.f;
    if (w > h)
    {
        scale = (float)target_size / w;
        w = target_size;
        h = h * scale;
    }
    else
    {
        scale = (float)target_size / h;
        h = target_size;
        w = w * scale;
    }

    ncnn::Mat in = ncnn::Mat::from_pixels_resize(bgr.data, ncnn::Mat::PIXEL_BGR2RGB, img_w, img_h, w, h);

    int wpad = (target_size + MAX_STRIDE - 1) / MAX_STRIDE * MAX_STRIDE - w;
    int hpad = (target_size + MAX_STRIDE - 1) / MAX_STRIDE * MAX_STRIDE - h;
    ncnn::Mat in_pad;
    ncnn::copy_make_border(in, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2, ncnn::BORDER_CONSTANT, 114.f);

    const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
    in_pad.substract_mean_normalize(0, norm_vals);

    ncnn::Extractor ex = yolov8.create_extractor();

    ex.input("in0", in_pad);

    std::vector<Object> proposals;

    // stride 32
    {
        ncnn::Mat out;
        ex.extract("out0", out);

        std::vector<Object> objects32;

        parse_yolov8_detections(
                (float*)out.data, prob_threshold,
                out.h, out.w, num_labels,
                in_pad.w, in_pad.h,
                objects32);
        proposals.insert(proposals.end(), objects32.begin(), objects32.end());
    }

    // sort all proposals by score from highest to lowest
    qsort_descent_inplace(proposals);

    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(proposals, picked, nms_threshold);

    int count = picked.size();

    objects.resize(count);
    for (int i = 0; i < count; i++)
    {
        objects[i] = proposals[picked[i]];

        // adjust offset to original unpadded
        float x0 = (objects[i].rect.x - (wpad / 2)) / scale;
        float y0 = (objects[i].rect.y - (hpad / 2)) / scale;
        float x1 = (objects[i].rect.x + objects[i].rect.width - (wpad / 2)) / scale;
        float y1 = (objects[i].rect.y + objects[i].rect.height - (hpad / 2)) / scale;

        // clip
        x0 = std::max(std::min(x0, (float)(img_w - 1)), 0.f);
        y0 = std::max(std::min(y0, (float)(img_h - 1)), 0.f);
        x1 = std::max(std::min(x1, (float)(img_w - 1)), 0.f);
        y1 = std::max(std::min(y1, (float)(img_h - 1)), 0.f);

        objects[i].rect.x = x0;
        objects[i].rect.y = y0;
        objects[i].rect.width = x1 - x0;
        objects[i].rect.height = y1 - y0;
    }

    return 0;
}

static void draw_objects(const cv::Mat& bgr, const std::vector<Object>& objects)
{
    static const char* class_names[] = {
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
            "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
            "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
            "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
            "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
            "hair drier", "toothbrush"
    };

    static const unsigned char colors[19][3] = {
            {54, 67, 244},
            {99, 30, 233},
            {176, 39, 156},
            {183, 58, 103},
            {181, 81, 63},
            {243, 150, 33},
            {244, 169, 3},
            {212, 188, 0},
            {136, 150, 0},
            {80, 175, 76},
            {74, 195, 139},
            {57, 220, 205},
            {59, 235, 255},
            {7, 193, 255},
            {0, 152, 255},
            {34, 87, 255},
            {72, 85, 121},
            {158, 158, 158},
            {139, 125, 96}
    };

    int color_index = 0;

    cv::Mat image = bgr.clone();

    for (size_t i = 0; i < objects.size(); i++)
    {
        const Object& obj = objects[i];

        const unsigned char* color = colors[color_index % 19];
        color_index++;

        cv::Scalar cc(color[0], color[1], color[2]);

        fprintf(stderr, "%d = %.5f at %.2f %.2f %.2f x %.2f\n", obj.label, obj.prob,
                obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);

        cv::rectangle(image, obj.rect, cc, 2);

        char text[256];
        sprintf(text, "%s %.1f%%", class_names[obj.label], obj.prob * 100);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

        int x = obj.rect.x;
        int y = obj.rect.y - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > image.cols)
            x = image.cols - label_size.width;

        cv::rectangle(image, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                      cc, -1);

        cv::putText(image, text, cv::Point(x, y + label_size.height),
                    cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(255, 255, 255));
    }

    cv::imshow("image", image);
    cv::waitKey(0);
}
#define TAG "NcnnYolov8"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 全局变量
jobjectArray globalStringArray = nullptr;
static jclass objCls = NULL;
static jmethodID constructortorId;
static jfieldID xId;
static jfieldID yId;
static jfieldID wId;
static jfieldID hId;
static jfieldID labelId;
static jfieldID probId;
static jfieldID msid;
static jfieldID objectsid;

// 转换 Java 字符串数组为 C 字符串数组
void convertJObjectArrayToCharArray(JNIEnv* env, jobjectArray stringArray, const char* class_names[]) {
    jsize length = env->GetArrayLength(stringArray);
    jstring javaString;
    const char* utfString;

    // 确保 class_names 数组已正确分配内存，可能需要根据 length 进行动态分配
    for (int i = 0; i < length; i++) {
        javaString = (jstring)env->GetObjectArrayElement(stringArray, i);
        utfString = env->GetStringUTFChars(javaString, nullptr);
        class_names[i] = strdup(utfString);  // 复制字符串
        env->ReleaseStringUTFChars(javaString, utfString);
    }

    // 释放分配的内存
    for (int i = 0; i < length; i++) {
        free((void*)class_names[i]);
    }
}

// 将 Android Bitmap 转换为 OpenCV Mat
cv::Mat bitmapToMat(JNIEnv* env, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels = nullptr;

    // 获取 Bitmap 信息
    int ret = AndroidBitmap_getInfo(env, bitmap, &info);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "NcnnYolov8", "Failed to get bitmap info");
        return cv::Mat();
    }

    // 创建 OpenCV Mat，行列数据和宽高对应
    cv::Mat mat(info.height, info.width, CV_8UC4); // Assuming ARGB_8888

    // 锁定 Bitmap，获取像素数据
    ret = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "NcnnYolov8", "Failed to lock bitmap pixels");
        return cv::Mat();
    }

    // 将像素数据从 Bitmap 复制到 OpenCV Mat
    memcpy(mat.data, pixels, info.height * info.stride);

    // 解锁 Bitmap
    AndroidBitmap_unlockPixels(env, bitmap);

    // 转换为 BGR 格式（如果需要）
    cv::cvtColor(mat, mat, cv::COLOR_RGBA2BGR);

    return mat;
}

// 初始化 JNI 类和方法 ID
void initJNI(JNIEnv* env) {
    jclass localObjCls = env->FindClass("com/tencent/yolov8ncnn/Yolov8Ncnn$Obj");
    objCls = reinterpret_cast<jclass>(env->NewGlobalRef(localObjCls));
    constructortorId = env->GetMethodID(objCls, "<init>", "(Lcom/tencent/yolov8ncnn/Yolov8Ncnn;)V");
    xId = env->GetFieldID(objCls, "x", "F");
    yId = env->GetFieldID(objCls, "y", "F");
    wId = env->GetFieldID(objCls, "w", "F");
    hId = env->GetFieldID(objCls, "h", "F");
    labelId = env->GetFieldID(objCls, "label", "Ljava/lang/String;");
    probId = env->GetFieldID(objCls, "prob", "F");
    msid = env->GetFieldID(objCls, "speed", "F");
    objectsid = env->GetFieldID(objCls, "num", "F");
}
extern "C" {
// Yolov8 检测函数
JNIEXPORT jobjectArray JNICALL Java_com_tencent_yolov8ncnn_Yolov8Ncnn_Detect(
        JNIEnv* env, jobject thiz, jobject bitmap, jstring name, jobjectArray list, jint modelid, jint cpugpu)
 {


    jsize length = env->GetArrayLength(list);
    globalStringArray = (jobjectArray)env->NewGlobalRef(list);


    __android_log_print(ANDROID_LOG_DEBUG, "NcnnYolov8", "长度: %d", length);

    // 检测开始时间
    double start_time = ncnn::get_current_time();
    bool use_gpu = (int)cpugpu == 1;
    std::vector<Object> objects;
     LOGD("Converting Bitmap to Mat...");
     cv::Mat rgb = bitmapToMat(env, bitmap);
     if (rgb.empty()) {
         LOGE("Bitmap conversion to Mat failed");
         return nullptr;
     }
     LOGD("Bitmap successfully converted to Mat");

//    AndroidBitmapInfo info;
//    AndroidBitmap_getInfo(env, bitmap, &info);
//    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
//        return NULL;
//
//    // Android Bitmap转ncnn::Mat
//    ncnn::Mat mat = ncnn::Mat::from_android_bitmap(env, bitmap, ncnn::Mat::PIXEL_RGB);
//
//    cv::Mat rgb(mat.h, mat.w, CV_8UC3);
//    for (int c = 0; c < 3; c++) {
//        for (int i = 0; i < mat.h; i++) {
//            for (int j = 0; j < mat.w; j++) {
//                float t = ((float *) mat.data)[j + i * mat.w + c * mat.h * mat.w];
//                rgb.data[(2 - c) + j * 3 + i * mat.w * 3] = t;
//            }
//        }
//    }

    // 将 Bitmap 转换为 Mat 并进行检测
    detect_yolov8(rgb , env->GetStringUTFChars(name, 0), length, objects);

    // 初始化 JNI
    initJNI(env);

    __android_log_print(ANDROID_LOG_DEBUG, "NcnnYolov8", "objects num: %d", length);

    const char* class_names[length];
    convertJObjectArrayToCharArray(env, globalStringArray, class_names);

    jobjectArray jObjArray = env->NewObjectArray(objects.size(), objCls, NULL);
    __android_log_print(ANDROID_LOG_DEBUG, "NcnnYolov8", "objects num: %d", objects.size());

    for (size_t i = 0; i < objects.size(); i++) {
        jobject jObj = env->NewObject(objCls, constructortorId, thiz);
        env->SetFloatField(jObj, xId, objects[i].rect.x);
        env->SetFloatField(jObj, yId, objects[i].rect.y);
        env->SetFloatField(jObj, wId, objects[i].rect.width);
        env->SetFloatField(jObj, hId, objects[i].rect.height);
        env->SetFloatField(jObj, probId, objects[i].prob);

        // 设置标签
        const char* labelStr = class_names[objects[i].label];
        env->SetObjectField(jObj, labelId, env->NewStringUTF(labelStr));
        env->SetObjectArrayElement(jObjArray, i, jObj);
    }

    // 计算检测时间
    double elapsed = ncnn::get_current_time() - start_time;
    jclass javaClass = env->FindClass("com/tencent/yolov8ncnn/Yolov8Ncnn");
    if (javaClass != NULL) {
        jfieldID fieldId = env->GetStaticFieldID(javaClass, "speed", "D");
        if (fieldId != NULL) {
            env->SetStaticDoubleField(javaClass, fieldId, elapsed);
        }
    }

    __android_log_print(ANDROID_LOG_DEBUG, "NcnnYolov8", "%.2fms  detect", elapsed);

    // 清理全局引用
    env->DeleteGlobalRef(globalStringArray);
    env->DeleteGlobalRef(objCls);

    return jObjArray;
}
}