package com.example.motor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.example.motor.db.ChronicleInfo;

import org.xclcharts.chart.BarChart;
import org.xclcharts.chart.BarData;
import org.xclcharts.common.IFormatterDoubleCallBack;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.view.ChartView;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.xclcharts.renderer.XEnum.PanMode.HORIZONTAL;

public class BarChartView extends ChartView implements Runnable {

    private String TAG = "AnimationBar01View";
    private BarChart chart = new BarChart();

    // 标签轴
    private List<String> chartLabels = new LinkedList<String>();
    private List<BarData> chartData = new LinkedList<BarData>();

    private int max;
    private List<ChronicleInfo> infos;
    // RGB颜色数组
    private final int arrColorRgb[][] = {
            {79, 67, 143},
            {185, 68, 123},
            {248, 137, 117},
            {253, 173, 124},
            {249, 232, 118},
            {27, 188, 232},
            {115, 213, 238},
            {79, 67, 143},
            {184, 67, 122},
            {248, 137, 117},
            {252, 172, 123},
            {248, 232, 116},
            {26, 188, 232},
            {114, 212, 237},
            {248, 137, 117},
            {28, 188, 229}};
    // 这个类的构造方法
    public BarChartView(Context context, int max, List<ChronicleInfo> infos) {
        super(context);
        this.max = max;
        this.infos = infos;
        initView();
    }

    private void initView() {
        chartDataSet();
        chartRender();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 图所占范围大小
        chart.setChartRange(w, h);
    }

