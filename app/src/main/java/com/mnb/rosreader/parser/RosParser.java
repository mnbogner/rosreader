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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class RosParser {

  protected static final String TAG = "MNB.ROS";

  protected static final String ns = null;  // ignore namespaces?

  protected static final String NONE = "none";
  protected static final String SUBUNIT = "subunit";
  protected static final String PSYKER = "psyker";
  protected static final String DAMAGE = "damage";
  protected static final String WEAPON = "weapon";
  protected static final String POWER = "power";
  protected static final String RULE = "rule";

  protected Context context;

  protected XmlPullParser xpp;

  protected ArrayList<Unit> units;
  protected ArrayList<Unit> theEight;

  protected Unit currentUnit;
  protected SubUnit currentSubUnit;
  protected Psyker currentPsyker;
  protected Damage currentDamage;
  protected Weapon currentWeapon;
  protected Power currentPower;
  protected Rule currentRule;
  protected String inProgress;
  protected int selectionDepth;

  protected Unit rulesUnit;

  protected boolean isTheEight;

  protected String characteristic1 = "";
  protected String characteristic2 = "";
  protected String characteristic3 = "";

  public abstract ArrayList<String> getRosFileList();

  protected abstract InputStream openRosFile(String rosFile);
  protected abstract InputStream openRoszFile(String roszFile);

  public RosParser(Context context) {
    this.context = context;
  }

  public InputStream openFile(String file) {

    if (file.endsWith("ros")) {
      return openRosFile(file);
    } else if (file.endsWith("rosz")) {
      return openRoszFile(file);
    } else {
      System.out.println(TAG + " unknown file type: " + file);
      return null;
    }
  }

  protected InputStream unzipFile(InputStream rawInputStream) {

    try {
      String targetFolder = context.getExternalFilesDir(null).getAbsolutePath();

      ZipInputStream zis = new ZipInputStream(rawInputStream);
      ZipEntry ze = zis.getNextEntry();

      // assuming only one file (.rosz -> .ros)
      if (ze != null) {
        String unzippedName = ze.getName();
        File unzippedFile = new File(targetFolder + File.separator + unzippedName);
        File unzippedDir = unzippedFile.getParentFile();
        try {
          if (!unzippedDir.exists()) {
            unzippedDir.mkdirs();
          }
          if (!unzippedFile.exists()) {
            unzippedFile.createNewFile();
          }
        } catch (IOException ioe) {
          System.out.println(TAG + " failed to create file/dir: " + ioe.getMessage());
          return null;
        }

        try {
          FileOutputStream fos = new FileOutputStream(unzippedFile);
          byte[] buffer = new byte[4096];
          int length = 0;
          while ((length = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
            fos.flush();
          }
          fos.close();

          return new FileInputStream(unzippedFile);
        } catch (IOException ioe) {
          System.out.println(TAG + " failed to read/write file: " + ioe.getMessage());
          return null;
        }
      } else {
        System.out.println(TAG + " no zipped files found");
        return null;
      }
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to read zip entries: " + ioe.getMessage());
      return null;
    }
  }

  public ArrayList<Unit> parseRosFile(String rosFile) {

    units = new ArrayList<Unit>();

    InputStream is = openFile(rosFile);

    if (is == null) {
      System.out.println(TAG + " failed to open input stream");
      return units;
    }

    xpp = null;

    try {
      // TODO - verify state/encoding options
      xpp = Xml.newPullParser();
      xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      xpp.setInput(is, null);
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to create parser: " + xppe.getMessage());
      return units;
    }

    if (xpp == null) {
      System.out.println(TAG + " failed to create parser");
      return units;
    }

    // reset tracking variables
    currentUnit = null;
    currentRule = null;
    currentPsyker = null;
    currentPower = null;
    currentSubUnit = null;
    currentDamage = null;
    currentWeapon = null;
    inProgress = null;
    selectionDepth = 0;
    // create a unit to hold unattached rules
    rulesUnit = new Unit("Army Rules");
    units.add(rulesUnit);

    try {
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {

        switch (eventType) {
          case XmlPullParser.START_TAG:
            handleStartTag();
            break;
          case XmlPullParser.END_TAG:
            handleEndTag();
            break;
        }

        eventType = xpp.next();
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
      return units;
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
      return units;
    }

    return units;
  }

  private void handleStartTag() {

    String startName = xpp.getName();
    switch (startName) {
      case "selection":
        handleSelectionTag();
        break;
      case "category":
        handleCategoryTag();
        break;
      case "rule":
        handleRuleTag();
        break;
      case "profile":
        handleProfileTag();
        break;
      case "description":
        handleDescriptionTag();
        break;
      case "characteristic":
        handleCharacteristicTag();
        break;
    }
  }

  private void handleEndTag() {
    String endName = xpp.getName();
    if ("selection".equals(endName)) {
      selectionDepth--;
      if (selectionDepth == 0 || (selectionDepth == 1 && isTheEight)) {
        System.out.println(TAG + " closing out tag " + inProgress);
        inProgress = NONE;
        if (selectionDepth == 0 && isTheEight) {
          isTheEight = false;
        } else {
          // had to allow "upgrade" selections, so cleanup may be needed
          if (currentUnit.subUnits.size() == 0) {
            // remove units with no units
            units.remove(currentUnit);
            if (currentUnit.rules.size() > 0) {
              // if unit had rules, add them to army rule list
              for (Rule r : currentUnit.rules)
                rulesUnit.rules.add(r);
            }
          }
          currentUnit = null;
        }
      }
    }
  }

  private void handleSelectionTag() {
    // some units nest unit profile tags inside of additional selection tags
    selectionDepth++;
    if ((selectionDepth == 1 && !isTheEight) || (selectionDepth == 2 && isTheEight)) {
      String selectionType = xpp.getAttributeValue(ns, "type");
      // some units (abominant) are tagged as "upgrade"
      if ("model".equals(selectionType) || "unit".equals(selectionType) || "upgrade".equals(selectionType)) {
        String unitName = xpp.getAttributeValue(ns, "name");
        if ("The Eight".equals(unitName)) {
          isTheEight = true;
          theEight = new ArrayList<Unit>();
        } else {
          currentUnit = new Unit(unitName);
          units.add(currentUnit);
          if (isTheEight) {
            theEight.add(currentUnit);
          }
          // reset characteristics map
          characteristic1 = "";
          characteristic2 = "";
          characteristic3 = "";
        }
      }
    }
  }

  private void handleCategoryTag() {

    String categoryName = xpp.getAttributeValue(ns, "name");
    if (currentUnit != null) {
      // "Warlord" shows up in various places, need to make sure we're processing an actual unit
      if ("Warlord".equals(categoryName) && !NONE.equals(inProgress) && selectionDepth > 0) {
        currentUnit.warlord = true;
      }
      currentUnit.categories.add(categoryName);
    }
  }

  private void handleRuleTag() {

    String ruleName = xpp.getAttributeValue(ns, "name");
    currentRule = new Rule(ruleName);
    // if we're not parsing a unit, collect rules to display separately
    if (isTheEight && currentUnit != null && selectionDepth > 1) {
      currentUnit.rules.add(currentRule);
    } else if (!isTheEight && currentUnit != null && selectionDepth > 0) {
      currentUnit.rules.add(currentRule);
    } else {
      rulesUnit.rules.add(currentRule);
    }
    inProgress = RULE;
  }

  private void handleProfileTag() {

    String typeName = xpp.getAttributeValue(ns, "typeName");
    // in general, we only care about these tags while parsing a unit
    switch (typeName) {
      case "Psyker":
        String psykerName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentPsyker = new Psyker();
          currentUnit.psyker = currentPsyker;
          inProgress = PSYKER;
        }
        break;
      case "Psychic Power":
        String powerName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentPower = new Power(powerName);
          currentUnit.powers.add(currentPower);
          inProgress = POWER;
        }
        break;
      case "Unit":
        String unitName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentSubUnit = new SubUnit(unitName);
          currentUnit.subUnits.add(currentSubUnit);
          inProgress = SUBUNIT;
        }
        break;
      case "Weapon":
        String weaponName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentWeapon = new Weapon(weaponName);
          currentUnit.weapons.add(currentWeapon);
          inProgress = WEAPON;
        }
        break;
      case "Abilities":
        String ruleName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          if (isTheEight) {
            System.out.println("EIGHT - UNIT: " + currentUnit.name);
          }
          currentRule = new Rule(ruleName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;
        } else if (isTheEight) {
          System.out.println("EIGHT - ABILITY: " + ruleName);
          currentRule = new Rule(ruleName);
          rulesUnit.rules.add(currentRule);
          inProgress = RULE;
        }
        break;
      case "Tally":
        // weird epidemius thing
        String tallyName = "Tally " + xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(tallyName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;
        }
        break;
      case "Warp Vortex - D6 Roll":
        // weird mutalith thing
        String vortexName = xpp.getAttributeValue(ns, "name");
        if (vortexName.equals("-")) {
          vortexName = "Warp Vortex";
        }
        if (currentUnit != null) {
          currentRule = new Rule(vortexName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;
        }
        break;
      default:
        // need to catch inconsistent damage track tags
        if (typeName != null && (typeName.contains("Wound") || typeName.contains("Damage"))) {
          String damageName = xpp.getAttributeValue(ns, "name");
          if (currentUnit != null) {
            currentDamage = new Damage(damageName);
            currentUnit.damages.add(currentDamage);
            inProgress = DAMAGE;
          }
        }
    }
  }

  private void handleDescriptionTag() {

    try {
      String ruleDescription = xpp.nextText();
      currentRule.description = ruleDescription;
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void handleCharacteristicTag() {

    String characteristicName = xpp.getAttributeValue(ns, "name");
    try {
      switch (inProgress) {
        case SUBUNIT:
          switch (characteristicName) {
            case "M":
              currentSubUnit.m = xpp.nextText();
              break;
            case "WS":
              currentSubUnit.ws = xpp.nextText();
              break;
            case "BS":
              currentSubUnit.bs = xpp.nextText();
              break;
            case "S":
              currentSubUnit.s = xpp.nextText();
              break;
            case "T":
              currentSubUnit.t = xpp.nextText();
              break;
            case "W":
              currentSubUnit.w = xpp.nextText();
              break;
            case "A":
              currentSubUnit.a = xpp.nextText();
              break;
            case "Ld":
              currentSubUnit.ld = xpp.nextText();
              break;
            case "Save":
              currentSubUnit.save = xpp.nextText();
              break;
          }
          break;
        case PSYKER:
          switch (characteristicName) {
            case "Cast":
              currentPsyker.cast = xpp.nextText();
              break;
            case "Deny":
              currentPsyker.deny = xpp.nextText();
              break;
            case "Powers Known":
              currentPsyker.powersKnown = xpp.nextText();
              break;
            case "Other":
              currentPsyker.other = xpp.nextText();
              break;
          }
          break;
        case DAMAGE:
          handleDamageTrack(characteristicName);
          break;
        case WEAPON:
          switch (characteristicName) {
            case "Range":
              currentWeapon.range = xpp.nextText();
              break;
            case "Type":
              currentWeapon.type = xpp.nextText();
              break;
            case "S":
              currentWeapon.s = xpp.nextText();
              break;
            case "AP":
              currentWeapon.ap = xpp.nextText();
              break;
            case "D":
              currentWeapon.d = xpp.nextText();
              break;
            case "Abilities":
              currentWeapon.abilities = xpp.nextText();
              break;
          }
          break;
        case POWER:
          switch (characteristicName) {
            case "Warp Charge":
              currentPower.warpCharge = xpp.nextText();
              break;
            case "Range":
              currentPower.range = xpp.nextText();
              break;
            case "Details":
              currentPower.details = xpp.nextText();
              break;
          }
          break;
        case RULE:
          switch (characteristicName) {
            case "Description":
              currentRule.description = xpp.nextText();
              if (isTheEight) {
                System.out.println("EIGHT - DESCRIPTION: " + currentRule.description);
              }
              break;
            case "Effect":
              // weird epidemius thing
              // weird mutalith thing
              currentRule.description = xpp.nextText();
              break;
          }
          break;
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void handleDamageTrack(String characteristicName) {

    System.out.println(TAG + " - DMG - raw: " + characteristicName);

    String s = "";
    // damage tracks are a mess, try to match characteristic names to unit stats or missing values in unit data
    try {
      switch (characteristicName) {
        case "Characteristic 1":
          if (characteristic1.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic1;
            } else {
              characteristic1 = s;
            }
          } else {
            characteristicName = characteristic1;
          }
          break;
        case "Characteristic 2":
          if (characteristic2.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic2;
            } else {
              characteristic2 = s;
            }
          } else {
            characteristicName = characteristic2;
          }
          break;
        case "Characteristic 3":
          if (characteristic3.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic3;
            } else {
              characteristic3 = s;
            }
          } else {
            characteristicName = characteristic3;
          }
          break;
      }

      if (s.isEmpty()) {
        s = xpp.nextText();
      }

      System.out.println(TAG + " - DMG - mapped: " + characteristicName);

      switch (characteristicName) {
        case "Movement":
        case "M":
          currentDamage.m = s;
          break;
        case "WS":
          currentDamage.ws = s;
          break;
        case "BS":
          currentDamage.bs = s;
          break;
        case "S":
          currentDamage.s = s;
          break;
        case "T":
          currentDamage.t = s;
          break;
        case "W":
          currentDamage.w = s;
          break;
        case "Attacks":
        case "A":
          currentDamage.a = s;
          break;
        case "Ld":
          currentDamage.ld = s;
          break;
        case "Save":
          currentDamage.save = s;
          break;
        case "Relics":
          // weird st. katherine thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Relics";
          break;
        case "Additional attacks":
          // weird disco lord thing
          currentDamage.a = "+" + s;
          break;
        case "Snapping Claws":
          // weird keeper of secrets thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Claws";
          break;
        case "Psychic Test Bonus":
          // weird lord of change thing
          if (s.length() > 1) {
            currentDamage.remaining = currentDamage.remaining + ", " + s + " Psychic";
          } else {
            currentDamage.remaining = currentDamage.remaining + ", +" + s + " Psychic";
          }
          break;
        case "Void Shield":
          // weird titan thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Void";
          break;
        case "Host of Plagues":
          // weird mortarion thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Plague";
          break;
        case "Vortex Power":
          // weird mutalith thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Vortex";
          break;
        case "Psychic Overload":
          // weird maleceptor thing
          currentDamage.remaining = currentDamage.remaining + ", " + s;
          break;
        default:
          // need to catch inconsistent remaining wounds tags
          if (characteristicName.contains("Remaining")) {
            currentDamage.remaining = s;
          }
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void buildCharacteristicMap (SubUnit su) {
    if (su.m.equals("*")) {
      setCharacteristic("M");
    }
    if (su.ws.equals("*")) {
      setCharacteristic("WS");
    }
    if (su.bs.equals("*")) {
      setCharacteristic("BS");
    }
    if (su.s.equals("*")) {
      setCharacteristic("S");
    }
    if (su.a.equals("*")) {
      setCharacteristic("A");
    }
  }

  private void setCharacteristic(String c) {
    if (characteristic1.isEmpty()) {
      characteristic1 = c;
      System.out.println(TAG + " - DMG - build map, c1: " + c);
    } else if (characteristic2.isEmpty()) {
      characteristic2 = c;
      System.out.println(TAG + " - DMG - build map, c2: " + c);
    } else if (characteristic3.isEmpty()) {
      characteristic3 = c;
      System.out.println(TAG + " - DMG - build map, c3: " + c);
    }
  }
}
