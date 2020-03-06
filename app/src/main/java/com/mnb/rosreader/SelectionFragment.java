package com.mnb.rosreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;

public class SelectionFragment extends DialogFragment {

  private Navigator navigator;
  private String[] unitList;

  public SelectionFragment(Navigator navigator, String[] itemNameList) {
    this.navigator = navigator;
    this.unitList = itemNameList;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_selection, container, false);

    LinearLayout ll = view.findViewById(R.id.selection_list);

    for (String s : unitList) {
      View v = inflater.inflate(R.layout.item_selection, container, false);
      TextView tv = v.findViewById(R.id.selection_name);
      tv.setText(s);
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          navigator.goToItem(((TextView)v).getText().toString());
          dismiss();
        }
      });
      System.out.println("BAR - adding item for " + s);
      ll.addView(v);
    }

    System.out.println("BAR - UNITS: " + Arrays.toString(unitList));

    return view;

  }

  @Override
  public void onResume() {
    super.onResume();
    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
  }
}
