package com.mnb.rosreader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.Unit;

import java.util.ArrayList;

public class RulesFragment extends Fragment {

  private static final String TAG = "MNB.ROS";

  private Navigator navigator;
  private Unit unit;

  private ArrayList<String> rules;

  public RulesFragment(Navigator navigator, Unit unit) {
    this.navigator = navigator;
    this.unit = unit;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_rules, container, false);

    rules = new ArrayList<String>();

    if (unit == null) {
      System.out.println(TAG + " no unit rules to display");
      return view;
    }

    // set up unit menu button
    TextView rn = view.findViewById(R.id.rules_name);
    rn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.showItemSelector();
      }
    });

    // set up option menu button
    Button ub = view.findViewById(R.id.option_button);
    ub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Context c = getContext();
        navigator.showPopupMenu(c, v);
      }
    });

    LinearLayout rulesView = view.findViewById(R.id.rules_list);

    for (Rule r : unit.rules) {
      if (!rules.contains(r.name)) {
        rules.add(r.name);
        View v = inflater.inflate(R.layout.item_rule, container, false);
        TextView t = v.findViewById(R.id.item_rule_name);
        t.setText(r.name);
        t = v.findViewById(R.id.item_rule_description);
        if (r.description == null || r.description.isEmpty()) {
          t.setText("no description found");
        } else {
          t.setText(r.description);
        }
        rulesView.addView(v);
      }
    }

    return view;
  }
}
