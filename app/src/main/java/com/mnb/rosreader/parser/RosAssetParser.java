package com.mnb.rosreader.parser;

import android.content.Context;
import android.util.Xml;

import com.mnb.rosreader.data.Damage;
import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Psyker;
import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.SubUnit;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.data.Weapon;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RosAssetParser extends RosParser {

  public static String NONE = "none";
  public static String RULE = "rule";
  public static String PSYKER = "psyker";
  public static String POWER = "power";
  public static String UNIT = "unit";
  public static String DAMAGE = "damage";
  public static String WEAPON = "weapon";

  public static String ns = null;  // ignore namespaces?

  public RosAssetParser (Context context) {
    super(context);
  }

  @Override
  protected InputStream openRosFile(String rosFile) {
    try {
      return context.getAssets().open(rosFile);
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to open " + rosFile);
      return null;
    }
  }

  @Override
  protected InputStream openRoszFile(String roszFile) {
    try {
      return unzipFile(context.getAssets().open(roszFile));
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to open " + roszFile);
      return null;
    }
  }

  @Override
  public ArrayList<String> getRosFileList() {

    ArrayList<String> rosFileList = new ArrayList<String>();

    rosFileList.add("iron.rosz");
    rosFileList.add("nurgle.rosz");
    rosFileList.add("eight.ros");
    rosFileList.add("tau.ros");
    rosFileList.add("foo.ros");
    rosFileList.add("aberrants.ros");
    rosFileList.add("chaos.ros");
    rosFileList.add("grey.ros");
    rosFileList.add("iron.ros");
    rosFileList.add("sisters.ros");

    return rosFileList;
  }

  /*

  String characteristic1 = "";
  String characteristic2 = "";
  String characteristic3 = "";

  ArrayList<Unit> units;



  @Override
  public ArrayList<Unit> parseRosFile(String rosFile) {

    units = new ArrayList<Unit>();

    InputStream is = openFile(rosFile);

    if (is == null) {
      return units;
    }





    XmlPullParser xpp = null;

    try {
      System.out.println("BAR - trying to parse " + rosFile);
      xpp = Xml.newPullParser();
      xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      xpp.setInput(is, null);
      // xpp.nextTag();
    } catch (XmlPullParserException xppe) {
      System.out.println("BAR - failed to parse " + rosFile + " (parserexception) " + xppe.getMessage());
      return units;
      // } catch (IOException ioe) {
      //   System.out.println("BAR - failed to parse " + rosFile + " (ioexception) " + ioe.getMessage());
      //   return units;
    }

    System.out.println("BAR - ready to parse " + rosFile);

    if (xpp == null) {
      System.out.println("BAR - xml parser is null for " + rosFile);
      return units;
    }

    try {

      int eventType = xpp.getEventType();

      Unit currentUnit = null;

      Unit rulesUnit = new Unit("Army Rules");
      units.add(rulesUnit);

      // String currentCategory = null;
      Rule currentRule = null;
      Psyker currentPsyker = null;
      Power currentPower = null;
      SubUnit currentSubUnit = null;
      Damage currentDamage = null;
      Weapon currentWeapon = null;

      String inProgress = null;

      int selectionDepth = 0;

      while (eventType != XmlPullParser.END_DOCUMENT) {

        // System.out.println("BAR - event is " + eventType);

        switch (eventType) {
          case XmlPullParser.END_TAG:
            String endName = xpp.getName();
            // System.out.println("BAR - event name is " + name);
            if ("selection".equals(endName)) {
              selectionDepth--;
              // had to allow "upgrade" selection, so may need to do cleanup
              if (selectionDepth == 0) {
                System.out.println("BAR - CLOSING OUT " + inProgress);
                inProgress = NONE;
                if (currentUnit.subUnits.size() == 0) {
                  units.remove(currentUnit);
                  if (currentUnit.rules.size() > 0) {
                    for (Rule r : currentUnit.rules)
                      rulesUnit.rules.add(r);
                  }
                }
              }
            }
            break;
          case XmlPullParser.START_TAG:
            String startName = xpp.getName();
            // System.out.println("BAR - event name is " + name);
            if ("selection".equals(startName)) {

              // some units nest unit profile tags inside of additional selection tags
              selectionDepth++;
              if (selectionDepth == 1) {
                String type = xpp.getAttributeValue(ns, "type");
                // System.out.println("BAR - event type is " + type);

                // "upgrade" seems like an error...
                if ("model".equals(type) || "unit".equals(type) || "upgrade".equals(type)) {
                  String unitName = xpp.getAttributeValue(ns, "name");
                  System.out.println("BAR - unit name is " + unitName);

                  currentUnit = new Unit(unitName);
                  units.add(currentUnit);
                  System.out.println("CHAR - RESET");
                  characteristic1 = "";
                  characteristic2 = "";
                  characteristic3 = "";
                }
              }
            } else if ("category".equals(startName)) {
              String categoryName = xpp.getAttributeValue(ns, "name");
              System.out.println("BAR - category name is " + categoryName);

              if (currentUnit != null) {
                // currentCategory = categoryName;
                if ("Warlord".equals(categoryName) && !NONE.equals(inProgress) && selectionDepth > 0) {
                  System.out.println("BAR - WARLORD? - " + currentUnit.name + " - " + inProgress);
                  currentUnit.warlord = true;
                }
                currentUnit.categories.add(categoryName);
              }
            } else if ("rule".equals(startName)) {
              String ruleName = xpp.getAttributeValue(ns, "name");
              System.out.println("BAR - rule name is " + ruleName);

              // need to account for rules outside of units
              // if (currentUnit != null) {
                currentRule = new Rule(ruleName);
                if (currentUnit != null && selectionDepth > 0) {
                  currentUnit.rules.add(currentRule);
                } else {
                  rulesUnit.rules.add(currentRule);
                }
                inProgress = RULE;
              // }
            } else if ("profile".equals(startName)) {
              String type = xpp.getAttributeValue(ns, "typeName");
              System.out.println("BAR - profile type is " + type);

              if ("Psyker".equals(type)) {
                String psykerName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - psyker name is " + psykerName);

                if (currentUnit != null) {
                  currentPsyker = new Psyker();
                  currentUnit.psyker = currentPsyker;
                  inProgress = PSYKER;
                }
              } else if ("Psychic Power".equals(type)) {
                String powerName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - power name is " + powerName);

                if (currentUnit != null) {
                  currentPower = new Power(powerName);
                  currentUnit.powers.add(currentPower);
                  inProgress = POWER;
                }
              } else if ("Unit".equals(type)) {
                String unitName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - unit name is " + unitName);

                if (currentUnit != null) {
                  currentSubUnit = new SubUnit(unitName);
                  currentUnit.subUnits.add(currentSubUnit);
                  inProgress = UNIT;
                }
              } else if ("Weapon".equals(type)) {
                String weaponName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - weapon name is " + weaponName);

                if (currentUnit != null) {
                  currentWeapon = new Weapon(weaponName);
                  currentUnit.weapons.add(currentWeapon);
                  inProgress = WEAPON;
                }
              } else if (type != null && (type.contains("Wound") || type.contains("Damage"))) {
                //String weaponName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - damage track is " + type);

                if (currentUnit != null) {
                  currentDamage = new Damage();
                  currentUnit.damages.add(currentDamage);
                  inProgress = DAMAGE;
                }
              } else if ("Abilities".equals(type)) {
                String ruleName = xpp.getAttributeValue(ns, "name");
                System.out.println("BAR - rule name is " + ruleName);

                if (currentUnit != null) {
                  currentRule = new Rule(ruleName);
                  currentUnit.rules.add(currentRule);
                  inProgress = RULE;
                }
              }
            } else if ("description".equals(startName)) {
              String ruleDescription = xpp.nextText();
              System.out.println("BAR - rule description is " + ruleDescription);

              currentRule.description = ruleDescription;
            } else if ("characteristic".equals(startName)) {
              String characteristicName = xpp.getAttributeValue(ns, "name");
              System.out.println("BAR - characteristic name is " + characteristicName);

              if (PSYKER.equals(inProgress)) {
                if ("Cast".equals(characteristicName)) {
                  currentPsyker.cast = xpp.nextText();
                } else if ("Deny".equals(characteristicName)) {
                  currentPsyker.deny = xpp.nextText();
                } else if ("Powers Known".equals(characteristicName)) {
                  currentPsyker.powersKnown = xpp.nextText();
                } else if ("Other".equals(characteristicName)) {
                  currentPsyker.other = xpp.nextText();
                }
              } else if (POWER.equals(inProgress)) {
                if ("Warp Charge".equals(characteristicName)) {
                  currentPower.warpCharge = xpp.nextText();
                } else if ("Range".equals(characteristicName)) {
                  currentPower.range = xpp.nextText();
                } else if ("Details".equals(characteristicName)) {
                  currentPower.details = xpp.nextText();
                }
              } else if (UNIT.equals(inProgress)) {
                if ("M".equals(characteristicName)) {
                  currentSubUnit.m = xpp.nextText();
                } else if ("WS".equals(characteristicName)) {
                  currentSubUnit.ws = xpp.nextText();
                } else if ("BS".equals(characteristicName)) {
                  currentSubUnit.bs = xpp.nextText();
                } else if ("S".equals(characteristicName)) {
                  currentSubUnit.s = xpp.nextText();
                } else if ("T".equals(characteristicName)) {
                  currentSubUnit.t = xpp.nextText();
                } else if ("W".equals(characteristicName)) {
                  currentSubUnit.w = xpp.nextText();
                } else if ("A".equals(characteristicName)) {
                  currentSubUnit.a = xpp.nextText();
                } else if ("Ld".equals(characteristicName)) {
                  currentSubUnit.ld = xpp.nextText();
                } else if ("Save".equals(characteristicName)) {
                  currentSubUnit.save = xpp.nextText();
                }
              } else if (WEAPON.equals(inProgress)) {

                if ("Range".equals(characteristicName)) {
                  currentWeapon.range = xpp.nextText();
                } else if ("Type".equals(characteristicName)) {
                  currentWeapon.type = xpp.nextText();
                } else if ("S".equals(characteristicName)) {
                  currentWeapon.s = xpp.nextText();
                } else if ("AP".equals(characteristicName)) {
                  currentWeapon.ap = xpp.nextText();
                } else if ("D".equals(characteristicName)) {
                  currentWeapon.d = xpp.nextText();
                } else if ("Abilities".equals(characteristicName)) {
                  currentWeapon.abilities = xpp.nextText();
                }
              } else if (DAMAGE.equals(inProgress)) {

                System.out.println("BAR - DAMAGE: " + characteristicName);

                String s = "";

                if ("Characteristic 1".equals(characteristicName)) {
                  if (characteristic1.isEmpty()) {
                    s = xpp.nextText();
                    if (Character.isDigit(s.charAt(0))) {
                      // need to build a map
                      foo(currentSubUnit);
                      characteristicName = characteristic1;
                      System.out.println("CHAR - GET?: " + characteristic1);
                    } else {
                      characteristic1 = s;
                      System.out.println("CHAR - SET: " + characteristic1);
                    }
                  } else {
                    characteristicName = characteristic1;
                    System.out.println("CHAR - GET: " + characteristic1);
                  }
                } else if ("Characteristic 2".equals(characteristicName)) {
                  if (characteristic2.isEmpty()) {
                    s = xpp.nextText();
                    if (Character.isDigit(s.charAt(0))) {
                      // need to build a map
                      foo(currentSubUnit);
                      characteristicName = characteristic2;
                      System.out.println("CHAR - GET?: " + characteristic2);
                    } else {
                      characteristic2 = s;
                      System.out.println("CHAR - SET: " + characteristic2);
                    }
                  } else {
                    characteristicName = characteristic2;
                    System.out.println("CHAR - GET: " + characteristic2);
                  }
                } else if ("Characteristic 3".equals(characteristicName)) {
                  if (characteristic3.isEmpty()) {
                    s = xpp.nextText();
                    if (Character.isDigit(s.charAt(0))) {
                      // need to build a map
                      foo(currentSubUnit);
                      characteristicName = characteristic3;
                      System.out.println("CHAR - GET?: " + characteristic3);
                    } else {
                      characteristic3 = s;
                      System.out.println("CHAR - SET: " + characteristic3);
                    }
                  } else {
                    characteristicName = characteristic3;
                    System.out.println("CHAR - GET: " + characteristic3);
                  }
                }

                // if ("Remaining W".equals(characteristicName)) {
                if (characteristicName.contains("Remaining")) {
                  if (s.isEmpty()) {
                    currentDamage.remaining = xpp.nextText();
                  } else {
                    currentDamage.remaining = s;
                  }
                } else if ("Movement".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.m = xpp.nextText();
                  } else {
                    currentDamage.m = s;
                  }
                } else if ("M".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.m = xpp.nextText();
                  } else {
                    currentDamage.m = s;
                  }
                } else if ("WS".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.ws = xpp.nextText();
                  } else {
                    currentDamage.ws = s;
                  }
                } else if ("BS".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.bs = xpp.nextText();
                  } else {
                    currentDamage.bs = s;
                  }
                } else if ("S".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.s = xpp.nextText();
                  } else {
                    currentDamage.s = s;
                  }
                } else if ("T".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.t = xpp.nextText();
                  } else {
                    currentDamage.t = s;
                  }
                } else if ("W".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.w = xpp.nextText();
                  } else {
                    currentDamage.w = s;
                  }
                } else if ("Attacks".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.a = xpp.nextText();
                  } else {
                    currentDamage.a = s;
                  }
                } else if ("A".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.a = xpp.nextText();
                  } else {
                    currentDamage.a = s;
                  }
                } else if ("Ld".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.ld = xpp.nextText();
                  } else {
                    currentDamage.ld = s;
                  }
                } else if ("Save".equals(characteristicName)) {
                  if (s.isEmpty()) {
                    currentDamage.save = xpp.nextText();
                  } else {
                    currentDamage.save = s;
                  }
                } else if ("Relics".equals(characteristicName)) {
                  // weird st. katherine thing
                  if (s.isEmpty()) {
                    currentDamage.remaining = currentDamage.remaining + ", " + xpp.nextText() + " Relics";
                  } else {
                    currentDamage.remaining = currentDamage.remaining + ", " + s + " Relics";;
                  }
                } else if ("Additional attacks".equals(characteristicName)) {
                  // weird disco lord thing
                  if (s.isEmpty()) {
                    currentDamage.a = "+" + xpp.nextText();
                  } else {
                    currentDamage.a = "+" + s;
                  }
                }
              } else if (RULE.equals(inProgress)) {
                if ("Description".equals(characteristicName)) {
                  currentRule.description = xpp.nextText();
                }
              }
            }
            break;
        }

        eventType = xpp.next();

      }

    } catch (XmlPullParserException xppe) {
      xppe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return units;

  }

   */


}
