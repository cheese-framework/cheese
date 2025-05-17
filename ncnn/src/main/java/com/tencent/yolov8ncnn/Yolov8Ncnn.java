// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
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

package com.tencent.yolov8ncnn;

import android.content.res.AssetManager;
import android.util.Log;
import android.view.Surface;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Yolov8Ncnn
{

    public native Obj[] Detect(Bitmap bitmap,String name,String[] list,int modelid, int cpugpu);

    static {
        System.loadLibrary("ncnn_runtime");
    }

    public static class Obj
    {
        public Obj(com.tencent.yolov8ncnn.Yolov8Ncnn yolov8Ncnn) {
            // Initialize any fields or use the yolov8Ncnn object as needed
        }
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
        public float speed;
        public float num;
    }
    public static double speed;


}
