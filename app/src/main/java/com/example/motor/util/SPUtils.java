package com.example.motor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对SharedPreference文件中的各种类型的数据进行存取操作
 */
public class SPUtils {

    private static final String TAG = "debug_SPUtils";
    private static SharedPreferences sp;
    private static final String SP_NAME = "cache";

    private static void init(Context context) {
        if (sp == null) {
            // 建立一个cache的偏好文件
            sp = context.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            // sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
    }

    public static void saveIntData(Context context, String key, int value) {
        if (sp == null) {
            init(context);
        }
        sp.edit().putInt(key, value).commit();
    }

    public static int getIntData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getInt(key, 0);
    }

    public static void savelongData(Context context, String key, long value) {
        if (sp == null) {
            init(context);
        }
        sp.edit().putLong(key, value).commit();
    }

    public static long getlongData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getLong(key, 0l);
    }

    public static void saveFloatData(Context context, String key,
                                     float value) {
        if (sp == null) {
            init(context);
        }
        sp.edit().putFloat(key, value).commit();
    }

    public static Float getFloatData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getFloat(key, 0f);
    }

    public static void saveBooleanData(Context context, String key,
                                       boolean value) {
        if (sp == null) {
            init(context);
        }
        sp.edit().putBoolean(key, value).commit();
    }

    public static Boolean getBooleanData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getBoolean(key, false);
    }

    public static void saveStringData(Context context, String key, String value) {
        if (sp == null) {
            init(context);
        }
        sp.edit().putString(key, value).commit();
    }

    public static void removeData(Context context, String key) {
        if (sp != null) {
            sp.edit().remove(key).commit();
        }
    }

    public static String getStringData(Context context, String key) {
        // 初始化sp偏好文件
        if (sp == null) {
            init(context);
        }
        return sp.getString(key, "");
    }

    public static int saveObject(Context context, String key, Object object) {
        if (sp == null) {
            init(context);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(object.toString().length());
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            String objectVal = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(key, objectVal);
            edit.commit();
            return 1;
        } catch (IOException e) {
            Log.e(TAG, "saveObject:  = " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.getClass();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static <T> T getObject(Context context, String key, Class<T> clazz) {
        if (sp == null) {
            init(context);
        }
        if (sp.contains(key)) {
            String objectVal = sp.getString(key, null);
            byte[] objectArray = Base64.decode(objectVal, Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(objectArray);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
                T t = (T) ois.readObject();
                return t;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                    if (bais != null) {
                        bais.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}