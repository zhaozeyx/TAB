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
 * The class used to show tab with fragments
 */
public abstract class BaseTabActivity extends FragmentActivity {


    /**
     * TAG prefix
     */
    private static final String TAGS_PREFIX = "main_tab_";

    @SuppressWarnings("rawtypes")
    private Class[] mContentClazzes;

    /**
     * TabHost
     */
    private TabHost mTabHost;

    /**
     * manage fragment, to show,add fragment to tab
     */
    private TabManager mTabManager;

    /**
     * an array of tab indicator
     */
    private TabIndicatorView[] mIndicatorViews;

    /**
     * TAB IDS
     */
    private String[] mTabIds;

    /**
     * current fragment
     */
    private Fragment mCurrentVisibleFragment;

    private OnTabChangedListener mOnTabChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        if (null == findViewById(R.id.tabContentContainer)) {
            throw new RuntimeException("a view to show content must be defined with id tabContentContainer");
        }
        initTabHost();
        setTabContent();
    }

    private void setTabContent() {

        mContentClazzes = getContentClazzes();

        String[] titles = getTabTitles();

        int[] icons = getTabIcons();

        if (null != titles && mContentClazzes.length != titles.length) {
            throw new RuntimeException("the count of contents must equal with the count of titles");
        }
        if (null != icons && mContentClazzes.length != icons.length) {
            throw new RuntimeException("the count of contents must equal with the count of icons");
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

        // set default content
        mTabManager.onTabChanged(mTabIds[0]);
    }

    /**
     * the tab layout id<BR>
     *
     * @return layout id
     */
    protected abstract int getLayoutId();

    /**
     * content class<BR>
     *
     * @return the fragments to show
     */
    @SuppressWarnings("rawtypes")
    protected abstract Class[] getContentClazzes();

    /**
     * the title of tab indicator<BR>
     *
     * @return the title of tab indicator
     */
    protected abstract String[] getTabTitles();

    /**
     * the icon of tab indicator<BR>
     *
     * @return the icon of tab indicator
     */
    protected int[] getTabIcons() {
        return null;
    }

    /**
     * the position icon relative title<BR>
     *
     * @return the position icon relative title
     */
    protected int getTabIconDirection() {
        return TabIndicatorView.DRAWABLE_TOP;
    }

    /**
     * get current fragment<BR>
     *
     * @return get current fragment
     */
    protected Fragment getVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    /**
     * get fragment by id<BR>
     *
     * @param tag fragment ID
     * @return fragment
     */
    protected Fragment getTabById(String tag) {
        return mTabManager.getFragmentById(tag);
    }

    /**
     * get fragment by index<BR>
     *
     * @param index the index of tab
     * @return fragment
     */
    protected Fragment getTabByIndex(int index) {
        return mTabManager.getFragmentById(getTabId(index));
    }

    /**
     * set listener <BR>
     *
     * @param listener
     */
    protected void setOnTabChangedListener(OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    /**
     * switch tab<BR>
     *
     * @param index 显示的tab索引
     */
    protected void setCurrentTab(int index) {
        mTabHost.setCurrentTab(index);
    }

    /**
     * on tab changed<BR>
     *
     * @param tabId tab id
     */
    protected void onTabChanged(String tabId) {
        mTabManager.onTabChanged(tabId);
    }

    /**
     * get tab id by index<BR>
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
     * set indicator count (such as unread message)<BR>
     *
     * @param index the index of tab
     * @param count the count to be set
     */
    protected void setIndicatorCount(int index, int count) {
        mIndicatorViews[index].setUnreadCount(count);
    }

    /**
     * get the number of tab indicator<BR>
     *
     * @param index the index of tab
     * @return the count of indicator
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
         * icon direction left
         */
        private static final int DRAWABLE_LEFT = 0x01;

        /**
         * icon direction top
         */
        private static final int DRAWABLE_TOP = 0x02;

        /**
         * icon direction right
         */
        private static final int DRAWABLE_RIGHT = 0x03;

        /**
         * icond direction bottom
         */
        private static final int DRAWABLE_BOTTOM = 0x04;

        /**
         * title
         */
        private String mTabTitleStr;

        /**
         * the view shows title
         */
        private TextView mTitleView;

        /**
         * the view shows the number of new message
         */
        private TextView mCountView;

        /**
         * the number of new message
         */
        private int mCount;

        /**
         * icon id
         */
        private int mDrawableId;

        /**
         * icon direction
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
     * the listener invoked when switch tab<BR>
     */
    public interface OnTabChangedListener {
        void onTabChanged(String tabId);
    }

}
