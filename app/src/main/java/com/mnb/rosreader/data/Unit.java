package com.mnb.rosreader.data;

import com.mnb.rosreader.parser.ParseRos;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.mnb.rosreader.parser.ParseRos.ns;

public class Unit {

  public String name;

  public Boolean warlord;
  public ArrayList<String> categories;
  public ArrayList<Rule> rules;
  public Psyker psyker;
  public ArrayList<Power> powers;
  public ArrayList<SubUnit> subUnits;
  public ArrayList<Damage> damages;
  public ArrayList<Weapon> weapons;

  public Unit (String name) {

    this.name = name;
    this.warlord = false;
    this.categories = new ArrayList<String>();
    this.rules = new ArrayList<Rule>();
    this.powers = new ArrayList<Power>();
    this.subUnits = new ArrayList<SubUnit>();
    this.damages = new ArrayList<Damage>();
    this.weapons = new ArrayList<Weapon>();

  }

  public static Unit readUnit(XmlPullParser xpp) throws IOException, XmlPullParserException {

    System.out.println("BAR - UNIT - parsing unit");

    xpp.require(XmlPullParser.START_TAG, ns, "selection");
    while (xpp.next() != XmlPullParser.END_TAG) {

      String name = xpp.getName();

      System.out.println("BAR - UNIT - parsing " + name);

      if (name == null) {
        return new Unit("");
      }

      if (name.equals("rule")) {

        String ruleName = xpp.getAttributeValue(ns, "name");

        System.out.println("BAR - UNIT - " + ruleName);

      } else if (name.equals("profile")) {

        String profileName = xpp.getAttributeValue(ns, "typeName");

        if (profileName.equals("Unit")) {
          String unitName = xpp.getAttributeValue(ns, "name");
          System.out.println("BAR - UNIT - " + unitName);
        } else if (profileName.equals("Weapon")) {
          String weaponName = xpp.getAttributeValue(ns, "name");
          System.out.println("BAR - UNIT - " + weaponName);
        } else if (profileName.equals("Psyker")) {
          String psykerName = xpp.getAttributeValue(ns, "name");
          System.out.println("BAR - UNIT - " + psykerName);
        } else if (profileName.equals("Psychic Power")) {
          String powerName = xpp.getAttributeValue(ns, "name");
          System.out.println("BAR - UNIT - " + powerName);
        } else {
          System.out.println("BAR - UNIT - " + profileName + " (ignore)");
        }
      } else {
        ParseRos.skip(xpp);
      }
    }

    return new Unit("");

  }

  private static Unit readUnit(XmlPullParser xpp, Unit unit) {


    return unit;

  }

  private static Unit readWeapon(XmlPullParser xpp, Unit unit) {


    return unit;

  }

  private static Unit readPsyker(XmlPullParser xpp, Unit unit) {


    return unit;

  }

  private static Unit readPower(XmlPullParser xpp, Unit unit) {


    return unit;

  }


}
