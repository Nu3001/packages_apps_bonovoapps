package com.bonovo.musicplayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TrackItem implements Serializable {
    public int trackIndex;// �������
    public String trackTitle;// �������
    public String trackPerformer;// ����
    public String waitTime;// �հ�ʱ��
    public String playtime;// �������ʱ��
}

public class CUEManager {
    private String ablumName;// ר�����
    private String ablumPerformer;// ר������
    private String apeFilePath;// ר��Ape�ļ���ַ

    private String filePath;

    private ArrayList<TrackItem> mTrackItems;

    public CUEManager() {

    }

    public CUEManager(String filePath) {
        this.filePath = filePath;
    }

    public boolean parse() {
        mTrackItems = new ArrayList<TrackItem>();
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(new FileInputStream(filePath),
                    Charset.forName("GBK"));
            BufferedReader reader = new BufferedReader(streamReader);
            String line = reader.readLine();
            if (null != line)
                line = line.trim();
            while (null != line && !line.startsWith("TRACK")) {
                if (line.startsWith("PERFORMER")) {
                    ablumPerformer = line.substring(line.indexOf("\"") + 1,
                            line.lastIndexOf("\""));
                } else if (line.startsWith("TITLE")) {
                    ablumName = line.substring(line.indexOf("\"") + 1,
                            line.lastIndexOf("\""));
                } else if (line.startsWith("FILE")) {
                    apeFilePath = line.substring(line.indexOf("\"") + 1,
                            line.lastIndexOf("\""));
                }
                line = reader.readLine().trim();
            }

            setupTrackItems(line, reader);

        } catch (IOException e) {
            e.printStackTrace();
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
        return true;
    }

    private void setupTrackItems(String line, BufferedReader reader)
            throws IOException {
        while (null != line && line.startsWith("TRACK")) {
            TrackItem item = new TrackItem();
            Pattern pattern = Pattern.compile("([0-9]{2})");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find())
                item.trackIndex = Integer.parseInt(matcher.group());
            line = reader.readLine().trim();
            while (null != line && !line.startsWith("TRACK")) {
                if (line.startsWith("PERFORMER")) {
                    item.trackPerformer = line.substring(
                            line.indexOf("\"") + 1, line.lastIndexOf("\""));
                } else if (line.startsWith("TITLE")) {
                    item.trackTitle = line.substring(line.indexOf("\"") + 1,
                            line.lastIndexOf("\""));
                } else if (line.startsWith("INDEX")) {
                    Pattern indexpattern = Pattern.compile("([0-9]{2})");
                    Matcher indexmatcher = indexpattern.matcher(line);
                    if (indexmatcher.find()) {
                        int indexNo = Integer.parseInt(indexmatcher.group());

                        Pattern timePattern = Pattern
                                .compile("[0-9]{2}:[0-9]{2}:[0-9]{2}");
                        Matcher timeMatcher = timePattern.matcher(line);
                        if (timeMatcher.find()) {
                            if (0 == indexNo) {
                                item.waitTime = timeMatcher.group();
                            } else if (1 == indexNo) {
                                item.playtime = timeMatcher.group();
                            }
                        }
                    }

                }
                line = reader.readLine();
                if (null != line)
                    line = line.trim();
            }
            mTrackItems.add(item);
        }
    }

    public ArrayList<TrackItem> getSongListResult() {
        return mTrackItems;
    }

    public String getAblumName() {
        return ablumName;
    }

    public String getAblumPerformer() {
        return ablumPerformer;
    }

    public String getApeFilePath() {
        return apeFilePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
