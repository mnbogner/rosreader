package com.mnb.rosreader.data;

public class Rule {

  public String name;

  public String description = "";

  // need specific properties to handle some explosion data
  public String roll = "";
  public String distance = "";
  public String wounds = "";

  public Rule (String name) {
    this.name = name;
  }
}
