package com.example.motor.util;

import androidx.core.content.FileProvider;

/**
 * 
 * Author : 赵彬彬
 * Date   : 2020/4/4
 * Time   : 17:21
 * Desc   : 继承自FileProvider，避免自动安装apk的适配7.0时provider的冲突（在manifest中引用）
 */
public class MyProvider extends FileProvider { }
