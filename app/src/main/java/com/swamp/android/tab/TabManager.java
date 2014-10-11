package com.swamp.android.tab;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理添加fragment的类<BR>
 */
public class TabManager implements OnTabChangeListener {

    /**
     * TAG
     */
    private static final String TAG = "TabManager";

    /**
     * 要添加fragment TAB的activity
     */
    private final FragmentActivity mActivity;

    /**
     * TAB缓存
     */
    private final Map<String, TabInfo> mTabs = new HashMap<String, TabInfo>();

    /**
     * TabHost
     */
    private final TabHost mTabHost;

    /**
     * fragment的父容器
     */
    private final int mContainerID;

    /**
     * 上一个点击的tab
     */
    private TabInfo mLastTab;

    /**
     * tab该表的监听器
     */
    private OnTabChangeListener mOnTabChangeListener;

    /**
     * 构造方法
     *  @param activity    context
     * @param tabHost     tab
     * @param containerID fragment's parent note
     */
    public TabManager(FragmentActivity activity, TabHost tabHost,
                      int containerID) {
        mActivity = activity;
        mTabHost = tabHost;
        mContainerID = containerID;
        mTabHost.setOnTabChangedListener(this);
    }

    /**
     * Tab信息<BR>
     *
     * @author zhaozeyang
     * @version [Paitao Client V20130911, 2013-9-18]
     */
    static final class TabInfo {
        /**
         * 每一个Tab的Tag
         */
        private final String mTag;

        /**
         * 每个tab页签要展示的view的类的引用
         */
        private final Class<?> mClss;

        /**
         * 传入的参数
         */
        private final Bundle mArgs;

        /**
         * 添加的fragment
         */
        private Fragment mFragment;

        /**
         * 构造方法
         *
         * @param tag  标签
         * @param clss Class
         * @param args 参数
         */
        TabInfo(String tag, Class<?> clss, Bundle args) {
            mTag = tag;
            mClss = clss;
            mArgs = args;
        }
    }

    /**
     * 创建tab页签的工厂类<BR>
     *
     * @author zhaozeyang
     * @version [Paitao Client V20130911, 2013-9-18]
     */
    static class TabFactory implements TabHost.TabContentFactory {
        private static TabFactory sInstance;

        private Context mContext;

        private TabFactory(Context context) {
            mContext = context;
        }

        public static TabFactory getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new TabFactory(context);
            }
            return sInstance;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumHeight(0);
            v.setMinimumWidth(0);
            return v;
        }
    }

    /**
     * 添加页签<BR>
     *
     * @param tabSpec TabSpec
     * @param clss    Class
     * @param args    Bundle
     */
    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        //      tabSpec.setContent(new TabFactory(mActivity));
        tabSpec.setContent(TabFactory.getInstance(mActivity));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clss, args);

        final FragmentManager fm = mActivity.getSupportFragmentManager();
        //      final FragmentManager fm = mActivity.getFragmentManager();
        info.mFragment = fm.findFragmentByTag(tag);

        if (info.mFragment != null) {
            // isDetached分离状态
            if (!info.mFragment.isDetached()) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(info.mFragment);
                ft.commit();
            }
        }
        mTabs.put(tag, info);
        mTabHost.addTab(tabSpec);
    }

    /**
     * 根据ID获得对应的fragment<BR>
     *
     * @param tabId 页签ID
     * @return fragment
     */
    public Fragment getFragmentById(String tabId) {
        TabInfo tabInfo = mTabs.get(tabId);
        return tabInfo.mFragment;
    }

    /**
     * 每次都重新建
     *
     * @param tabId 页签ID
     */
    @Override
    public void onTabChanged(String tabId) {
        TabInfo newTab = mTabs.get(tabId);
        if (mLastTab != newTab) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // 脱离之前的tab
            if (mLastTab != null && mLastTab.mFragment != null) {
                fragmentTransaction.hide(mLastTab.mFragment);
            }
            if (newTab != null) {
                if (newTab.mFragment == null) {
                    newTab.mFragment = Fragment.instantiate(mActivity,
                            newTab.mClss.getName(),
                            newTab.mArgs);
                    fragmentTransaction.add(mContainerID,
                            newTab.mFragment,
                            newTab.mTag);
                } else {
                    fragmentTransaction.show(newTab.mFragment);
                }
            }
            mLastTab = newTab;
            fragmentTransaction.commitAllowingStateLoss();
            // 会在进程的主线程中，用异步的方式来执行,如果想要立即执行这个等待中的操作，就要调用这个方法
            // 所有的回调和相关的行为都会在这个调用中被执行完成，因此要仔细确认这个方法的调用位置。
            fragmentManager.executePendingTransactions();
            invokeOnTabChangeListener(tabId);
        }
    }

    /**
     * Register a callback to be invoked when the selected state of any of the items
     * in this list changes
     *
     * @param l The callback that will run
     */
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    private void invokeOnTabChangeListener(String tabId) {
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    /**
     * tab widget内容发生变化的监听<BR>
     * 如果有Fragment需要tab通知其数据刷新，则实现该接口
     */
    public interface OnTabContentRefreshListener {
        /**
         * 当数据需要刷新<BR>
         */
        void onTabContentRefresh();
    }
}
