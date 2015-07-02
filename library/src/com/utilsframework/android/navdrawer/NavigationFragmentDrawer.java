package com.utilsframework.android.navdrawer;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.utilsframework.android.R;
import com.utilsframework.android.fragments.Fragments;
import com.utilsframework.android.view.GuiUtilities;

import java.util.*;

/**
 * Created by CM on 12/26/2014.
 */
public abstract class NavigationFragmentDrawer {
    private final AppCompatActivity activity;
    private final TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    private FragmentFactory fragmentFactory;
    private NavigationView navigationView;
    private int currentSelectedItem;
    private int navigationLevel = 0;
    private int currentSelectedTabIndex;
    private Set<FragmentManager.OnBackStackChangedListener> backStackChangedListeners =
            Collections.newSetFromMap(new WeakHashMap<FragmentManager.OnBackStackChangedListener, Boolean>());

    private void clearBackStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        for(FragmentManager.OnBackStackChangedListener onBackStackChangedListener : backStackChangedListeners) {
            fragmentManager.removeOnBackStackChangedListener(onBackStackChangedListener);
        }

        Fragments.clearBackStack(activity);
    }

    private void selectFragment(int menuItemId, int navigationLevel, int tabIndex, boolean createFragment) {
        if(menuItemId == currentSelectedItem && navigationLevel == this.navigationLevel){
            return;
        }

        tabLayout.removeAllTabs();
        int tabsCount = fragmentFactory.getTabsCount(menuItemId, navigationLevel);
        if (tabsCount <= 1) {
            if (menuItemId != currentSelectedItem) {
                clearBackStack();
                Fragments.removeFragmentWithId(activity.getSupportFragmentManager(), getContentId());
                Fragment fragment = fragmentFactory.createFragmentBySelectedItem(menuItemId, 0, navigationLevel);
                Fragments.replaceFragment(activity, getContentId(), fragment);
            }
            tabLayout.setVisibility(View.GONE);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            initTabs(tabsCount, menuItemId, navigationLevel, createFragment, tabIndex);
        }

        hide();

        this.currentSelectedItem = menuItemId;
        this.navigationLevel = navigationLevel;
        updateActionBarTitle();
    }

    private void initTabs(int tabsCount,
                          final int menuItemId,
                          final int navigationLevel,
                          final boolean createFragment,
                          final int selectedTabIndex) {
        tabLayout.removeAllTabs();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            boolean shouldCreateFragment = createFragment;

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (menuItemId != currentSelectedItem) {
                    clearBackStack();
                    Fragments.removeFragmentWithId(activity.getSupportFragmentManager(), getContentId());
                }

                int tabIndex = tab.getPosition();
                if (shouldCreateFragment || selectedTabIndex != tabIndex) {
                    Fragment fragment =
                            fragmentFactory.createFragmentBySelectedItem(menuItemId, tabIndex,
                                    navigationLevel);
                    Fragments.replaceFragment(activity, getContentId(), fragment);
                }

                shouldCreateFragment = true;
                currentSelectedTabIndex = tabIndex;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        for (int i = 0; i < tabsCount; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            fragmentFactory.initTab(menuItemId, i, navigationLevel, tab);
            tabLayout.addTab(tab, i == selectedTabIndex);
        }
    }

    public void init() {
        initDrawableToggle();
        int tabsCount = fragmentFactory.getTabsCount(currentSelectedItem, navigationLevel);
        if (tabsCount > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            initTabs(tabsCount, currentSelectedItem, navigationLevel, true, 0);
        } else {
            tabLayout.setVisibility(View.GONE);
            Fragments.replaceFragment(activity, getContentId(),
                    fragmentFactory.createFragmentBySelectedItem(currentSelectedItem, 0, navigationLevel));
        }
        updateActionBarTitle();

        navigationView.inflateMenu(getMenuId());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                updateActionBarTitle();
                selectFragment(menuItem.getItemId(), 0, 0, true);
                return true;
            }
        });
    }

    public NavigationFragmentDrawer(final AppCompatActivity activity,
                                    FragmentFactory fragmentFactory,
                                    int currentSelectedItem) {
        this.fragmentFactory = fragmentFactory;
        this.currentSelectedItem = currentSelectedItem;
        this.activity = activity;

        drawerLayout = (DrawerLayout) activity.findViewById(getDrawerLayoutId());
        navigationView = (NavigationView) activity.findViewById(getNavigationViewId());
        tabLayout = (TabLayout) activity.findViewById(getTabLayoutId());
        Toolbar toolbar = (Toolbar) activity.findViewById(getToolBarLayoutId());
        activity.setSupportActionBar(toolbar);
    }

    public void updateActionBarTitle() {
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar == null) {
                    return;
                }

                String title = null;
                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(getContentId());
                if (fragment instanceof ActionBarTitleProvider) {
                    title = ((ActionBarTitleProvider) fragment).getActionBarTitle();
                }
                if (title == null) {
                    title = getActionBarTitle(currentSelectedItem, currentSelectedTabIndex, navigationLevel);
                }

                actionBar.setTitle(title);
            }
        });
    }

    private void initDrawableToggle() {
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            public void onDrawerClosed(View view) {
                updateActionBarTitle();
            }

            public void onDrawerOpened(View drawerView) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    String title = getActionBarTitleWhenNavigationIsShown(currentSelectedItem);
                    actionBar.setTitle(title);
                }
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getToggleIconResourceId());
    }

    public void show() {
        drawerLayout.openDrawer(navigationView);
    }

    public void hide() {
        drawerLayout.closeDrawer(navigationView);
    }

    protected abstract int getDrawerLayoutId();
    protected abstract int getContentId();
    protected abstract int getNavigationViewId();
    protected abstract int getTabLayoutId();
    protected abstract int getToolBarLayoutId();
    protected abstract int getMenuId();

    protected String getActionBarTitle(int currentSelectedItem, int tabIndex, int navigationLevel) {
        return GuiUtilities.getApplicationName(activity);
    }

    protected String getActionBarTitleWhenNavigationIsShown(int currentSelectedItem) {
        return GuiUtilities.getApplicationName(activity);
    }

    // Override this method only for API 21+
    protected int getToggleIconResourceId() {
        return R.drawable.ic_menu_white;
    }

    public void onBackPressed() {
        if(currentSelectedTabIndex != 0){
            Fragments.removeFragmentWithId(activity.getSupportFragmentManager(), R.id.content);
        }
    }

    public void replaceFragment(Fragment newFragment, final int navigationLevel) {
        final int lastNavigationLevel = this.navigationLevel;
        final int lastTabIndex = currentSelectedTabIndex;
        FragmentManager.OnBackStackChangedListener onBackStackChangedListener =
                Fragments.replaceFragmentAndAddToBackStack(activity, R.id.content, newFragment,
                new Fragments.OnBack() {
                    @Override
                    public void onBack() {
                        selectFragment(currentSelectedItem, lastNavigationLevel, lastTabIndex, false);
                    }
                });
        backStackChangedListeners.add(onBackStackChangedListener);
        selectFragment(currentSelectedItem, navigationLevel, 0, false);
    }

    public int getCurrentSelectedItem() {
        return currentSelectedItem;
    }

    public void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(navigationView);
        }
    }
}
