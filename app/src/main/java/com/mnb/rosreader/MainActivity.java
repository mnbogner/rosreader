package com.mnb.rosreader;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.parser.RosAssetParser;
import com.mnb.rosreader.parser.RosDownloadParser;
import com.mnb.rosreader.parser.RosParser;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements Navigator {

  private static final String TAG = "MNB.ROS";

  private static final String FILE_TAG = "file_dialog";
  private static final String ITEM_TAG = "item_dialog";
  private static final String INFO_TAG = "info_dialog";

  private static final int PERMISSION_REQUEST_CODE = 100;

  private SharedPreferences preferences;

  private RosParser parser;

  private ArrayList<Unit> units = null;
  private HashMap<String, Integer> index = new HashMap<String, Integer>();

  private FragmentManager fm;
  private ViewPager vp;
  private PagerAdapter pa;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // check permissions before trying to get file list
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      System.out.println(TAG + " need permissions");
      ActivityCompat.requestPermissions(this,
          new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
          PERMISSION_REQUEST_CODE);
    } else {
      System.out.println(TAG + " permissions ok");
      init();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        System.out.println(TAG + " got permissions");
        init();
      } else {
        System.out.println(TAG + " permissions denied");
      }
    } else {
      System.out.println(TAG + " wrong request code");
    }
  }

  private void init() {
    preferences = getSharedPreferences(TAG, MODE_PRIVATE);

    // switch between loading assets for testing on emulators and loading downloads for actual use
    // parser = new RosAssetParser(this);
    parser = new RosDownloadParser(this);

    fm = getSupportFragmentManager();
    vp = findViewById(R.id.fragment_pager);
    initPager();
    showFileDialog();
  }

  private void initPager() {
    // creating a new adapter was the only way i found to clear stale pages from previous data set
    pa = new MyPagerAdapter(fm);
    vp.setAdapter(pa);
    vp.setCurrentItem(0);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle inState) {
    super.onRestoreInstanceState(inState);
  }

  @Override
  public void onBackPressed() {
    int i = vp.getCurrentItem();
    if (i == 0) {
      super.onBackPressed();
    } else {
      vp.setCurrentItem(i - 1);
    }
  }

  @Override
  public void showPopupMenu(Context c, View v) {
    final PopupMenu popup = new PopupMenu(c, v);
    popup.inflate(R.menu.popup_menu);
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.load_item:
            showFileSelector();
            return true;
          case R.id.count_item:
            toggleOption(COUNT_PREF);
            item.setChecked(checkOption(COUNT_PREF));
            initPager();
            return true;
          case R.id.points_item:
            toggleOption(POINTS_PREF);
            item.setChecked(checkOption(POINTS_PREF));
            initPager();
            return true;
          default:
            return false;
        }
      }
    });
    popup.getMenu().findItem(R.id.count_item).setChecked(checkOption(COUNT_PREF));
    popup.getMenu().findItem(R.id.points_item).setChecked(checkOption(POINTS_PREF));
    popup.show();
  }

  @Override
  public void showFileSelector() {
    showFileDialog();
  }

  void showFileDialog() {
    FragmentTransaction ft = fm.beginTransaction();
    // remove any existing version of this fragment
    Fragment f = fm.findFragmentByTag(FILE_TAG);
    if (f != null) {
      ft.remove(f);
    }
    ft.addToBackStack(null);
    ArrayList<String> rosFileList = parser.getRosFileList();
    DialogFragment df = new FileFragment(this, rosFileList);
    df.show(ft, FILE_TAG);
  }

  @Override
  public void openFile(String fileName){
    units = parser.parseRosFile(fileName);
    int i = 0;
    for (Unit u : units) {
      index.put(u.name, i);
      i++;
    }
    initPager();
  }

  @Override
  public void showItemSelector() {
    showItemDialog();
  }

  void showItemDialog() {
    FragmentTransaction ft = fm.beginTransaction();
    // remove any existing version of this fragment
    Fragment f = fm.findFragmentByTag(ITEM_TAG);
    if (f != null) {
      ft.remove(f);
    }
    ft.addToBackStack(null);
    String[] itemNameList = new String[units.size()];
    int i = 0;
    for (Unit u : units) {
      itemNameList[i] = u.name;
      i++;
    }
    DialogFragment df = new SelectionFragment(this, itemNameList);
    df.show(ft, ITEM_TAG);
  }

  @Override
  public void goToItem(String itemName) {
    int itemIndex = index.get(itemName);
    vp.setCurrentItem(itemIndex);
  }

  @Override
  public void showItemInfo(String itemName) {
    FragmentTransaction ft = fm.beginTransaction();
    // remove any existing version of this fragment
    Fragment f = fm.findFragmentByTag(INFO_TAG);
    if (f != null) {
      ft.remove(f);
    }
    ft.addToBackStack(null);
    // lookup unit
    for (Unit u : units) {
      if (u.name.equals(itemName)) {
        DialogFragment df = new InfoFragment(u, checkOption(POINTS_PREF));
        df.show(ft, INFO_TAG);
        return;
      }
    }
  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      if (units == null || units.isEmpty()) {
        return null;
      }
      Unit u = units.get(i);
      Fragment f = fm.findFragmentByTag(u.name);
      if (f == null) {
        // initial page is a general roster info page, remaining pages show unit info
        if (i == 0) {
          f = new RulesFragment(MainActivity.this, u);
        } else {
          f = new UnitFragment(MainActivity.this, u, checkOption(COUNT_PREF));
        }
      }
      return f;
    }

    @Override
    public int getCount() {
      if (units == null) {
        return 0;
      } else {
        return units.size();
      }
    }
  }

  // currently there are only boolean checkbox options

  @Override
  public void toggleOption(String optionName) {
    boolean currentValue = preferences.getBoolean(optionName, false);
    SharedPreferences.Editor e = preferences.edit();
    e.putBoolean(optionName, !currentValue);
    e.commit();
  }

  @Override
  public boolean checkOption(String optionName) {
    return preferences.getBoolean(optionName, false);
  }
}
