// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
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

package com.tencent.paddleocrncnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class PaddleOCRNcnn
{
    public native boolean Init(AssetManager mgr);
    public native boolean InitSd(String modelDir);

    public class Obj
    {
        public float x0;
        public float y0;
        public float x1;
        public float y1;
        public float x2;
        public float y2;
        public float x3;
        public float y3;
        public String label;
        public float prob;

        public void print() {
            Log.d("Obj", String.format("x0=%.2f, y0=%.2f", x0, y0));
            Log.d("Obj", String.format("x1=%.2f, y1=%.2f", x1, y1));
            Log.d("Obj", String.format("x2=%.2f, y2=%.2f", x2, y2));
            Log.d("Obj", String.format("x3=%.2f, y3=%.2f", x3, y3));
            Log.d("Obj", "label=" + label + ", prob=" + prob);
        }
    }



    public class Obj1 {
        public float left;
        public float top;
        public float right;
        public float bottom;
        public String text;
        public float prob;
    }

    public  Obj1 convertToObj1(Obj obj) {
        Obj1 result = new Obj1();

        // 计算四个顶点的最小/最大坐标
        float left = Math.min(Math.min(obj.x0, obj.x1), Math.min(obj.x2, obj.x3));
        float top = Math.min(Math.min(obj.y0, obj.y1), Math.min(obj.y2, obj.y3));
        float right = Math.max(Math.max(obj.x0, obj.x1), Math.max(obj.x2, obj.x3));
        float bottom = Math.max(Math.max(obj.y0, obj.y1), Math.max(obj.y2, obj.y3));

// 创建Rect或RectF
        RectF rect = new RectF(left, top, right, bottom);

//        obj.print();

        result.left = rect.left;
        result.top = rect.top;
        result.right = rect.right;
        result.bottom = rect.bottom;

        result.text = obj.label;
        result.prob = obj.prob;

        return result;
    }

    public  Obj1[] convertToObj1Array(Obj[] objs) {
        if (objs == null) return null;

        Obj1[] result = new Obj1[objs.length];
        for (int i = 0; i < objs.length; i++) {
            result[i] = convertToObj1(objs[i]);
        }
        return result;
    }
    
    

    private  float min(float[] values) {
        float min = values[0];
        for (float v : values) {
            if (v < min) min = v;
        }
        return min;
    }

    private  float max(float[] values) {
        float max = values[0];
        for (float v : values) {
            if (v > max) max = v;
        }
        return max;
    }

    public Obj1[] ocr(Bitmap bitmap){

        Obj1[] results =convertToObj1Array( Detect(bitmap,false)) ;
//        for (int i = 0; i < results.length; i++) {
//            Obj1 obj = results[i];
//            System.out.println("位置 " + i);
//            System.out.println("  文本: " + obj.text);
//            System.out.println("  概率: " + obj.prob);
//            System.out.println("  边界: left=" + obj.left + ", top=" + obj.top + ", right=" + obj.right + ", bottom=" + obj.bottom);
//        }

      return  results ;
    }


    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu);

    static {
        System.loadLibrary("ncnn_runtime");
    }
}
