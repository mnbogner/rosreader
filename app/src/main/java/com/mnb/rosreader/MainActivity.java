package com.mnb.rosreader;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.parser.ParseRos;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements RosSelector {

  private ArrayList<Unit> units = null;
  private HashMap<String, Integer> index = new HashMap<String, Integer>();

  //private ArrayList<Fragment> fragments = null;

  private FragmentManager fm;

  private ViewPager vp;
  private PagerAdapter pa;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    fm = getSupportFragmentManager();

    vp = findViewById(R.id.fragment_pager);
    pa = new MyPagerAdapter(fm);
    vp.setAdapter(pa);

    // initFragments();

    showDialog();

    // units = ParseRos.doParse(this, "aberrants.ros");
    // pa.notifyDataSetChanged();

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
  public void showInfo(ArrayList<Power> powers, ArrayList<Rule> rules) {

    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("info");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    String[] rosFiles = getRosFiles();
    DialogFragment d = new InfoFragment(powers, rules);

    d.show(ft, "info");

  }

  @Override
  public void showSelector() {

    showDialog();

  }

  @Override
  public void showItems() {
    showOtherDialog();
  }

  @Override
  public void loadRos(String rosFile) {

    System.out.println(rosFile);

    units = ParseRos.doParse(this, rosFile);

    int i = 0;
    for (Unit u : units) {
      index.put(u.name, i);
      i++;
    }

    pa.notifyDataSetChanged();
    vp.setCurrentItem(0);

  }

  @Override
  public void goToItem(String itemName) {

    System.out.println("BAZ - go to item " + itemName);

    int target = index.get(itemName);

    System.out.println("BAZ - go to index " + target);

    vp.setCurrentItem(target);

  }

  private String[] getRosFiles() {

    String[] files = new String[3];
    files[0] = "aberrants.ros";
    files[1] = "more_grey.ros";
    files[2] = "sisters.ros";
    return files;

    /*
    File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    System.out.println("BAR - download directory: " + downloadDirectory.getPath());

    if (downloadDirectory.exists()) {
      System.out.println("BAR - exists");
      FilenameFilter rosFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".ros");
        }
      };
      return downloadDirectory.list(rosFilter);
    } else {
      System.out.println("BAR - no download directory?");
    }
    return new String[0];
    */
  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {

      /*
      if (fragments != null && i < fragments.size()) {
        return fragments.get(i);
      } else {
        return null;
      }

       */


      if (units == null || units.isEmpty()) {
        return null;
      }

      Unit u = units.get(i);

      Fragment f = fm.findFragmentByTag(u.name);

      if (f == null) {
        if (i == 0) {
          f = new RulesFragment(MainActivity.this, u);
        } else {
          f = new UnitFragment(MainActivity.this, u);
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

  private void initFragments() {

    /*
    if (units == null) {
      units = new ArrayList<Unit>();
    }

    if (units.size() > 0) {
      for (Unit u : units) {
        UnitFragment uf = new UnitFragment(this, u);
        fragments.add(uf);
      }
      pa.notifyDataSetChanged();
    } else {

      System.out.println("BAR - SHOW DIALOG?");
      showDialog();
    }


    /*
    if (fragments == null) {
      fragments = new ArrayList<Fragment>();
    } else {
      FragmentTransaction ft = fm.beginTransaction();
      for (Fragment f : fragments) {
        ft.remove(f);
      }
      ft.commitAllowingStateLoss();
    }
    fragments.clear();
    String[] rosFiles = getRosFiles();
    FileFragment ff = new FileFragment(this, rosFiles);
    fragments.add(ff);
    if (units != null) {
      for (Unit u : units) {
        UnitFragment uf = new UnitFragment(this, u);
        fragments.add(uf);
      }
    }
    pa.notifyDataSetChanged();
    if (fragments.size() > 1) {
      vp.setCurrentItem(1);
    }

     */
  }


  void showDialog() {

    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    String[] rosFiles = getRosFiles();
    DialogFragment d = new FileFragment(this, rosFiles);

    d.show(ft, "dialog");
  }

  void showOtherDialog() {

    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    String[] stuff = new String[units.size()];
    int i = 0;
    for (Unit u : units) {
      stuff[i] = u.name;
      i++;
    }
    DialogFragment d = new SelectionFragment(this, stuff);

    d.show(ft, "dialog");
  }

}
