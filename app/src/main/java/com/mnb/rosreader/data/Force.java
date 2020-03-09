package com.mnb.rosreader.data;

import java.util.ArrayList;

public class Force {

  public String name;

  public int pl = 0;
  public int pts = 0;
  public ArrayList<Unit> units;
  public ArrayList<Rule> rules;

  public Force (String name) {
    this.name = name;
    this.units = new ArrayList<Unit>();
    this.rules = new ArrayList<Rule>();
  }
}
