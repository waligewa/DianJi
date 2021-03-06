package com.example.motor.adapter;

import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.db.TaskItemBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author : yanftch
 * Date : 2018/3/21
 * Time : 12:55
 * Desc : 待办任务适配器
 *
 */

public class TaskRvAdapter extends RecyclerView.Adapter<TaskRvAdapter.TaskViewHolder> {

    private static final String TAG = "zbb_TaskRvAdapter";
    private Context mContext;
    private List<TaskItemBean> mTaskItemBeanList;
    private LayoutInflater mLayoutInflater;
    private boolean MultipleChoice = false;
    private String string2;

    public TaskRvAdapter(Context context, List<TaskItemBean> taskItemBeanList) {
        mContext = context;
        mTaskItemBeanList = taskItemBeanList;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    /**
     * 重置所有选中状态为未选中
     */
    public void resetSelectedStatus() {
        for (TaskItemBean bean : mTaskItemBeanList) {
            bean.setMultipleChoiceSelected(false);
        }
    }

    /**
     * 获取当前选中的Item
     */
    public List<TaskItemBean> getCurrentChoosenItems() {
        List<TaskItemBean> temp = new ArrayList<>();

        if (mTaskItemBeanList == null || mTaskItemBeanList.isEmpty()) {
            return null;
        }

        for (int i = 0; i < mTaskItemBeanList.size(); i++) {
            if (mTaskItemBeanList.get(i).isMultipleChoiceSelected()) {
                temp.add(mTaskItemBeanList.get(i));
            }
        }

        return temp;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskViewHolder(mLayoutInflater.inflate(R.layout.task_rv_item_layout,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {
        // TODO: 2018/3/21 赋值
        // TODO: 2018/3/21 赋值
        // TODO: 2018/3/21 赋值
        String userId = String.valueOf(mContext.getSharedPreferences("UserInfo", 0)
                .getInt("UserID", 0));
        String username = mContext.getSharedPreferences("UserInfo", 0)
                .getString("username", "");
        holder.tvLeftNo.setText(position + 1 + "");// 这个是子项的左上角的数量标记
        final TaskItemBean bean = mTaskItemBeanList.get(position);
        if (null == bean) return;
        holder.tvTitle.setText(TextUtils.isEmpty(bean.getWOTitle()) ? "标题：" + "" : "标题：" +
                bean.getWOTitle());// 标题的赋值操作
        holder.tvDesc.setText(TextUtils.isEmpty(bean.getWOIssuedDate()) ? "时间：" + "" :
                "时间：" + (bean.getWOIssuedDate().split("T")[0] + " " +
                        bean.getWOIssuedDate().split("T")[1].substring(0, 8)));// 第二行信息的赋值操作
        holder.tvInfo.setText(TextUtils.isEmpty(bean.getWOContent()) ? "内容：" + "" : "内容：" +
                bean.getWOContent());// 第三行描述的赋值操作
        holder.tvWoid.setText(TextUtils.isEmpty(bean.getWOID()) ? "工单id：" + "" : "工单id：" +
                bean.getWOID());// 第四行WOID的赋值操作
        //Log.e(TAG, bean.getWOContent());
        // 如果WOState是true的话就让已处理里面（处理该任务）按钮消失，如果WOState是false的话就让（处理该任务）按钮存在
        // 第一个是让已处理的处理该任务按钮消失，第三个是让已转发的处理该任务按钮消失
        if(!TextUtils.isEmpty(bean.getWOState()) && bean.getWOState().equals("true")){
            holder.tvItemRightBtn.setVisibility(View.GONE);// 处理该任务按钮消失
        } else if(!TextUtils.isEmpty(bean.getWOState()) && bean.getWOState().equals("false") &&
                !bean.getWOIssuedUser().equals(userId)) {
            holder.tvItemRightBtn.setVisibility(View.VISIBLE);// 处理该任务按钮显示
        } else if(!TextUtils.isEmpty(bean.getWOState()) && bean.getWOState().equals("false") &&
                bean.getWOIssuedUser().equals(userId)){
            holder.tvItemRightBtn.setVisibility(View.GONE);// 处理该任务按钮消失
        }
        // 以前的时候是把这两个Date放在成员变量的位置，导致时间得不到更新，现在放在这个位置为了可以
        // 实时刷新时间，每次刷新一次就能刷新一次时间
        Date date1 = new Date();
        Date date2 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 显示摘要最左边的TextView在执行中的计算时间问题。算出它的持续时间，得到WOIssuedDate时间，将T
        // 给剔除
        try{
            date2 = simpleDateFormat.parse(bean.getWOIssuedDate().split("T")[0]);
        }catch (ParseException e){
            e.printStackTrace();
        }
        // 显示摘要最左边的TextView赋值
        if(bean.getWOState().equals("true")){
            holder.tvTime.setText("已处理完成");
        } else if(bean.getWOState().equals("false")){
            long millisecond = date1.getTime() - date2.getTime();// 得到毫秒数
            long day = millisecond / (1000 * 60 * 60 * 24);// 得到天数
            long hour = (millisecond - day*(1000 * 60 * 60 * 24))/(1000* 60 * 60);// 得到小时数
            // 得到分钟数
            long minute = (millisecond - day*(1000 * 60 * 60 * 24) - hour*(1000* 60 * 60))/(1000* 60);
            if(day == 0){
                holder.tvTime.setText("已持续" + hour + "小时" + minute + "分钟");
            } else {
                holder.tvTime.setText("已持续" + day + "天" + hour + "小时" + minute + "分钟");
            }
        }

        // 扩展开的3条摘要的赋值
        // 已处理
        if(bean.getWOType().equals("1") &&
                bean.getWOState().equals("true")){
            holder.tvExtDesc.setText("接收人：" + bean.getReceiveUser());
            if(!TextUtils.isEmpty(bean.getWOItemsNum()) && !TextUtils.isEmpty(bean.getWOPerformNum())){
                holder.tvExtCompany.setText("已处理工单所占比例：" + (Float.valueOf(
                        bean.getWOPerformNum()) / Float.valueOf(bean.getWOItemsNum()) * 100) + "%");
            } else {
                holder.tvExtCompany.setVisibility(View.GONE);
            }
            holder.tvExtPerson.setText("设备名称：" + bean.getDeviceName());
        }
        // 已转发
        if(bean.getWOType().equals("1") &&
                bean.getWOState().equals("false") &&
                bean.getWOIssuedUser().equals(userId)){
            holder.tvExtDesc.setText("转发情况：已转发");
            holder.tvExtCompany.setText("转发人：" + username);
            holder.tvExtPerson.setText("设备名称：" + bean.getDeviceName());
        }
        // 执行中
        if(bean.getWOType().equals("1") &&
                bean.getWOState().equals("false") &&
                !bean.getWOIssuedUser().equals(userId)){
            holder.tvExtDesc.setText("工单中设备总数量：" + bean.getWOItemsNum());
            holder.tvExtCompany.setText("工单中设备完成数量：" + bean.getWOPerformNum());
            holder.tvExtPerson.setText("设备名称：" + bean.getDeviceName());
        }
        // 多选模式,如果是多选外加false（代表未完成）选择的红色圆圈显示，按钮容器消失，扩展信息容器消失
        // 如果是多选加true（代表已处理）选择的红色圆圈消失。如果是单选的话，选择的红色圆圈消失，
        // 按钮容器显示，扩展信息容器显示
        if (MultipleChoice) {
            // 执行中
            if(bean.getWOType().equals("1") &&
                    bean.getWOState().equals("false") &&
                    !bean.getWOIssuedUser().equals(userId)){
                holder.ivChoose.setVisibility(View.VISIBLE);
                holder.llBtnContainer.setVisibility(View.GONE);//按钮容器
                holder.llExtContainer.setVisibility(View.GONE);//扩展信息容器
            // 已转发
            } else if (bean.getWOType().equals("1") &&
                    bean.getWOState().equals("false") &&
                    bean.getWOIssuedUser().equals(userId)){
                holder.ivChoose.setVisibility(View.GONE);
                holder.llBtnContainer.setVisibility(View.VISIBLE);//按钮容器
            // 已处理
            } else if (bean.getWOState().equals("true")){
                holder.ivChoose.setVisibility(View.GONE);
                holder.llBtnContainer.setVisibility(View.VISIBLE);//按钮容器
            }
        } else {
            holder.ivChoose.setVisibility(View.GONE);
            holder.llBtnContainer.setVisibility(View.VISIBLE);//按钮容器
            holder.llExtContainer.setVisibility(View.VISIBLE);//扩展信息容器
        }

        if (bean.isMultipleChoiceSelected()) {
            holder.ivChoose.setImageResource(R.mipmap.ap_im_selected);
        } else {
            holder.ivChoose.setImageResource(R.mipmap.ap_im_unselected);
        }

        // 面板的显示与隐藏
        if (bean.isShowPanel()) {
            holder.llExtContainer.setVisibility(View.VISIBLE);
        } else {
            holder.llExtContainer.setVisibility(View.GONE);
        }

        holder.ivChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnImageClickEvent) {
                    mOnImageClickEvent.onImageClick(position);
                }
            }
        });

        /**
         * 容器点击事件
         */
        holder.baseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });

        /**
         * 左边按钮点击事件
         */
        holder.tvItemLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnLeftButtonClickEvent) {
                    mOnLeftButtonClickEvent.onLeftButtonClick(position);
                }
                if (bean.isShowPanel()) {
                    holder.llExtContainer.setVisibility(View.GONE);
                } else {
                    holder.llExtContainer.setVisibility(View.VISIBLE);
                }
                bean.setShowPanel(!bean.isShowPanel());
                notifyDataSetChanged();
            }
        });
        /**
         * 右边按钮点击
         */
        holder.tvItemRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onRightButtonClickEvent) {
                    onRightButtonClickEvent.onRightButtonClick(position);
                }
            }
        });
    }

    /**
     * @param isMultChoiceModel
     */
    public void changeModel(boolean isMultChoiceModel) {
        MultipleChoice = isMultChoiceModel;
        notifyDataSetChanged();
    }

    private void showInfoPanel(boolean show) {
        if (show) {}
    }

    @Override
    public int getItemCount() {
        return null == mTaskItemBeanList ? 0 : mTaskItemBeanList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivHeadImage;    //  子项中的图片
        private TextView tvLeftNo;    //  子项左上角的数字标记

        private TextView tvTitle;   // 标题
        private TextView tvDesc;    // 描述信息
        private TextView tvInfo;    // 从上往下第三条描述信息
        private TextView tvWoid;    // 从上往下第四条WOID
        private TextView tvTime;    // 两个按钮最左边的信息
        private TextView tvItemLeftBtn;   //  显示摘要按钮
        private TextView tvItemRightBtn;  //  处理该任务

        //扩展信息
        private TextView tvExtDesc;     // 扩展开的文字描述
        private TextView tvExtCompany;  // 扩展开的报修单位
        private TextView tvExtPerson;   // 扩展开的报修人
        //private TextView tvExtPhone;    // 扩展开的报修人电话

        private LinearLayout llBtnContainer;  //  按钮容器
        private LinearLayout llExtContainer;  //  扩展四条项目的容器

        private CardView baseContainer;       //  整个cardView
        //多选模式按钮
        private ImageView ivChoose;           //  可以进行选择的那个圆形图

        public TaskViewHolder(View itemView) {
            super(itemView);
            baseContainer = (CardView) itemView.findViewById(R.id.baseContainer);
            tvLeftNo = (TextView) itemView.findViewById(R.id.tvLeftNo);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDesc);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            tvWoid = (TextView) itemView.findViewById(R.id.tvWoid);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvItemLeftBtn = (TextView) itemView.findViewById(R.id.tvItemLeftBtn);
            tvItemRightBtn = (TextView) itemView.findViewById(R.id.tvItemRightBtn);
            tvExtDesc = (TextView) itemView.findViewById(R.id.tvExtDesc);
            tvExtCompany = (TextView) itemView.findViewById(R.id.tvExtCompany);
            tvExtPerson = (TextView) itemView.findViewById(R.id.tvExtPerson);
            //tvExtPhone = (TextView) itemView.findViewById(R.id.tvExtPhone);
            ivChoose = (ImageView) itemView.findViewById(R.id.ivChoose);
            llBtnContainer = (LinearLayout) itemView.findViewById(R.id.llBtnContainer);
            llExtContainer = (LinearLayout) itemView.findViewById(R.id.llExtContainer);
            //ivHeadImage = (CircleImageView) itemView.findViewById(R.id.ivHeadImage);
        }
    }

    /**
     * 点击事件
     */
    public interface onItemClickListener {
        void onItemClick(int position);
    }

    public interface onLeftButtonClickEvent {
        void onLeftButtonClick(int position);
    }

    public interface onRightButtonClickEvent {
        void onRightButtonClick(int position);
    }

    public interface onImageClickEvent {
        void onImageClick(int position);
    }

    public onItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(onItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public onLeftButtonClickEvent mOnLeftButtonClickEvent;
    // 这个是l不是1
    public void setOnLeftButtonClickEvent(onLeftButtonClickEvent l) {
        mOnLeftButtonClickEvent = l;
    }

    public onRightButtonClickEvent onRightButtonClickEvent;
    // 这个是l不是1
    public void setOnRightButtonClickEvent(onRightButtonClickEvent l) {
        onRightButtonClickEvent = l;
    }

    public onImageClickEvent mOnImageClickEvent;

    public void setOnImageClickEvent(onImageClickEvent imageClickEvent) {
        mOnImageClickEvent = imageClickEvent;
    }
}
