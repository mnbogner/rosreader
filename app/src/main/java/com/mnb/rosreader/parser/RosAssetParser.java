package com.mnb.rosreader.parser;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RosAssetParser extends RosParser {

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
    // add names of files included in assets here
    rosFileList.add("<rosz file>.rosz");
    rosFileList.add("<ros file>.ros");
    return rosFileList;
  }
}
