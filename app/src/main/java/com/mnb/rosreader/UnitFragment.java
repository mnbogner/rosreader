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

import com.mnb.rosreader.data.Damage;
import com.mnb.rosreader.data.SubUnit;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.data.Weapon;

import java.util.ArrayList;

public class UnitFragment extends Fragment {

  private RosSelector selector;
  private Unit unit;

  private View view;

  private ArrayList<String> units = new ArrayList<String>();
  private ArrayList<String> weapons = new ArrayList<String>();

  public UnitFragment (RosSelector selector, Unit unit) {
    this.selector = selector;
    this.unit = unit;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_unit, container, false);

    if (unit == null) {
      System.out.println("BAR - EMPTY FRAGMENT");
      return view;
    }

    TextView t = view.findViewById(R.id.unit_name);
    if (unit.warlord) {
      t.setText(unit.name + "(Warlord)");
    } else {
      t.setText(unit.name);
    }

    Button ub = view.findViewById(R.id.unit_button);
    ub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showSelector();
      }
    });

    Button ib = view.findViewById(R.id.info_button_open);
    ib.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showInfo(unit.powers, unit.rules);
      }
    });

    TextView nt = view.findViewById(R.id.unit_name);
    nt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showItems();
      }
    });

    LinearLayout unitView = view.findViewById(R.id.unit_list);

    View v = inflater.inflate(R.layout.item_unit, container, false);
    unitView.addView(v);

    for (SubUnit su : unit.subUnits) {
      if (!units.contains(su.name)) {
        units.add(su.name);
        v = inflater.inflate(R.layout.item_unit, container, false);
        t = v.findViewById(R.id.item_unit_name);
        t.setText(su.name);
        t = v.findViewById(R.id.item_unit_m);
        t.setText(su.m);
        t = v.findViewById(R.id.item_unit_ws);
        t.setText(su.ws);
        t = v.findViewById(R.id.item_unit_bs);
        t.setText(su.bs);
        t = v.findViewById(R.id.item_unit_s);
        t.setText(su.s);
        t = v.findViewById(R.id.item_unit_t);
        t.setText(su.t);
        t = v.findViewById(R.id.item_unit_w);
        t.setText(su.w);
        t = v.findViewById(R.id.item_unit_a);
        t.setText(su.a);
        t = v.findViewById(R.id.item_unit_ld);
        t.setText(su.ld);
        t = v.findViewById(R.id.item_unit_save);
        t.setText(su.save);
        unitView.addView(v);
      }
    }

    LinearLayout psykerView = view.findViewById(R.id.psyker);
    if (unit.psyker != null) {
      v = inflater.inflate(R.layout.item_psyker, container, false);
      psykerView.addView(v);

      v = inflater.inflate(R.layout.item_psyker, container, false);
      t = v.findViewById(R.id.item_psyker_name);
      t.setText("Psyker");
      t = v.findViewById(R.id.item_psyker_cast);
      t.setText(unit.psyker.cast);
      t = v.findViewById(R.id.item_psyker_deny);
      t.setText(unit.psyker.deny);
       t = v.findViewById(R.id.item_psyker_known);
       t.setText(unit.psyker.powersKnown);
      // t = v.findViewById(R.id.item_psyker_other);
      // t.setText(unit.psyker.other);
      psykerView.addView(v);
    } else {
      psykerView.setVisibility(View.GONE);
    }

    LinearLayout damageView = view.findViewById(R.id.damage_list);
    if (unit.damages.size() > 0) {
      v = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v);

      for (Damage d : unit.damages) {
        v = inflater.inflate(R.layout.item_damage, container, false);
        t = v.findViewById(R.id.item_damage_remaining);
        t.setText(d.remaining);
        t = v.findViewById(R.id.item_damage_m);
        t.setText(d.m);
        t = v.findViewById(R.id.item_damage_ws);
        t.setText(d.ws);
        t = v.findViewById(R.id.item_damage_bs);
        t.setText(d.bs);
        t = v.findViewById(R.id.item_damage_s);
        t.setText(d.s);
        t = v.findViewById(R.id.item_damage_t);
        t.setText(d.t);
        t = v.findViewById(R.id.item_damage_w);
        t.setText(d.w);
        t = v.findViewById(R.id.item_damage_a);
        t.setText(d.a);
        t = v.findViewById(R.id.item_damage_ld);
        t.setText(d.ld);
        t = v.findViewById(R.id.item_damage_save);
        t.setText(d.save);
        damageView.addView(v);
      }
    } else {
      damageView.setVisibility(View.GONE);
    }

    LinearLayout weaponView = view.findViewById(R.id.weapon_list);

    v = inflater.inflate(R.layout.item_weapon, container, false);
    weaponView.addView(v);

    for (Weapon w : unit.weapons) {
      if (!weapons.contains(w.name)) {
        weapons.add(w.name);
        v = inflater.inflate(R.layout.item_weapon, container, false);
        t = v.findViewById(R.id.item_weapon_name);
        t.setText(w.name);
        t = v.findViewById(R.id.item_weapon_range);
        if ("Melee".equals(w.range)) {
          t.setText("n/a");
        } else {
          t.setText(w.range);
        }
        t = v.findViewById(R.id.item_weapon_type);
        String shortType = w.type.replace("Rapid Fire", "R")
            .replace("Assault", "A")
            .replace("Heavy", "H")
            .replace("Pistol", "P")
            .replace("Grenade", "G");
        t.setText(shortType);
        t = v.findViewById(R.id.item_weapon_s);
        if ("User".equals(w.s)) {
          t.setText("n/a");
        } else {
          t.setText(w.s);
        }
        t = v.findViewById(R.id.item_weapon_ap);
        t.setText(w.ap);
        t = v.findViewById(R.id.item_weapon_d);
        t.setText(w.d);
        t = v.findViewById(R.id.item_weapon_abilities);
        t.setText(w.abilities);
        weaponView.addView(v);
      }
    }

    return view;
  }

}
