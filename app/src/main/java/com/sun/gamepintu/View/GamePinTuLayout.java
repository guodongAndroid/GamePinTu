package com.sun.gamepintu.View;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sun.gamepintu.CrimeCameraFragment;
import com.sun.gamepintu.R;
import com.sun.gamepintu.Utils.ImagePiece;
import com.sun.gamepintu.Utils.ImageSplitterUtil;

/**
 * @author Sun
 */
public class GamePinTuLayout extends RelativeLayout implements OnClickListener
{

    /**
     * 一行图片的个数，初始值为3
     */
    private int mColumn = 3;
    /**
     * 容器的内边距
     */
    private int mPadding;
    /**
     * 每张小图之间的距离，单位dp
     */
    private int mMargin = 3;
    /**
     * 所需图片数组
     */
    private ImageView[] mGameItems;
    /**
     * 每张小图的宽度
     */
    private int mItemWidth;
    /**
     * 游戏的完整图片
     */
    private Bitmap mBitmap;
    /**
     * 游戏面板的宽高
     */
    private int mWidth;
    /**
     * 动画层
     */
    private RelativeLayout mAnimLayout;
    /**
     * 图片List
     */
    private List<ImagePiece> mItemBitmaps;
    /**
     * 是否执行一次
     */
    private boolean once;
    /**
     * 是否正在进行切换图片动画
     */
    private boolean isAniming;
    /**
     * 是否成功
     */
    private boolean isGameSuccess;
    /**
     * 游戏是否结束
     */
    private boolean isGameOver;
    /**
     * 游戏是否处于暂停状态
     */
    private boolean isPause;

    /**
     * @author Sun 回调接口
     */
    public interface GamePinTuListener
    {

        void nextLevel(int nextLevel);

        void timeChanged(int currentTime);

        void gameOver();
    }

    public GamePinTuListener mGamePinTuListener;

    /**
     * 设置接口回调
     *
     * @param mGamePinTuListener
     */
    public void setOnGamePinTuListener(GamePinTuListener mGamePinTuListener)
    {
        this.mGamePinTuListener = mGamePinTuListener;
    }

    /**
     * 关卡
     */
    private int mLevel = 1;
    private static final int TIME_CHANGED = 0x110;
    private static final int NEXT_LEVEL = 0x111;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case TIME_CHANGED:
                    if (isGameSuccess || isGameOver || isPause)
                        return;

                    if (mGamePinTuListener != null)
                    {
                        mGamePinTuListener.timeChanged(mTime);
                        if (mTime == 0)
                        {
                            isGameOver = true;
                            mGamePinTuListener.gameOver();
                            return;
                        }
                    }

