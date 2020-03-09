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

import com.mnb.rosreader.data.Force;
import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.Unit;

import java.util.ArrayList;
import java.util.HashMap;

public class RulesFragment extends Fragment {

  private static final String TAG = "MNB.ROS";

  private Navigator navigator;
  private ArrayList<Force> forces;
  private boolean showPoints;

  private ArrayList<String> rules;
  private HashMap<Integer, Rule> numberedRules;

  public RulesFragment(Navigator navigator, ArrayList<Force> forces, boolean showPoints) {
    this.navigator = navigator;
    this.forces = forces;
    this.showPoints = showPoints;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_rules, container, false);

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

    if (forces == null || forces.isEmpty()) {
      System.out.println(TAG + " no unit rules to display");
      return view;
    }

    // used to organize sets of rules
    numberedRules = new HashMap<Integer, Rule>();

    LinearLayout rulesView = view.findViewById(R.id.rules_list);

    for (Force f : forces) {
      // only need to enforce uniqueness within each detachment
      rules = new ArrayList<String>();

      // show power level/point cost, if any (and if option is toggled)
      String pointsString = "";
      if (showPoints && (f.pl > 0 || f.pts > 0)) {
        pointsString = pointsString + " - PL: " + f.pl + " / PTS: " + f.pts;
      }

      // show detachment info prior to showing detachment rules
      View v = inflater.inflate(R.layout.item_rule, container, false);
      // prune to show only specific faction
      String[] detachmentParts = f.name.split(" - ");
      TextView t = v.findViewById(R.id.item_rule_name);
      if (detachmentParts.length > 1) {
        t.setText(detachmentParts[1] + pointsString);
      } else {
        t.setText(f.name + pointsString);
      }
      String unitList = "";
      for (Unit u : f.units) {
        if (!unitList.isEmpty()) {
          unitList = unitList + "\n";
        }
        unitList = unitList + " - " + u.name;
      }
      t = v.findViewById(R.id.item_rule_description);
      t.setText(unitList);
      rulesView.addView(v);

      for (Rule r : f.rules) {
        if (!rules.contains(r.name)) {
          rules.add(r.name);
          // set aside numbered rules to display in order at the end
          // currently only intended for c'tan powers
          if (Character.isDigit(r.name.charAt(0)) && r.name.charAt(1) == ')') {
            Integer ruleNumber = Integer.parseInt("" + r.name.charAt(0));
            numberedRules.put(ruleNumber, r);
          } else {
            v = inflater.inflate(R.layout.item_rule, container, false);
            t = v.findViewById(R.id.item_rule_name);
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
      }
    }

    // need to fix range, but expected values < 10 and may not be contiguous
    // currently only supports c'tan powers
    for (int i = 0; i < 10; i++) {
      if (numberedRules.containsKey(i)) {
        Rule r = numberedRules.get(i);
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
