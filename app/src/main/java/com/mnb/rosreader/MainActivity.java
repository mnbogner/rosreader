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

import com.mnb.rosreader.data.Force;
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

  private static final String ARMY_RULES = "Army Rules";

  private static final int PERMISSION_REQUEST_CODE = 100;

  private SharedPreferences preferences;

  private RosParser parser;

  private ArrayList<Force> forces = null;
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
    // creating a new adapter seemed like the only way to clear stale pages from previous data set
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
            // show list of ross/ros files
            showFileSelector();
            return true;
          case R.id.count_item:
            // toggle unit/weapon counts (values are inconsistent)
            toggleOption(COUNT_PREF);
            item.setChecked(checkOption(COUNT_PREF));
            initPager();
            return true;
          case R.id.points_item:
            // toggle power level/points (values are inconsistent)
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
    // parse xml and populate data structures
    forces = parser.parseRosFile(fileName);
    units = new ArrayList<Unit>();
    // combine units from all detachments
    for (Force f : forces) {
      units.addAll(f.units);
    }

    // number units if necessary to ensure uniqueness
    HashMap<String, Integer> unitCounts = new HashMap<String, Integer>();
    for (Unit u : units) {
      Integer i = unitCounts.get(u.name);
      if (i == null) {
        i = 0;
      }
      i++;
      unitCounts.put(u.name, i);
    }
    for (String s : unitCounts.keySet()) {
      Integer i = unitCounts.get(s);
      if (i > 1) {
        int num = 1;
        for (Unit u : units) {
          if (u.name.equals(s)) {
            u.name = u.name + " " + num;
            num++;
          }
        }
      }
    }

    // add army info as first page
    int i = 0;
    index.put(ARMY_RULES, i);
    i++;
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
    ArrayList<String> itemNameList = new ArrayList<String>();
    itemNameList.add(ARMY_RULES);
    for (Unit u : units) {
      itemNameList.add(u.name);
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
      // first page is an army info page, remaining pages show unit info
      if (units == null || units.isEmpty() || i == 0) {
        Fragment f = new RulesFragment(MainActivity.this, forces, checkOption(POINTS_PREF));
        return f;
      } else {
        // unit index starts at 0
        Unit u = units.get(i - 1);
        Fragment f = new UnitFragment(MainActivity.this, u, checkOption(COUNT_PREF));
        return f;
      }
    }

    @Override
    public int getCount() {
      // first page is an army info page
      if (units == null || units.isEmpty()) {
        return 1;
      } else {
        return 1 + units.size();
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