                    mTime--;
                    mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);

                    break;
                case NEXT_LEVEL:
                    mLevel++;
                    if (mGamePinTuListener != null)
                    {
                        mGamePinTuListener.nextLevel(mLevel);
                    }
                    else
                    {
                        nextLevel();
                    }
                    break;
            }
        }

        ;
    };
    /**
     * 是否开启时间`
     */
    private boolean isTimeEnabled = false;
    private int mTime;

    /**
     * 设置是否开启时间
     *
     * @param isTimeEnabled
     */
    public void setTimeEnabled(boolean isTimeEnabled)
    {
        this.isTimeEnabled = isTimeEnabled;
    }

    public GamePinTuLayout(Context context)
    {
        this(context, null);
    }

    public GamePinTuLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public GamePinTuLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取宽和高的小值
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (!once)
        {
            String path = CrimeCameraFragment.EXTRA_PHOTO_PATH + "/" + CrimeCameraFragment.EXTRA_PHOTO_NAME;
            mBitmap = BitmapFactory.decodeFile(path);
            if (mBitmap == null)
                initBitmap();

            setInitBitmap(mBitmap);

            initItem();

            checkTimeEnable();

            once = true;
        }
        // 强制设置容器为正方形
        setMeasuredDimension(mWidth, mWidth);

    }

    /**
     * 判读是否开启时间
     */
    private void checkTimeEnable()
    {

        if (isTimeEnabled)
        {
            // 根据当前等级设置时间
            countTimeBaseLevel();
            mHandler.sendEmptyMessage(TIME_CHANGED);
        }
    }

    /**
     * 根据当前等级设置时间
     */
    private void countTimeBaseLevel()
    {

        mTime = (int) Math.pow(2, mLevel) * 60;
    }

    /**
     * 进行切图并乱序
     */
    private void initBitmap()
    {

        if (mBitmap == null)
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qianqian);

        mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
        // 对每张小图进行乱序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>()
        {
            public int compare(ImagePiece lhs, ImagePiece rhs)
            {

                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    public void setInitBitmap(Bitmap bitmap)
    {
        this.removeAllViews();
        mAnimLayout = null;
        isGameSuccess = false;
        checkTimeEnable();
        mBitmap = bitmap;
        initBitmap();
        initItem();
    }

    /**
     * 设置ImageView的宽高等属性
     */
    private void initItem()
    {

        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;

        mGameItems = new ImageView[mColumn * mColumn];
        // 生成我们的Item,设置Rule
        for (int i = 0; i < mGameItems.length; i++)
        {

            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());

            mGameItems[i] = item;
            item.setId(i + 1);
            // 在Item的Tag中存储了index,方便下面切换图片时使用
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);

            // 设置Item之间的横向间隙，通过rightMargin
            // 不是最后一列
            if ((i + 1) % mColumn != 0)
            {
                lp.rightMargin = mMargin;
            }

            // 不是第一列
            if (i % mColumn != 0)
            {
                lp.addRule(RelativeLayout.RIGHT_OF, mGameItems[i - 1].getId());
            }

            // 如果不是第一行，设置topMargin和Rule
            if ((i + 1) > mColumn)
            {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW, mGameItems[i - mColumn].getId());
            }
            addView(item, lp);
        }

    }

    private void init()
    {

        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());

        mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());

    }

    /**
     * 获取多个参数的最小值
     *
     * @param paddingLeft
     * @param paddingRight
     * @param paddingTop
     * @param paddingBottom
     * @return
     */
    private int min(int... params)
    {

        int min = params[0];
        for (int param : params)
        {

            if (param < min)
            {
                min = param;
            }
        }
        return min;
    }

    /**
     * 选中的第一个图片
     */
    private ImageView mFirst;
    /**
     * 选中的第二个图片
     */
    private ImageView mSecond;

    @Override
    public void onClick(View v)
    {

        if (isAniming)
            return;

        // 取消选中的View
        if (mFirst == v)
        {

            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }

        if (mFirst == null)
        {

            mFirst = (ImageView) v;
            mFirst.setColorFilter(Color.parseColor("#55FF0000"));
        }
        else
        {

            mSecond = (ImageView) v;

            exchangView();
        }

    }

    /**
     * 切换图片
     */
    private void exchangView()
    {

        mFirst.setColorFilter(null);

        final String firstTag = (String) mFirst.getTag();
        final String secondTag = (String) mSecond.getTag();
        setUpAnimLayout();

        ImageView first = new ImageView(getContext());
        final Bitmap firstBitmap = mItemBitmaps.get(getImageIdByTag(firstTag)).getBitmap();
        first.setImageBitmap(firstBitmap);
        LayoutParams lpFirst = new LayoutParams(mItemWidth, mItemWidth);
        lpFirst.leftMargin = mFirst.getLeft() - mPadding;
        lpFirst.topMargin = mFirst.getTop() - mPadding;
        first.setLayoutParams(lpFirst);
        mAnimLayout.addView(first);

        ImageView second = new ImageView(getContext());
        final Bitmap secondBitmap = mItemBitmaps.get(getImageIdByTag(secondTag)).getBitmap();
        second.setImageBitmap(secondBitmap);
        LayoutParams lpSecond = new LayoutParams(mItemWidth, mItemWidth);
        lpSecond.leftMargin = mSecond.getLeft() - mPadding;
        lpSecond.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lpSecond);
        mAnimLayout.addView(second);

        // 为first设置动画
        TranslateAnimation animFirst = new TranslateAnimation(0, mSecond.getLeft() - mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
        animFirst.setDuration(300);
        animFirst.setFillAfter(true);
        first.setAnimation(animFirst);

        // 为second设置动画
        TranslateAnimation animSecond = new TranslateAnimation(0, -mSecond.getLeft() + mFirst.getLeft(), 0, -mSecond.getTop() + mFirst.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        second.setAnimation(animSecond);

        // 监听动画
        animFirst.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {

                mFirst.setVisibility(View.INVISIBLE);
                mSecond.setVisibility(View.INVISIBLE);
                isAniming = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

                mSecond.setImageBitmap(firstBitmap);

                mFirst.setImageBitmap(secondBitmap);

                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);

                mFirst.setVisibility(View.VISIBLE);
                mSecond.setVisibility(View.VISIBLE);

                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();
                checkSuccess();
                isAniming = false;
            }
        });
    }

    /**
     * 判读拼图是否成功
     */
    private void checkSuccess()
    {

        boolean isSuccess = true;

        for (int i = 0; i < mGameItems.length; i++)
        {

            ImageView imageView = mGameItems[i];

            if (getItemIndexByTag((String) imageView.getTag()) != i)
            {

                isSuccess = false;
            }
        }
        if (isSuccess)
        {

            isGameSuccess = true;
            mHandler.removeMessages(TIME_CHANGED);

            Log.d("TAG", "SUCCESS");
            Toast.makeText(getContext(), "拼图成功，进入下一关", Toast.LENGTH_LONG).show();
            // mHandler.sendEmptyMessage(NEXT_LEVEL);
            mHandler.sendEmptyMessageDelayed(NEXT_LEVEL, 3000);
        }

    }

    /**
     * 根据tag获取Id
     *
     * @param tag
     * @return
     */
    public int getImageIdByTag(String tag)
    {

        String[] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    /**
     * 格局tag获取Index
     *
     * @param tag
     * @return
     */
    public int getItemIndexByTag(String tag)
    {

        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }

    /**
     * 构造动画层
     */
    private void setUpAnimLayout()
    {

        if (mAnimLayout == null)
        {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    /**
     * 进入下一关
     */
    public void nextLevel()
    {

        this.removeAllViews();
        mAnimLayout = null;
        mColumn++;
        isGameSuccess = false;
        checkTimeEnable();
        initBitmap();
        initItem();
    }

    /**
     * 重新开始游戏时调用
     */
    public void restart()
    {

        isGameOver = false;
        mColumn--;
        nextLevel();
    }

    /**
     * 游戏暂停时时调用
     */
    public void pause()
    {

        isPause = true;
        mHandler.removeMessages(TIME_CHANGED);
    }

    /**
     * 从暂停状态恢复时调用
     */
    public void resume()
    {

        if (isPause)
        {

            isPause = false;
            mHandler.sendEmptyMessage(TIME_CHANGED);
        }
    }

}
