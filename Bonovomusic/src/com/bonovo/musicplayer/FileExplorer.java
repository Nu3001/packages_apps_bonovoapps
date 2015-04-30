package com.bonovo.musicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zybo on 10/10/14.
 */
public class FileExplorer {
    private static final String ROOT_PATH = "/mnt";
    private static final String[] EXTERNAL_MOUNT_POINTS = new String[]{
            "external_sd",
            "internal_sd",
            "usb_storage"
    };

    private static final String[] EXTERNAL_ROOT_FOLDERS = new String[]{
            "/mnt/external_sd",
            "/mnt/internal_sd",
            "/mnt/usb_storage"
    };

    private FileExplorer() {
    }

    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static String[] getExternalRootFolders() {
        return EXTERNAL_ROOT_FOLDERS;
    }

    public static String getParentPath(String path) {
        if (path.equals(ROOT_PATH))
            return path;
        if (!path.startsWith(ROOT_PATH))
            return null;
        final File file = new File(path);
        return file.getParent();
    }

    public static String[] createList(String path) {
        if (path == null) {
            return createRootList();
        } else {
            final File file = new File(path);
            if (!file.exists()) {
                return createRootList();
            } else {
                if (path.equals(ROOT_PATH))
                    return EXTERNAL_MOUNT_POINTS;
                else
                    return file.list();
            }
        }
    }

    public static boolean isDirectory(final String path) {
        final File file = new File(path);
        if (file.exists())
            return file.isDirectory();
        return false;
    }

    public static String[] createRootList() {
        ArrayList<String> folderList = new ArrayList<String>();
        for (int i = 0; i < EXTERNAL_MOUNT_POINTS.length; i++) {
            final String folderName = EXTERNAL_MOUNT_POINTS[i];
            final String path = ROOT_PATH + File.separator + folderName;
            final File dir = new File(path);
            final String[] files = dir.list();
            if (files != null)
                folderList.add(path);
        }
        int size = folderList.size();
        if (size == 0) {
            return null;
        } else {
            final String[] files = new String[size];
            folderList.toArray(files);
            return files;
        }
    }

    public static class FileTraveller {
        private static final String[] IGNORE_DIR = new String[]{
                "LOST.DIR",
                ".android_secure"
        };
        private List<String> mFilePool;

        public FileTraveller(String rootPath) {
            mFilePool = new ArrayList<String>();
            mFilePool.add(rootPath);
        }

        public String nextFile() {
            while (mFilePool.size() != 0) {
                final String filePath = mFilePool.remove(0);
                final File file = new File(filePath);
                if (file.isDirectory()) {
//                    Collections.addAll(mFilePool, file.list());
                    if (file.isHidden())
                        continue;
                    if (file.list() == null)
                        continue;
                    for (String child : file.list()) {
                        boolean skip = false;
                        for (String ignoreFile : IGNORE_DIR) {
                            if (ignoreFile.equals(child)) {
                                skip = true;
                                break;
                            }
                        }
                        if (!skip)
                            mFilePool.add(filePath + File.separator + child);
                    }
                } else if (file.exists()) {
                    return filePath;
                }
            }
            return null;
        }
    }
}
