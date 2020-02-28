package com.mnb.rosreader;

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

  private RosSelector selector;
  private Unit unit;

  private View view;

  private ArrayList<String> rules = new ArrayList<String>();

  public RulesFragment(RosSelector selector, Unit unit) {
    this.selector = selector;
    this.unit = unit;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_rules, container, false);

    if (unit == null) {
      System.out.println("BAR - EMPTY FRAGMENT");
      return view;
    }

    Button ub = view.findViewById(R.id.rules_button);
    ub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showSelector();
      }
    });

    TextView nt = view.findViewById(R.id.rules_name);
    nt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showItems();
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
