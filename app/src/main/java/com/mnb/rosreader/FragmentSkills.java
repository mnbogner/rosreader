package com.mnb.rosreader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentSkills extends Fragment {

  static String TAG = "SKILLS";

  //Navigator navigator;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.fragment_skills, container, false);

    /*
    Button b1 = v.findViewById(R.id.button1_3);
    b1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.toFragmentTwo();
      }
    });

    Button b2 = v.findViewById(R.id.button2_3);
    b2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.toFragmentOne();
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
