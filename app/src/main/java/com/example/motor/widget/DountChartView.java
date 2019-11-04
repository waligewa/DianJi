package com.example.motor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.example.motor.db.ChronicleInfo;

import org.xclcharts.chart.DountChart;
import org.xclcharts.chart.PieData;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.plot.PlotLegend;
import org.xclcharts.view.ChartView;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DountChartView extends ChartView implements Runnable {// dount 圆环图   pie 馅饼

    private String TAG = "DountChartView";
    private DountChart chart = new DountChart();
    private int sum;
    private List<ChronicleInfo> infos;
    LinkedList<PieData> lPieData = new LinkedList<PieData>();
    // RGB颜色数组
    private final int arrColorRgb[][] = {
            {79, 67, 143},
            {185, 68, 123},
            {248, 137, 117},
            {253, 173, 124},
            /*{249, 232, 118},此为黄色*/
            {27, 188, 232},
            {115, 213, 238},
            {79, 67, 143},
            {184, 67, 122},
            {248, 137, 117},
            {252, 172, 123},
            /*{248, 232, 116},*/
            {26, 188, 232},
            {114, 212, 237},
            {248, 137, 117},
            {28, 188, 229}};

    public DountChartView(Context context, int sum, List<ChronicleInfo> infos) {
        super(context);
        this.sum = sum;
        this.infos = infos;
        initView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //图所占范围大小   range 范围
        chart.setChartRange(w, h);
    }

    private void initView() {
        try {
            // 那些百分之多少数字所在的位置 标签风格labelstyle  slice  切下 划分
            chart.setLabelStyle(XEnum.SliceLabelStyle.INSIDE);
            // 那些百分之多少数字的颜色
            chart.getLabelPaint().setColor(Color.WHITE);
            // 显示key  plot地基 legend图例
            chart.getPlotLegend().show();
            // 显示图例 enum枚举  column圆柱
            PlotLegend legend = chart.getPlotLegend();
            legend.setType(XEnum.LegendType.COLUMN);          // colunm是列  row是行
            // 设置图例位置 水平对齐                             left左 right右 CENTER中间
            legend.setHorizontalAlign(XEnum.HorizontalAlign.RIGHT);
            // 设置图例位置 垂直对齐                             top顶部  Middle中间  BOTTOM底部
            legend.setVerticalAlign(XEnum.VerticalAlign.TOP);
            // 表示图例有个背景盒子
            legend.showBox();
            //  border边境  rect矩形  transparent透明的
            legend.getBox().setBorderRectType(XEnum.RectType.RECT);
            legend.getBox().setBorderLineColor(Color.TRANSPARENT);
            legend.getBox().getBackgroundPaint().setColor(Color.TRANSPARENT);
            // 图背景色
            chart.setApplyBackgroundColor(false);
            // 内环背景色
            chart.getInnerPaint().setColor(0xFFFFFFFF);
            // 显示边框线，并设置其颜色  arc  弧
            // chart.getArcBorderPaint().setColor(Color.rgb(25, 216, 239));
            chart.getArcBorderPaint().setColor(Color.BLUE);
            // 可用这个修改环所占比例
            chart.setInnerRadius(0.5f);
            // 保存标签位置   LabelSaveType 有3个选项  ALL  NONE  ONLYPOSITION 最后一个选择之后百分之多少就没有了
            chart.saveLabelsPosition(XEnum.LabelSaveType.ALL);
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                // 这是设置曲线图离着上下左右的距离
                chart.setPadding(50, 50, 100, 50);
                chart.setCenterText("故障总数:\n" + sum);
                // 图例的隐藏与显示，show是显示，hide是隐藏
                legend.show();
                Random random = new Random();
                HashSet<Integer> integerHashSet = new HashSet<Integer>();
                double percent;
                for (int i = 0; i < infos.size(); i++) {
                    // 设置图表数据源
                    int randomInt = random.nextInt(arrColorRgb.length);

                    percent = (new BigDecimal(((double) infos.get(i).getNum() / sum) * 100)
                            .setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
                    //lPieData.add(new PieData(infos.get(i).getEventMessage(), percent + "%", percent, 0xff000000 | random.nextInt(0x00ffffff)));
                    lPieData.add(new PieData(infos.get(i).getEventMessage(), percent + "%", percent,
                            Color.rgb(arrColorRgb[randomInt][0], arrColorRgb[randomInt][1], arrColorRgb[randomInt][2])));
                }
            } else {
                // 这是设置曲线图离着上下左右的距离
                chart.setPadding(0, 20, 200, 20);
                // 图例的隐藏与显示，show是显示，hide是隐藏
                //legend.hide();
                Random random = new Random();
                double percent;
                for (int i = 0; i < infos.size(); i++) {
                    // 设置图表数据源
                    int randomInt = random.nextInt(arrColorRgb.length);

                    percent = (new BigDecimal(((double) infos.get(i).getNum() / sum) * 100)
                            .setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
                    lPieData.add(new PieData(infos.get(i).getEventMessage(), "", percent,
                            Color.rgb(arrColorRgb[randomInt][0], arrColorRgb[randomInt][1], arrColorRgb[randomInt][2])));
                }
                legend.setType(XEnum.LegendType.COLUMN);
            }
            //激活点击监听
            chart.ActiveListenItemClick();
            chart.showClikedFocus();

            new Thread(this).start();  //这个线程的启动会导致run()的运行
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
        }
    }

    @Override
    //render 提供
    public void render(Canvas canvas) {
        try {
            chart.render(canvas);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void run() {
        try {
            chartAnimation();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    private void chartAnimation() {
        try {
            chart.setDataSource(lPieData);
            int count = 360 / 10;
            for (int i = 1; i < count; i++) {
                Thread.sleep(40);               //线程休眠40毫秒
                chart.setTotalAngle(10 * i);
                //激活点击监听
                if (count - 1 == i) {
                    chart.setTotalAngle(360);//angle角
                    chart.ActiveListenItemClick();
                    //显示边框线，并设置其颜色  0x19D8EF
                    //chart.getArcBorderPaint().setColor(Color.rgb(25, 216, 239));
                    chart.getArcBorderPaint().setColor(Color.WHITE);
                    chart.getArcBorderPaint().setStrokeWidth(2);
                }
                postInvalidate();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}
