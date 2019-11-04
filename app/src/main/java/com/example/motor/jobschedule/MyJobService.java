package com.example.motor.jobschedule;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.motor.doubleprocess.LocalService;
import com.example.motor.doubleprocess.RemoteService;

/**
 * 上面的jobschedule包，这是通过jobschedule进行进程保活的方法。
 *     JobScheduler允许在特定状态与特定时间间隔周期执行任务。可以利用它的这个特点完成保活的功能，
 * 效果类似开启一个定时器，与普通定时器不同的是其调度由系统完成。它是在Android5.0之后推出的，在5.0之前
 * 无法使用。首先写一个Service类继承自JobService，在小于7.0的系统上，JobInfo可以周期性的执行，
 * 但是在7.0以上的系统上，不能周期性的执行了。因此可以在JobService的onStartJob回调方法中继续开启一个
 * 任务来执行。
 */
@SuppressLint("NewApi")
public class MyJobService extends JobService {
    private static final String TAG = "MyJobService";

    public static void startJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(10, new
                ComponentName(context.getPackageName(),
                MyJobService.class.getName())).setPersisted(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // 如果小于5.0，就直接返回
            return;
        } else if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
            // 如果 5.0< 目标 <7.0,每隔1s 执行一次 job
            builder.setPeriodic(1_000);
        } else {
            // 如果大于7.0延迟执行任务
            builder.setMinimumLatency(1_000);
        }

        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "start job schedule");
        // 如果7.0以上 轮询
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startJob(this);
        }

        // JobSchedule结合双进程守护
        boolean isLocalRun = ProcessUtils.isRunningService(this, LocalService.class.getName());
        boolean isRemoteRun = ProcessUtils.isRunningService(this, RemoteService.class.getName());
        if (!isLocalRun || !isRemoteRun) {
            startService(new Intent(this, LocalService.class));
            startService(new Intent(this, RemoteService.class));
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
