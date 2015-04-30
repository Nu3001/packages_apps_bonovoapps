package com.bonovo.musicplayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LRCManager {
    private static class Item {
        public int Minute;
        public int Second;
        public String Content;
    }

    private String mFileName = "";
    private int mFetchRowNum = 5;
    private final ArrayList<Item> mListToContents;
    private String mTitle;
    private String mAuthor;
    private String mAlbum;
    private boolean mIsFileChanged = false;

    public LRCManager() {
        mListToContents = new ArrayList<Item>();
    }

    public void setPath(String fileName) {
        if (fileName == null || 0 == fileName.trim().length()) {
            throw new IllegalArgumentException(
                    " fileName can not null or empty");
        }

        if (fileName != mFileName) {
            mFileName = fileName;
            mIsFileChanged = true;

        } else {
            mIsFileChanged = false;
        }
    }

    public boolean parse() {
        /*
         * if(mFileName==null||0==mFileName.trim().length()){ throw new
         * IllegalStateException("please call setPath method!"); }
         */

        if (!mIsFileChanged) {
            return true;
        }

        mListToContents.clear();
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(
                    new FileInputStream(mFileName), Charset.forName("GBK"));
            BufferedReader reader = new BufferedReader(streamReader);
            String line = reader.readLine();
            Pattern pattern = Pattern
                    .compile("(\\[[0-9]{2}:[0-9]{2}\\.[0-9]{2}\\])");
            while (null != line) {
                ArrayList<Item> items = getItems(line, pattern);
                if (items.size() > 0) {
                    mListToContents.addAll(items);
                } else if (line.startsWith("[ti")) {
                    mTitle = getContent(line);
                } else if (line.startsWith("[ar")) {
                    mAuthor = getContent(line);
                } else if (line.startsWith("[al")) {
                    mAlbum = getContent(line);
                }
                line = reader.readLine();
            }
            sort();
            mIsFileChanged = false;
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (null != streamReader) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }
    }

    private void sort() {
        Collections.sort(mListToContents, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (o1.Minute > o2.Minute) {
                    return 1;
                } else if (o1.Minute < o2.Minute) {
                    return -1;
                } else {
                    return o1.Second - o2.Second;
                }
            }
        });
    }

    private ArrayList<Item> getItems(String line, Pattern pattern) {
        ArrayList<Item> items = new ArrayList<Item>();
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String content = line.substring(line.lastIndexOf(']') + 1,
                    line.length()).trim();
            String strTime = matcher.group();
            int minute = Integer.parseInt(strTime.substring(1, 3));
            int second = Integer.parseInt(strTime.substring(4, 6));
            Item item = new Item();
            item.Minute = minute;
            item.Second = second;
            item.Content = content;
            items.add(item);

        }
        return items;
    }

    private String getContent(String line) {
        int index = line.indexOf(':');
        return line.substring(index + 1, line.length() - 1).trim();
    }

    public void setFetchRow(int num) {
        mFetchRowNum = num;
    }

    public ArrayList<String> getRows(int minute, int second) {
        ArrayList<String> contents = new ArrayList<String>(mFetchRowNum);
        int index = 0;
        for (Item item : mListToContents) {
            if (item.Minute > minute
                    || (item.Minute == minute && item.Second > second)) {
                break;
            }
            index++;
        }
        int currentIndex = index - 1;
        int pre = mFetchRowNum / 2 - (mFetchRowNum % 2 == 0 ? 1 : 0);
        int next = mFetchRowNum - pre - 1;
        for (int i = currentIndex - pre; i <= currentIndex + next; i++) {
            if (i >= 0 && i < mListToContents.size()) {
                contents.add(mListToContents.get(i).Content);
            } else {
                contents.add("");
            }
        }

        return contents;
    }

    public ArrayList<String> getRows(int totalSecond) {
        int minute = totalSecond / 60;
        int second = totalSecond % 60;
        return getRows(minute, second);

    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getAlbum() {
        return mAlbum;
    }

}