    private void chartRender() { // render 提供
        try {
            // 设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....
            chart.setPadding(100, 50, 50, 70);
            // 显示边框   加上下面这一句就会出现一个黑色的框，不加就消失，我感觉还是不加为好
            //chart.showRoundBorder();
            // 数据源
            chart.setDataSource(chartData);
            chart.setCategories(chartLabels);
            // 数据轴 axis轴
            //chart.getDataAxis().setAxisMax(sum);
            chart.getDataAxis().setAxisMax(getMax());
            chart.getDataAxis().setAxisMin(0);
            int digit = String.valueOf(max).length();
            // Math.pow()    求乘方运算
            if (max / Math.pow(10, digit - 1) > 2) {
                chart.getDataAxis().setAxisSteps(Math.pow(10, digit - 1));
            } else {//pow  脑袋
                chart.getDataAxis().setAxisSteps(Math.pow(10, digit - 2));
            }
            // 图背景色
            chart.setApplyBackgroundColor(false);
            // 轴颜色
            chart.getDataAxis().getAxisPaint().setColor(Color.rgb(246, 133, 39));//  Y轴
            chart.getCategoryAxis().getAxisPaint().setColor(Color.rgb(246, 133, 39));//  X轴
            //  tick  在（纸）上打钩
            chart.getDataAxis().getTickMarksPaint().setColor(Color.rgb(246, 133, 39));//  Y轴上的勾
            chart.getCategoryAxis().getTickMarksPaint().setColor(Color.rgb(246, 133, 39));//  X轴上的勾
            chart.getDataAxis().getTickLabelPaint().setColor(Color.BLACK);//  Y轴上的标签颜色
            chart.getCategoryAxis().getTickLabelPaint().setColor(Color.BLACK);//  X轴上的标签颜色
            chart.getAxisTitle().getLeftTitlePaint().setColor(Color.BLACK);// Y轴的标题颜色
            chart.getAxisTitle().getLowerTitlePaint().setColor(Color.BLACK);// X轴的标题颜色
            //  以下两句代码是每一根柱子上面的数字的颜色和字体大小
            chart.getBar().getItemLabelPaint().setColor(Color.rgb(246, 133, 39));
            chart.getBar().getItemLabelPaint().setTextSize(15);

            //轴标题
            chart.getAxisTitle().setLeftTitle("发生次数");
            chart.getAxisTitle().setLowerTitle("故障类型");

            //定义数据轴标签显示格式
            chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack() {

                @Override
                public String textFormatter(String value) {
                    // TODO Auto-generated method stub
                    Double tmp = Double.parseDouble(value);
                    DecimalFormat df = new DecimalFormat("#0");
                    String label = df.format(tmp).toString();
                    return (label);
                }
            });

            //在柱形顶部显示值
            chart.getBar().setItemLabelVisible(true);
            //设定格式
            chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
                @Override
                public String doubleFormatter(Double value) {
                    // TODO Auto-generated method stub
                    DecimalFormat df = new DecimalFormat("#0");
                    String label = df.format(value).toString();
                    return label;
                }
            });
            chart.enablePanMode();
            chart.setPlotPanMode(HORIZONTAL);
            //让柱子间不留空白，你如果写上10000.0f也不会有太大变化，只是比0.0f缩小了一点
            chart.getBar().setBarInnerMargin(0.0f);
            //隐藏Key
            chart.getPlotLegend().show();
            //   chart.disableHighPrecision();
            new Thread(this).start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
        }
    }

    private void chartDataSet() {
        List<Double> dataSeries = new LinkedList<Double>();//series  系列
        List<Integer> dataColor = new LinkedList<Integer>();
        Random random = new Random();
        HashSet<Integer> integerHashSet = new HashSet<Integer>();
        for (int i = 0; i < infos.size(); i++) {
            int randomInt = random.nextInt(arrColorRgb.length);
            //如果HashSet里面包含randomInt，就把randomInt添加到HashSet中去
            if (!integerHashSet.contains(randomInt)) {
                integerHashSet.add(randomInt);
            } else {
                randomInt = random.nextInt(arrColorRgb.length);
            }
            dataSeries.add((double) infos.get(i).getNum());
            //  dataColor.add(0xff000000 | random.nextInt(0x00ffffff));
            dataColor.add(Color.rgb(arrColorRgb[randomInt][0], arrColorRgb[randomInt][1],
                    arrColorRgb[randomInt][2]));
            //chartLabels就是X轴下面的各种标签
            chartLabels.add(infos.get(i).getEventMessage());
        }
        //表格数据添加上  数据数字  柱子的颜色  数据数字的颜色
        chartData.add(new BarData("", dataSeries, dataColor,
                Color.rgb(arrColorRgb[random.nextInt(arrColorRgb.length)][0],
                        arrColorRgb[random.nextInt(arrColorRgb.length)][1],
                        arrColorRgb[random.nextInt(arrColorRgb.length)][2])));
    }

    private Integer getMax() {
        int num = 0;
        int digit = String.valueOf(max).length();    // digit  数字
        //  % 是求余运算   / 是普通的除号
        if ((max / Math.pow(10, digit - 2)) % 10 > 5) {
            num = (int) (((int) (max / Math.pow(10, digit - 1)) + 1) * Math.pow(10, digit - 1));
            if ((max / Math.pow(10, digit - 2)) % 10 > 7) {
                num = (int) (((int) (num / Math.pow(10, digit - 2)) + 2) * Math.pow(10, digit - 2));
            }
        } else {
            if ((max / Math.pow(10, digit - 1)) % 10 > 2) {
                num = (int) (((int) (max / Math.pow(10, digit - 2)) + 2) * Math.pow(10, digit - 2));
            } else {
                num = (int) (((int) (max / Math.pow(10, digit - 2)) + 1) * Math. pow(10, digit - 2));
            }
        }
        return num;
    }

    @Override
    public void render(Canvas canvas) {
        try {
            chart.render(canvas);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void run() {
        // while如打开，会循环显示动画
        // while(!Thread.currentThread().isInterrupted()) {
        try {
            chartAnimation();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        // } //end while
    }

    private void chartAnimation() {
        try {
            chart.getDataAxis().hide();
            for (int i = 8; i > 0; i--) {
                Thread.sleep(100);
                chart.setPadding(100, 50, 50, 70);
                if (1 == i) {
                    chart.getDataAxis().show();
                }
                postInvalidate();  // invalidate  无效的
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}

