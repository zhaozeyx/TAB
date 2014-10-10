package young.swamp.com.tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;


/**
 * 基于fragment实现的TAB的基类
 */
public abstract class BaseTabActivity extends FragmentActivity {


    /**
     * TAG 前缀
     */
    private static final String TAGS_PREFIX = "main_tab_";

    @SuppressWarnings("rawtypes")
    private Class[] mContentClazzes;

    /**
     * TabHost
     */
    private TabHost mTabHost;

    /**
     * Tab管理类，用来添加fragment到每个tab中
     */
    private TabManager mTabManager;

    private TabIndicatorView[] mIndicatorViews;

    /**
     * TAB IDS
     */
    private String[] mTabIds;

    /**
     * 当前可见的fragment
     */
    private Fragment mCurrentVisibleFragment;

    private OnTabChangedListener mOnTabChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        if (null == findViewById(R.id.tabContentContainer)) {
            throw new RuntimeException("用于展示内容的视图必须定义名称为 tabContentContainer");
        }
        initTabHost();
        setTabContent();
    }

    private void setTabContent() {
        // 获得需要展示的数据内容
        mContentClazzes = getContentClazzes();

        // widget 标题
        String[] titles = getTabTitles();

        // widget 图标
        int[] icons = getTabIcons();

        if (null != titles && mContentClazzes.length != titles.length) {
            throw new RuntimeException("标题数目与展示的标签数目不同");
        }
        if (null != icons && mContentClazzes.length != icons.length) {
            throw new RuntimeException("标题数目与展示的标签图标数目不同");
        }

        mIndicatorViews = new TabIndicatorView[mContentClazzes.length];
        mTabIds = new String[mContentClazzes.length];

        for (int i = 0; i < mContentClazzes.length; i++) {
            mIndicatorViews[i] = getIndicatorView((null != titles && titles.length > 0) ? titles[i]
                            : "",
                    (null != icons && icons.length > 0) ? icons[i] : -1);
            mTabIds[i] = getFragmentTag(i);
            mTabManager.addTab(mTabHost.newTabSpec(mTabIds[i])
                            .setIndicator(mIndicatorViews[i]),
                    mContentClazzes[i],
                    null);
        }
        mCurrentVisibleFragment = mTabManager.getFragmentById(mTabIds[0]);
        mTabManager.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                mCurrentVisibleFragment = mTabManager.getFragmentById(tabId);
                if (null != mOnTabChangedListener) {
                    mOnTabChangedListener.onTabChanged(tabId);
                }
            }
        });

        // 设置默认的显示页面
        mTabManager.onTabChanged(mTabIds[0]);
    }

    /**
     * 布局界面<BR>
     *
     * @return 布局ID
     */
    protected abstract int getLayoutId();

    /**
     * 页签内容<BR>
     *
     * @return 显示页签内容的fragments
     */
    @SuppressWarnings("rawtypes")
    protected abstract Class[] getContentClazzes();

    /**
     * 页签指示器标题<BR>
     *
     * @return 指示器标题
     */
    protected abstract String[] getTabTitles();

    /**
     * 页签指示器图标<BR>
     *
     * @return 指示器图标
     */
    protected int[] getTabIcons() {
        return null;
    }

    /**
     * 图标相对文字的方向<BR>
     *
     * @return 图标相对文字的方向
     */
    protected int getTabIconDirection() {
        return TabIndicatorView.DRAWABLE_TOP;
    }

    /**
     * 获得当前可见fragment<BR>
     *
     * @return 当前可见的fragment
     */
    protected Fragment getVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    /**
     * 根据id获取fragment<BR>
     *
     * @param tag fragment的ID
     * @return fragment
     */
    protected Fragment getTabById(String tag) {
        return mTabManager.getFragmentById(tag);
    }

    /**
     * 根据索引获取fragment<BR>
     *
     * @param index 索引
     * @return fragment
     */
    protected Fragment getTabByIndex(int index) {
        return mTabManager.getFragmentById(getTabId(index));
    }

    /**
     * 设置页签切换的事件监听<BR>
     *
     * @param listener
     */
    protected void setOnTabChangedListener(OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    /**
     * 切换显示的tab<BR>
     *
     * @param index 显示的tab索引
     */
    protected void setCurrentTab(int index) {
        mTabHost.setCurrentTab(index);
    }

    /**
     * 当页签切换<BR>
     *
     * @param tabId 页签ID
     */
    protected void onTabChanged(String tabId) {
        mTabManager.onTabChanged(tabId);
    }

    /**
     * 根据索引获得页签ID<BR>
     *
     * @param index 索引
     * @return 页签
     */
    protected String getTabId(int index) {
        if (index >= mTabIds.length) {
            throw new IndexOutOfBoundsException("当前索引超过显示的页签个数");
        }
        return mTabIds[index];
    }

    /**
     * 设置指示器数字<BR>
     *
     * @param index 指示器索引
     * @param count 个数
     */
    protected void setIndicatorCount(int index, int count) {
        mIndicatorViews[index].setUnreadCount(count);
    }

    /**
     * 获得指示器数字<BR>
     *
     * @param index 所以
     * @return 指示器数字
     */
    protected int getIndicatorCount(int index) {
        return mIndicatorViews[index].getUnreadCount();
    }

    private String getFragmentTag(int index) {
        return TAGS_PREFIX + mContentClazzes[index].getName();
    }

    private TabIndicatorView getIndicatorView(String title, int drawableId) {
        return new TabIndicatorView(this, title, drawableId,
                getTabIconDirection());
    }

    private void initTabHost() {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabManager = new TabManager(this, mTabHost, R.id.tabContentContainer);
    }

    @SuppressLint("InflateParams")
    public class TabIndicatorView extends RelativeLayout {
        /**
         * 图标方向 左
         */
        private static final int DRAWABLE_LEFT = 0x01;

        /**
         * 图标方向 上
         */
        private static final int DRAWABLE_TOP = 0x02;

        /**
         * 图标方向 右
         */
        private static final int DRAWABLE_RIGHT = 0x03;

        /**
         * 图标方向 下
         */
        private static final int DRAWABLE_BOTTOM = 0x04;

        /**
         * 标题字符串
         */
        private String mTabTitleStr;

        /**
         * 标题视图
         */
        private TextView mTitleView;

        /**
         * 计数视图
         */
        private TextView mCountView;

        /**
         * 计数
         */
        private int mCount;

        /**
         * 标志资源ID
         */
        private int mDrawableId;

        /**
         * 标志方向
         */
        private int mDrawableDirection;

        public TabIndicatorView(Context context) {
            this(context, null);
        }

        public TabIndicatorView(Context context, String tabTitleStr) {
            this(context, tabTitleStr, -1, DRAWABLE_TOP);

        }

        public TabIndicatorView(Context context, String tabTitleStr,
                                int drawableId, int drawableDirection) {
            super(context);
            mTabTitleStr = tabTitleStr;
            mDrawableId = drawableId;
            mDrawableDirection = drawableDirection;
            initView(context);
        }

        private void initView(Context context) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.tab_indicator, null);
            mTitleView = (TextView) view.findViewById(R.id.tab_title);
            mCountView = (TextView) view.findViewById(R.id.tab_unread_msg);
            mTitleView.setText(mTabTitleStr);

            if (mDrawableId > 0) {
                switch (mDrawableDirection) {
                    case DRAWABLE_TOP:
                        mTitleView.setCompoundDrawablesWithIntrinsicBounds(0,
                                mDrawableId,
                                0,
                                0);
                        break;
                    case DRAWABLE_LEFT:
                        mTitleView.setCompoundDrawablesWithIntrinsicBounds(mDrawableId,
                                0,
                                0,
                                0);
                        break;
                    case DRAWABLE_RIGHT:
                        mTitleView.setCompoundDrawablesWithIntrinsicBounds(0,
                                0,
                                mDrawableId,
                                0);
                        break;
                    case DRAWABLE_BOTTOM:
                        mTitleView.setCompoundDrawablesWithIntrinsicBounds(0,
                                0,
                                0,
                                mDrawableId);
                        break;
                    default:
                        mTitleView.setCompoundDrawablesWithIntrinsicBounds(0,
                                mDrawableId,
                                0,
                                0);
                        break;
                }
            }
            addView(view, new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setUnreadCount(0);
        }

        public void setUnreadCount(int count) {
            mCount = count;
            mCountView.setVisibility(mCount > 0 ? View.VISIBLE : View.GONE);
            mCountView.setText(String.valueOf(count));
        }

        public int getUnreadCount() {
            return mCount;
        }

    }

    /**
     * tab切换监听<BR>
     */
    public interface OnTabChangedListener {
        void onTabChanged(String tabId);
    }

}
