package com.mnb.rosreader.parser;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class RosDownloadParser extends RosParser{

  public RosDownloadParser (Context context) {
    super(context);
  }

  @Override
  protected InputStream openRosFile(String rosFile) {
    try {
      File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File f = new File(downloadDirectory, rosFile);
      return new FileInputStream(f);
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to open " + rosFile);
      return null;
    }
  }

  @Override
  protected InputStream openRoszFile(String roszFile) {
    try {
      File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File f = new File(downloadDirectory, roszFile);
      return unzipFile(new FileInputStream(f));
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to open " + roszFile);
      return null;
    }
  }

  @Override
  public ArrayList<String> getRosFileList() {
    ArrayList<String> rosFileList = new ArrayList<String>();
    File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    if (downloadDirectory.exists()) {
      // get both compressed rosz files and regular ros files
      FilenameFilter roszFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".rosz");
        }
      };
      rosFileList.addAll(Arrays.asList(downloadDirectory.list(roszFilter)));
      FilenameFilter rosFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".ros");
        }
      };
      rosFileList.addAll(Arrays.asList(downloadDirectory.list(rosFilter)));
    } else {
      System.out.println(TAG + " download directory " + downloadDirectory.getPath() + " not found");
    }
    return rosFileList;
  }
}
