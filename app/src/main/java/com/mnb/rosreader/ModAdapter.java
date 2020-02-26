package com.mnb.rosreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ModAdapter extends BaseAdapter {

  public static String STRINGS_TAG = "STRINGS";
  public static String STATES_TAG = "STATES";

  private Context context;
  private ModListInterface modInterface;
  //private ArrayList<Modifier> modList;
  //private ArrayList<String> modState;
  private String[] modStrings;
  private Boolean[] modStates;

  public ModAdapter(Context context, ModListInterface modInterface) {
    this.context = context;
    this.modInterface = modInterface;
  }

  public String[] getStrings() {
    return modStrings;
  }

  public void setStrings(String[] modStrings) {
    this.modStrings = modStrings;
  }

  public Boolean[] getStates() {
    return modStates;
  }

  public void setStates(Boolean[] modStates) {
    this.modStates = modStates;
  }

/*
  public ModAdapter(Context context, ArrayList<Modifier> modList) {
    this.context = context;
    this.modList = modList;
    modState = new boolean[modList.size()];
    //modState = new ArrayList<String>(modList.size());
    //for (Modifier m : modList) {
    //  modState.add(0);
    //}
  }

  public ModAdapter(Context context, ArrayList<Modifier> modList, boolean[] modState) {
    this.context = context;
    this.modList = modList;
    this.modState = modState;
  }

  public ArrayList<Modifier> getList() {
    return modList;
  }

  public boolean[] getState() {
    return modState;
  }
  */

  @Override
  public int getCount() {
    if (modStrings == null) {
      return 0;
    } else {
      return modStrings.length;
    }
  }

  @Override
  public Object getItem(int position) {
    if (modStrings == null) {
      return null;
    } else if (modStrings.length <= position) {
      return null;
    } else {
      return modStrings[position];
    }
  }

  @Override
  public long getItemId(int position) {
    if (modStrings == null) {
      return -1;
    } else if (modStrings.length <= position) {
      return -1;
    } else {
      return position;
    }
  }

  @Override
  public View getView(final int position, View view, ViewGroup parent) {

    if (view == null) {
      view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    TextView tv = view.findViewById(R.id.item_txt);
    tv.setText(modStrings[position]);
    Button b = view.findViewById(R.id.item_button);
    if (modStates[position]) {
      b.setText("(ON)");
    } else {
      b.setText("(OFF)");
    }
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        modInterface.toggleMod(modStrings[position]);
      }
    });

    return view;
  }

  public interface ModListInterface {
    public void toggleMod(String modName);
  }

}
