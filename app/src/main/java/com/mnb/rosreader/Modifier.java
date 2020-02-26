package com.mnb.rosreader;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Modifier implements Parcelable {

  private String name;
  private HashMap<String, Integer> mods;

  public Modifier(String name, HashMap<String, Integer> mods) {
    this.name = name;
    this.mods = mods;
  }

  public Modifier(Parcel parcel) {
    this.name = parcel.readString();
    this.mods = (HashMap<String, Integer>)parcel.readSerializable();
  }

  public static final Creator<Modifier> CREATOR = new Creator<Modifier>() {
    @Override
    public Modifier createFromParcel(Parcel in) {
      return new Modifier(in);
    }

    @Override
    public Modifier[] newArray(int size) {
      return new Modifier[size];
    }
  };

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public HashMap<String, Integer> getMods() {
    return mods;
  }

  public void setMods(HashMap<String, Integer> mods) {
    this.mods = mods;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.name);
    dest.writeSerializable(this.mods);
  }
}
