package com.mnb.rosreader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentFeats extends Fragment {

  static String TAG = "FEATS";

  //Navigator navigator;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.fragment_feats, container, false);

    /*
    Button b1 = v.findViewById(R.id.button1_2);
    b1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.toFragmentOne();
      }
    });

    Button b2 = v.findViewById(R.id.button2_2);
    b2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.toFragmentThree();
      }
    });
    */

    return v;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    //navigator = (Navigator) context;
  }
}
