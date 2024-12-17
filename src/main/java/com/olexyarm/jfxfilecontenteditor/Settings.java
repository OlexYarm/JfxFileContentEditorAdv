/*
 * Copyright (c) 2024, Oleksandr Yarmolenko. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 */
package com.olexyarm.jfxfilecontenteditor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.util.Arrays;

public class Settings {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    // -------------------------------------------------------------------------------------
    // Build properties
    private static final String STR_UNKNOWN = "UNKNOWN";
    public static String STR_VERSION = STR_UNKNOWN;
    public static String STR_BUILD_TIME = STR_UNKNOWN;
    public static String STR_BUILD_JAVA_HOME = STR_UNKNOWN;
    public static String STR_BUILD_OS = STR_UNKNOWN;

    private static final String STR_VERSION_FILENAME = "version.txt";

    static {
        try (InputStream is = Settings.class.getResourceAsStream(STR_VERSION_FILENAME); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String strLine;
            int intLineCount = 0;
            while ((strLine = br.readLine()) != null) {
                intLineCount++;
                if (strLine.startsWith("Build.version")) {
                    STR_VERSION = strLine;
                } else if (strLine.startsWith("Build.date")) {
                    STR_BUILD_TIME = strLine;
                } else if (strLine.startsWith("Build.JavaHome")) {
                    STR_BUILD_JAVA_HOME = strLine;
                } else if (strLine.startsWith("Build.OS")) {
                    STR_BUILD_OS = strLine;
                } else {
                    LOGGER.info("Unknown line in Version file."
                            + " VersionFileName=\"" + STR_VERSION_FILENAME + "\""
                            + " LineNumber=" + intLineCount
                            + " Line=\"" + strLine + "\""
                    );
                }
            }
            LOGGER.info("Parsed Version file."
                    + " VersionFileName=\"" + STR_VERSION_FILENAME + "\""
                    + " STR_VERSION=\"" + STR_VERSION + "\""
                    + " STR_BUILD_TIME=\"" + STR_BUILD_TIME + "\""
                    + " STR_BUILD_JAVA_HOME=\"" + STR_BUILD_JAVA_HOME + "\""
                    + " STR_BUILD_OS=\"" + STR_BUILD_OS + "\"");
        } catch (IOException ex) {
            LOGGER.error("Could not read Version file."
                    + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                    + " IOException=\"" + ex.toString() + "\"");
        } catch (Throwable t) {
            LOGGER.error("Could not read Version file."
                    + " VersionFilePath=\"" + STR_VERSION_FILENAME + "\""
                    + " Throwable=\"" + t.toString() + "\"");
        }
    }

    // -------------------------------------------------------------------------------------
    // Current OS properties
    public static final String STR_OS_NAME = System.getProperty("os.name");

    private static final String STR_DIRECTORY_USER_HOME = "user.home";
    public static final String STR_DIRECTORY_USER_HOME_PATH = System.getProperty(STR_DIRECTORY_USER_HOME);

    private static final String STR_DIRECTORY_USER_DIR = "user.dir";
    public static final String STR_DIRECTORY_USER_HOME_DIR = System.getProperty(STR_DIRECTORY_USER_DIR);

    private static final List<String> LST_FONT_FAMILIES = Font.getFamilies();
    public static final ObservableList<String> OBS_LST_VIEW_FONT_FAMILIES = FXCollections.observableArrayList(LST_FONT_FAMILIES);

    public static final Font FONT_OS_DEFAULT = Font.getDefault();
    public static final String STR_FONT_FAMILY_OS_DEFAULT = FONT_OS_DEFAULT.getFamily();
    public static final double DOUBLE_FONT_SIZE_OS_DEFAULT = FONT_OS_DEFAULT.getSize();

    public static final SortedMap<String, Charset> MAP_CHARSETS_AVAILABLE = Charset.availableCharsets();
    public static final Charset CHARSET_OS_DEFAULT = Charset.defaultCharset();
    public static final String STR_CHARSET_OS_DEFAULT = CHARSET_OS_DEFAULT.name();

    // -------------------------------------------------------------------------------------
    // Screen sizes
    private static final Dimension DIM_SCREEN_SIZE = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    private static final double DOUBLE_SCREEN_WIDTH = DIM_SCREEN_SIZE.getWidth();
    private static final double DOUBLE_SCREEN_HIGHT = DIM_SCREEN_SIZE.getHeight();

    private static final Rectangle2D RECT_SCREEN_BOUNDS = Screen.getPrimary().getBounds();

    private static final double DOUBLE_SCREEN_SCALED_WIDTH = RECT_SCREEN_BOUNDS.getWidth();
    private static final double DOUBLE_SCREEN_SCALED_HIGHT = RECT_SCREEN_BOUNDS.getHeight();

    public static final int INT_WINDOW_WIDTH_MAX = (int) DOUBLE_SCREEN_SCALED_WIDTH; //8000;
    public static final int INT_WINDOW_HIGH_MAX = (int) DOUBLE_SCREEN_SCALED_HIGHT; //4000;

    // -------------------------------------------------------------------------------------
    // Global counters
    public static int INT_TABS_OPENED_TOTAL_COUNT = 0;
    public static int INT_TABS_OPENED_COUNT = 0;
    public static int INT_TABS_CLOSED_COUNT = 0;
    public static int INT_FILES_NEW_COUNT = 0;
    public static int INT_FILES_OPENED_TOTAL_COUNT = 0;

    // -------------------------------------------------------------------------------------
    // Unmodifiable settings
    // -------------------------------------------------------------------------------------
    public static final String STR_APP_TITLE = "Open JFX File Content Editor";
    public static final String STR_JFX_EDITOR_SETTINGS_DIRECTORY = "JfxEditor";

    // -------------------------------------------------------------------------------------
    // Window About
    public static final int INT_WINDOW_ABOUT_WIDTH = 350;
    public static final int INT_WINDOW_ABOUT_HIGH = 300;

    // -------------------------------------------------------------------------------------
    // Window Settings
    private static final String STR_PROP_NAME_WINDOW_SETTINGS_WIDTH = "window_setttings_widh";
    private static final String STR_PROP_NAME_WINDOW_SETTINGS_HIGH = "window_settings_high";
    private static final int INT_WINDOW_SETTINGS_WIDTH_DEFAULT = 400;
    private static final int INT_WINDOW_SETTINGS_HIGH_DEFAULT = 400;
    public static int INT_WINDOW_SETTINGS_WIDTH = 400;
    public static int INT_WINDOW_SETTINGS_HIGH = 300;

    // -------------------------------------------------------------------------------------
    // Window Main APP
    private static final String STR_PROP_NAME_WINDOW_WIDTH = "window_widh";
    private static final int INT_WINDOW_WIDTH_DEFAULT = 800;
    public static int INT_WINDOW_WIDTH = 800;

    private static final String STR_PROP_NAME_WINDOW_HIGH = "window_high";
    private static final int INT_WINDOW_HIGH_DEFAULT = 600;
    public static int INT_WINDOW_HIGH = 600;

    private static final String STR_PROP_NAME_WINDOW_POSITION_X = "window_position_x";
    private static final int INT_WINDOW_POSITION_X_DEFAULT = 100;
    public static int INT_WINDOW_POSITION_X = 100;

    private static final String STR_PROP_NAME_WINDOW_POSITION_Y = "window_position_y";
    private static final int INT_WINDOW_POSITION_Y_DEFAULT = 100;
    public static int INT_WINDOW_POSITION_Y = 100;

    private static final String STR_PROP_NAME_STAGE_MAXIMAZED = "stage_miximized";
    public static boolean BOO_STAGE_MAXIMIZED;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_BACKUP_FILES_EXT = "BackupFiles_ext";
    public static String STR_BACKUP_FILES_EXT = "bak";

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_FAVORITES_NAME = "Favorites"; // for future use
    private static final String STR_FILE_FAVORITES_NAME = "Favorites";
    private static final String STR_FILE_FAVORITES_EXT = "txt";

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_FAVORITES_DIR = "Favorites_path"; // for future use
    private static final String STR_FILE_FAVORITES_DIR = "";

    // -------------------------------------------------------------------------------------
    public static final String STR_NEW_FILENAME_DEFAULT = "new file";

    // -------------------------------------------------------------------------------------
    public static final String STR_FILENAME_EXT_DEFAULT = "txt";

    // -------------------------------------------------------------------------------------
    public static final String STR_CHARSET_REPLACE_WITH_DEFAULT = "?";

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_NAME = "Settings.properties";
    private static final String STR_SETTINGS_FILE_PATH = Settings.STR_DIRECTORY_USER_HOME_PATH
            + File.separator + STR_JFX_EDITOR_SETTINGS_DIRECTORY
            + File.separator + STR_PROP_NAME_FILE_NAME;
    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FILE_NAME_OPEN_TABS = "OpenTabs.properties";
    public static final String STR_SETTINGS_FILE_PATH_OPEN_TABS = Settings.STR_DIRECTORY_USER_HOME_PATH
            + File.separator + STR_JFX_EDITOR_SETTINGS_DIRECTORY
            + File.separator + STR_PROP_NAME_FILE_NAME_OPEN_TABS;
    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_LAST_OPENED_DIR = "Last-opened-dir";
    private static String STR_SETTINGS_LAST_OPENED_DIR;

    // -------------------------------------------------------------------------------------
    // Modifiable settings
    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_SHOW_TOOLBAR_ENABLED = "Show_Toolbar_enabled";
    private static final boolean BOO_SHOW_TOOLBAR_EABLED_DEFAULT = true;
    public static boolean BOO_SHOW_TOOLBAR_EABLED = false;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_AUTO_SAVE_FILES_ENABLED = "Auto_BakupFiles_enabled";
    private static final boolean BOO_AUTO_SAVE_FILES_EABLED_DEFAULT = false;
    public static boolean BOO_AUTO_SAVE_FILES_EABLED = false;

    private static final String STR_PROP_NAME_AUTO_SAVE_FILES_INTERVAL = "Auto_BakupFiles_interval";
    private static final int INT_AUTO_SAVE_FILES_INTERVAL_MAX = 60; // minutes
    private static final int INT_AUTO_SAVE_FILES_INTERVAL_DEFAULT = 60; // minutes
    private static int INT_AUTO_SAVE_FILES_INTERVAL = 5; // minutes

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_BACKUP_FILES_ENABLED = "BakupFiles_enabled";
    private static final boolean BOO_BACKUP_FILES_EABLED_DEFAULT = true;
    public static boolean BOO_BACKUP_FILES_EABLED = true;

    private static final String STR_PROP_NAME_BACKUP_FILES_DAILY_ONLY = "BackupFiles_DailyOnly";
    private static final boolean BOO_BACKUP_FILES_DAILY_ONLY_DEFAULT = true;
    public static boolean BOO_BACKUP_FILES_DAILY_ONLY;

    private static final String STR_PROP_NAME_BACKUP_FILES_MAX = "BackupFiles_max";
    private static final int INT_BACKUP_FILES_MAX_MAX = 500;
    public static int INT_BACKUP_FILES_MAX = 3;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_TABS_MAX = "Tabs_max";
    private static final int INT_TABS_COUNT_MAX_MAX = 50;
    private static final int INT_TABS_COUNT_MAX_DEFAULT = 3;
    public static int INT_TABS_COUNT_MAX;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_LOG_LEVEL = "Log_level";
    private static final String STR_LOG_LEVEL_DEFAULT = "I";
    private static String STR_LOG_LEVEL;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_TEXT_WRAP = "Text_wrap";
    public static final boolean BOO_TEXT_WRAP_DEFAULT = false;
    private static boolean BOO_TEXT_WRAP;

    // -------------------------------------------------------------------------------------
    private static Font FONT_CURRENT;
    // -------------------------------------------------------------------------------------
    private static final double DOUBLE_FONT_SIZE_MAX = 36;
    private static final double DOUBLE_FONT_SIZE_MIN = 8;
    private static final String STR_PROP_NAME_FONT_SIZE_CURRENT = "Font_size_current";
    private static final double DOUBLE_FONT_SIZE_DELTA = 0.5;
    private static double DOUBLE_FONT_SIZE_CURRENT;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_FONT_FAMILY_CURRENT = "Font_family_current";
    private static String STR_FONT_FAMILY_CURRENT;

    // -------------------------------------------------------------------------------------
    private static final String STR_PROP_NAME_CHARSET_CURRENT = "Charset_current";
    public static String STR_CHARSET_CURRENT;

    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    private static final Properties prop = new Properties();

    // -------------------------------------------------------------------------------------
    public static void load() {

        Path pathFileSettings = FileSystems.getDefault().getPath(STR_SETTINGS_FILE_PATH);

        String strErrMsg = Utils.checkFileExist("SettingsLoad", pathFileSettings);
        if (strErrMsg != null && !strErrMsg.isBlank()) {
            LOGGER.info("File Settings does not exist or not accessible.");
            Utils.createFile(pathFileSettings);
            return;
        }
        LOGGER.debug("Window size."
                + " DIM_SCREEN_SIZE=\"" + DIM_SCREEN_SIZE + "\""
                + " DOUBLE_SCREEN_WIDTH=" + DOUBLE_SCREEN_WIDTH
                + " DOUBLE_SCREEN_HIGHT=" + DOUBLE_SCREEN_HIGHT
                + " RECT_SCREEN_BOUNDS=\"" + RECT_SCREEN_BOUNDS + "\""
                + " DOUBLE_SCREEN_SCALED_WIDTH=" + DOUBLE_SCREEN_SCALED_WIDTH
                + " DOUBLE_SCREEN_SCALED_HIGHT=" + DOUBLE_SCREEN_SCALED_HIGHT
                + " INT_WINDOW_WIDTH_MAX=" + DOUBLE_SCREEN_SCALED_WIDTH
                + " INT_WINDOW_HIGH_MAX=" + DOUBLE_SCREEN_SCALED_HIGHT);

        File fileSettings = new File(STR_SETTINGS_FILE_PATH);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSettings));) {
            prop.load(bis);
        } catch (Exception ex) {
            LOGGER.error("Could not open Settings file."
                    + " pathFileSettings=\"" + pathFileSettings + "\""
                    + " Exception=\"" + ex.toString() + "\"");
            return;
        }

        String strPropValue;
        try {
            INT_WINDOW_WIDTH = getPropValueInt(STR_PROP_NAME_WINDOW_WIDTH, "" + INT_WINDOW_WIDTH_DEFAULT, INT_WINDOW_WIDTH_MAX);
            INT_WINDOW_HIGH = getPropValueInt(STR_PROP_NAME_WINDOW_HIGH, "" + INT_WINDOW_HIGH_DEFAULT, INT_WINDOW_HIGH_MAX);

            INT_WINDOW_POSITION_X = getPropValueInt(STR_PROP_NAME_WINDOW_POSITION_X, "" + INT_WINDOW_POSITION_X_DEFAULT, INT_WINDOW_WIDTH_MAX / 2);
            INT_WINDOW_POSITION_Y = getPropValueInt(STR_PROP_NAME_WINDOW_POSITION_Y, "" + INT_WINDOW_POSITION_Y_DEFAULT, INT_WINDOW_HIGH_MAX / 2);

            INT_WINDOW_SETTINGS_WIDTH = getPropValueInt(STR_PROP_NAME_WINDOW_SETTINGS_WIDTH, "" + INT_WINDOW_SETTINGS_WIDTH_DEFAULT, INT_WINDOW_WIDTH_MAX);
            INT_WINDOW_SETTINGS_HIGH = getPropValueInt(STR_PROP_NAME_WINDOW_SETTINGS_HIGH, "" + INT_WINDOW_SETTINGS_HIGH_DEFAULT, INT_WINDOW_HIGH_MAX);

            BOO_SHOW_TOOLBAR_EABLED = getPropValueBoolean(STR_PROP_NAME_SHOW_TOOLBAR_ENABLED, BOO_SHOW_TOOLBAR_EABLED_DEFAULT ? "Y" : "N");

            BOO_STAGE_MAXIMIZED = getPropValueBoolean(STR_PROP_NAME_STAGE_MAXIMAZED, "N");

            INT_TABS_COUNT_MAX = getPropValueInt(STR_PROP_NAME_TABS_MAX, "" + INT_TABS_COUNT_MAX_DEFAULT, INT_TABS_COUNT_MAX_MAX);

            strPropValue = prop.getProperty(STR_PROP_NAME_LOG_LEVEL);
            if (strPropValue == null) {
                LOGGER.trace("Could not find property \"" + STR_PROP_NAME_LOG_LEVEL + "\"");
                STR_LOG_LEVEL = STR_LOG_LEVEL_DEFAULT;
            } else {
                STR_LOG_LEVEL = strPropValue;
            }
            applyLogLevel(STR_LOG_LEVEL);

            strPropValue = prop.getProperty(STR_PROP_NAME_BACKUP_FILES_EXT);
            if (strPropValue == null) {
                LOGGER.trace("Could not find property \"" + STR_PROP_NAME_BACKUP_FILES_EXT + "\"");
            } else {
                STR_BACKUP_FILES_EXT = strPropValue;
            }

            BOO_SHOW_TOOLBAR_EABLED = getPropValueBoolean(STR_PROP_NAME_SHOW_TOOLBAR_ENABLED, BOO_SHOW_TOOLBAR_EABLED_DEFAULT ? "Y" : "N");

            BOO_AUTO_SAVE_FILES_EABLED = getPropValueBoolean(STR_PROP_NAME_AUTO_SAVE_FILES_ENABLED, BOO_AUTO_SAVE_FILES_EABLED_DEFAULT ? "Y" : "N");
            INT_AUTO_SAVE_FILES_INTERVAL = getPropValueInt(STR_PROP_NAME_AUTO_SAVE_FILES_INTERVAL, "" + INT_AUTO_SAVE_FILES_INTERVAL_DEFAULT, INT_AUTO_SAVE_FILES_INTERVAL_MAX);

            BOO_BACKUP_FILES_EABLED = getPropValueBoolean(STR_PROP_NAME_BACKUP_FILES_ENABLED, BOO_BACKUP_FILES_EABLED_DEFAULT ? "Y" : "N");
            BOO_BACKUP_FILES_DAILY_ONLY = getPropValueBoolean(STR_PROP_NAME_BACKUP_FILES_DAILY_ONLY, BOO_BACKUP_FILES_DAILY_ONLY_DEFAULT ? "Y" : "N");
            INT_BACKUP_FILES_MAX = getPropValueInt(STR_PROP_NAME_BACKUP_FILES_MAX, "" + INT_BACKUP_FILES_MAX, INT_BACKUP_FILES_MAX_MAX);

            DOUBLE_FONT_SIZE_CURRENT = getPropValueDouble(STR_PROP_NAME_FONT_SIZE_CURRENT, "" + DOUBLE_FONT_SIZE_OS_DEFAULT, DOUBLE_FONT_SIZE_MAX);

            strPropValue = prop.getProperty(STR_PROP_NAME_FONT_FAMILY_CURRENT);
            if (strPropValue == null) {
                LOGGER.trace("Could not find property \"" + STR_PROP_NAME_FONT_FAMILY_CURRENT + "\"");
                STR_FONT_FAMILY_CURRENT = STR_FONT_FAMILY_OS_DEFAULT;
            } else {
                if (!OBS_LST_VIEW_FONT_FAMILIES.contains(strPropValue)) {
                    LOGGER.error("Incorrect Font Family in settings file."
                            + " FontFamily=" + strPropValue);
                    STR_FONT_FAMILY_CURRENT = STR_FONT_FAMILY_OS_DEFAULT;
                } else {
                    STR_FONT_FAMILY_CURRENT = strPropValue;
                }
            }

            FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);

            strPropValue = prop.getProperty(STR_PROP_NAME_CHARSET_CURRENT);
            if (strPropValue == null) {
                LOGGER.trace("Could not find property \"" + STR_PROP_NAME_CHARSET_CURRENT + "\"");
                STR_CHARSET_CURRENT = STR_CHARSET_OS_DEFAULT;
            } else {
                STR_CHARSET_CURRENT = strPropValue;
            }

            strPropValue = prop.getProperty(STR_PROP_NAME_LAST_OPENED_DIR);
            if (strPropValue == null || (strPropValue = strPropValue.trim()).isEmpty()) {
                LOGGER.trace("Could not find property \"" + STR_PROP_NAME_LAST_OPENED_DIR + "\"");
                STR_SETTINGS_LAST_OPENED_DIR = STR_DIRECTORY_USER_HOME_PATH
                        + File.separator
                        + STR_JFX_EDITOR_SETTINGS_DIRECTORY;
            } else {
                STR_SETTINGS_LAST_OPENED_DIR = strPropValue;
            }

        } catch (Exception ex) {
            LOGGER.error("Could not process property."
                    + " IOException=\"" + ex.toString() + "\"");
        }

    }

    // -------------------------------------------------------------------------------------
    public static void save() {

        if (!BOO_STAGE_MAXIMIZED) {
            prop.setProperty(STR_PROP_NAME_WINDOW_WIDTH, "" + INT_WINDOW_WIDTH);
            prop.setProperty(STR_PROP_NAME_WINDOW_HIGH, "" + INT_WINDOW_HIGH);

            prop.setProperty(STR_PROP_NAME_WINDOW_POSITION_X, "" + INT_WINDOW_POSITION_X);
            prop.setProperty(STR_PROP_NAME_WINDOW_POSITION_Y, "" + INT_WINDOW_POSITION_Y);
        }

        prop.setProperty(STR_PROP_NAME_WINDOW_SETTINGS_WIDTH, "" + INT_WINDOW_SETTINGS_WIDTH);
        prop.setProperty(STR_PROP_NAME_WINDOW_SETTINGS_HIGH, "" + INT_WINDOW_SETTINGS_HIGH);

        prop.setProperty(STR_PROP_NAME_STAGE_MAXIMAZED, BOO_STAGE_MAXIMIZED ? "Y" : "N");

        prop.setProperty(STR_PROP_NAME_SHOW_TOOLBAR_ENABLED, BOO_SHOW_TOOLBAR_EABLED ? "Y" : "N");

        prop.setProperty(STR_PROP_NAME_AUTO_SAVE_FILES_ENABLED, BOO_AUTO_SAVE_FILES_EABLED ? "Y" : "N");
        prop.setProperty(STR_PROP_NAME_AUTO_SAVE_FILES_INTERVAL, "" + INT_AUTO_SAVE_FILES_INTERVAL);

        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_EXT, STR_BACKUP_FILES_EXT);
        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_ENABLED, BOO_BACKUP_FILES_EABLED ? "Y" : "N");
        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_DAILY_ONLY, BOO_BACKUP_FILES_DAILY_ONLY ? "Y" : "N");
        if (INT_BACKUP_FILES_MAX <= 0) {
            INT_BACKUP_FILES_MAX = 1;
        }
        if (INT_BACKUP_FILES_MAX > INT_BACKUP_FILES_MAX_MAX) {
            INT_BACKUP_FILES_MAX = INT_BACKUP_FILES_MAX_MAX;
        }
        prop.setProperty(STR_PROP_NAME_BACKUP_FILES_MAX, "" + INT_BACKUP_FILES_MAX);

        if (INT_TABS_COUNT_MAX <= 0) {
            INT_TABS_COUNT_MAX = 1;
        }
        if (INT_TABS_COUNT_MAX > INT_TABS_COUNT_MAX_MAX) {
            INT_TABS_COUNT_MAX = INT_TABS_COUNT_MAX_MAX;
        }
        prop.setProperty(STR_PROP_NAME_TABS_MAX, "" + INT_TABS_COUNT_MAX);

        if (STR_FONT_FAMILY_CURRENT == null) {
            STR_FONT_FAMILY_CURRENT = STR_FONT_FAMILY_OS_DEFAULT;
        }
        FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);
        prop.setProperty(STR_PROP_NAME_FONT_SIZE_CURRENT, "" + DOUBLE_FONT_SIZE_CURRENT);
        prop.setProperty(STR_PROP_NAME_FONT_FAMILY_CURRENT, STR_FONT_FAMILY_CURRENT);

        prop.setProperty(STR_PROP_NAME_CHARSET_CURRENT, STR_CHARSET_CURRENT);

        prop.setProperty(STR_PROP_NAME_LOG_LEVEL, STR_LOG_LEVEL);

        prop.setProperty(STR_PROP_NAME_LAST_OPENED_DIR, STR_SETTINGS_LAST_OPENED_DIR);

        LOGGER.trace("Saving Properties file."
                + " prop=\"" + prop.toString() + "\"");

        try {
            OutputStream os = new FileOutputStream(STR_SETTINGS_FILE_PATH);
            prop.store(os, "");
        } catch (IOException ex) {
            LOGGER.error("Could not save Properties file."
                    + " IOException=\"" + ex.toString() + "\"");
        }

    }

    // -------------------------------------------------------------------------------------
    public static String caclulateFavoritesPath() {

        String strFileFavoritesPath;
        if (Settings.STR_FILE_FAVORITES_DIR == null || Settings.STR_FILE_FAVORITES_DIR.isEmpty()) {
            strFileFavoritesPath = Settings.STR_DIRECTORY_USER_HOME_PATH
                    + File.separator + Settings.STR_JFX_EDITOR_SETTINGS_DIRECTORY
                    + File.separator + Settings.STR_FILE_FAVORITES_NAME;
        } else {
            strFileFavoritesPath = Settings.STR_FILE_FAVORITES_DIR + File.separator + Settings.STR_FILE_FAVORITES_NAME;
        }
        strFileFavoritesPath += "." + Settings.STR_FILE_FAVORITES_EXT;

        return strFileFavoritesPath;
    }

    // -------------------------------------------------------------------------------------
    public static String osName() {
        return STR_OS_NAME;
    }

    // -------------------------------------------------------------------------------------
    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    // -------------------------------------------------------------------------------------
    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

    // -------------------------------------------------------------------------------------
    private static boolean getPropValueBoolean(String strPropName, String strPropValueDefault) throws Exception {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        boolean booValue;
        if (strPropValue == null) {
            strPropValue = strPropValueDefault;
        }
        booValue = switch (strPropValue) {
            case "Y", "YES" ->
                true;
            case "N", "NO" ->
                false;
            default ->
                true;
        };
        return booValue;
    }

    // -------------------------------------------------------------------------------------
    private static int getPropValueInt(String strPropName, String strPropValueDefault, int IntPropValueMax) {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        if (strPropValue == null) {
            LOGGER.trace("Could not find property \"" + strPropName + "\"");
            strPropValue = strPropValueDefault;
        }
        int intPropValue = Integer.parseInt(strPropValue);
        if (intPropValue <= 0 || intPropValue > IntPropValueMax) {
            LOGGER.error("Properties value is incorrrect."
                    + " PropName=\"" + strPropName + "\""
                    + " IntPropValueMax=\"" + IntPropValueMax + "\""
                    + " strPropValue=\"" + strPropValue + "\""
                    + " intPropValue=\"" + intPropValue + "\"");
            intPropValue = Integer.parseInt(strPropValueDefault);
        }
        LOGGER.trace("Got int Properties value."
                + " PropName=\"" + strPropName + "\""
                + " IntPropValueMax=\"" + IntPropValueMax + "\""
                + " strPropValue=\"" + strPropValue + "\""
                + " intPropValue=\"" + intPropValue + "\"");
        return intPropValue;
    }

    // -------------------------------------------------------------------------------------
    private static double getPropValueDouble(String strPropName, String strPropValueDefault, double dblPropValueMax) {

        String strPropValue = prop.getProperty(strPropName, strPropValueDefault);
        if (strPropValue == null) {
            LOGGER.trace("Could not find property \"" + strPropName + "\"");
            strPropValue = strPropValueDefault;
        }
        double dblPropValue = Double.parseDouble(strPropValue);
        if (dblPropValue <= 0 || dblPropValue > dblPropValueMax) {
            LOGGER.error("Properties value is incorrrect."
                    + " PropName=\"" + strPropName + "\""
                    + " dblPropValueMax=\"" + dblPropValueMax + "\""
                    + " strPropValue=\"" + strPropValue + "\""
                    + " dblPropValue=\"" + dblPropValue + "\"");
            dblPropValue = Double.parseDouble(strPropValueDefault);
        }
        LOGGER.trace("Got double Properties value."
                + " PropName=\"" + strPropName + "\""
                + " dblPropValueMax=\"" + dblPropValueMax + "\""
                + " strPropValue=\"" + strPropValue + "\""
                + " dblPropValue=\"" + dblPropValue + "\"");
        return dblPropValue;
    }

    // -------------------------------------------------------------------------------------
    public static boolean isShowToolbarEnabled() {
        return BOO_SHOW_TOOLBAR_EABLED;
    }

    // -------------------------------------------------------------------------------------
    public static void setShowToolbarEnabled(boolean booShowToolbarEnabled) {
        BOO_SHOW_TOOLBAR_EABLED = booShowToolbarEnabled;
    }

    // -------------------------------------------------------------------------------------
    public static boolean isAutosaveEnabled() {
        return BOO_AUTO_SAVE_FILES_EABLED;
    }

    // -------------------------------------------------------------------------------------
    public static void setAutosaveEnabled(boolean booAutosaveEnabled) {
        BOO_AUTO_SAVE_FILES_EABLED = booAutosaveEnabled;
    }

    // -------------------------------------------------------------------------------------
    public static int getAutosaveInterval() {
        return INT_AUTO_SAVE_FILES_INTERVAL;
    }

    // -------------------------------------------------------------------------------------
    public static void setAutosaveInterval(int intInterval) {
        INT_AUTO_SAVE_FILES_INTERVAL = intInterval;
    }

    // -------------------------------------------------------------------------------------
    public static double changeFontSize(String strFontSize) {

        double dblFontSize;
        try {
            dblFontSize = Double.parseDouble(strFontSize);
        } catch (NumberFormatException e) {
            LOGGER.error("Could not changed settings Font Size Default."
                    + " strFontSize=\"" + strFontSize + "\""
                    + " NumberFormatException=\"" + e.toString() + "\"");
            return 0;
        }
        dblFontSize = restrictFontSize(dblFontSize);
        return dblFontSize;
    }

    // -------------------------------------------------------------------------------------
    public static double restrictFontSize(double dblFontSize) {

        if (dblFontSize <= DOUBLE_FONT_SIZE_MIN) {
            LOGGER.debug("Font size is too small."
                    + " dblFontSize=" + dblFontSize
                    + " DOUBLE_FONT_SIZE_MIN=" + DOUBLE_FONT_SIZE_MIN);
            dblFontSize = DOUBLE_FONT_SIZE_MIN;
        } else if (dblFontSize > Settings.DOUBLE_FONT_SIZE_MAX) {
            LOGGER.debug("Font size is too large."
                    + " dblFontSize=" + dblFontSize
                    + " DOUBLE_FONT_SIZE_MAX=" + DOUBLE_FONT_SIZE_MAX);
            dblFontSize = DOUBLE_FONT_SIZE_MAX;
        } else {
            LOGGER.debug("Font size changed."
                    + " dblFontSize=" + dblFontSize);
        }
        return dblFontSize;
    }

    // -------------------------------------------------------------------------------------
    public static double increaseFontSize(double dblFontSize, boolean booIncrease) {

        if (booIncrease) {
            dblFontSize += DOUBLE_FONT_SIZE_DELTA;
        } else {
            dblFontSize -= DOUBLE_FONT_SIZE_DELTA;
        }
        dblFontSize = restrictFontSize(dblFontSize);
        return dblFontSize;
    }

    // -------------------------------------------------------------------------------------
    public static Font getFontDefault() {

        return FONT_CURRENT;
    }

    // -------------------------------------------------------------------------------------
    public static String getFontFamilyDefault() {

        return FONT_CURRENT.getFamily();
    }

    // -------------------------------------------------------------------------------------
    public static void setFontFamilyDefault(String strFontFamily) {

        if (!OBS_LST_VIEW_FONT_FAMILIES.contains(strFontFamily)) {
            LOGGER.debug("Incorrect Font Family."
                    + " strFontFamily=" + strFontFamily);
            return;
        }
        STR_FONT_FAMILY_CURRENT = strFontFamily;
        FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);
        save();
    }

    // -------------------------------------------------------------------------------------
    public static double getFontSizeDefault() {

        return Settings.DOUBLE_FONT_SIZE_CURRENT;
    }

    // -------------------------------------------------------------------------------------
    public static double setFontSizeDefault(double dblFontSize) {

        dblFontSize = restrictFontSize(dblFontSize);
        DOUBLE_FONT_SIZE_CURRENT = dblFontSize;
        FONT_CURRENT = new Font(STR_FONT_FAMILY_CURRENT, DOUBLE_FONT_SIZE_CURRENT);
        save();
        return dblFontSize;
    }

    // -------------------------------------------------------------------------------------
    public static ObservableList<String> getObsLstFontCharsetsStandard() {

        //https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html
        List<String> lst = new ArrayList<>(Arrays.asList("US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"));
        ObservableList<String> ol = FXCollections.observableList(lst);
        return ol;
    }

    // -------------------------------------------------------------------------------------
    public static ObservableList<String> getObsLstFontCharsets() {

        Set<String> keyset = MAP_CHARSETS_AVAILABLE.keySet();
        List<String> lst = new ArrayList<>(keyset);
        ObservableList<String> ol = FXCollections.observableList(lst);
        return ol;
    }

    // -------------------------------------------------------------------------------------
    public static String getLogLevel() {
        return STR_LOG_LEVEL;
    }

    // -------------------------------------------------------------------------------------
    public static boolean isTextWtap() {
        return BOO_TEXT_WRAP;
    }

    // -------------------------------------------------------------------------------------
    public static void applyLogLevel(String strLogLevel) {

        String strLogLevelBefore = STR_LOG_LEVEL;
        STR_LOG_LEVEL = strLogLevel;
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Level levelLogBefore = loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").getLevel();
        LOGGER.debug("applyLogLevel before."
                + " strLogLevel=\"" + strLogLevel + "\""
                + " STR_LOG_LEVEL=\"" + STR_LOG_LEVEL + "\""
                + " levelLogBefore=\"" + levelLogBefore + "\"");

        String strLogLevelStartWith;
        if (strLogLevel == null || strLogLevel.isEmpty()) {
            strLogLevelStartWith = "O";
        } else {
            strLogLevelStartWith = strLogLevel.substring(0, 1);
        }
        switch (strLogLevelStartWith) {
            case "T":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.TRACE);
                break;
            case "D":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.DEBUG);
                break;
            case "I":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.INFO);
                break;
            case "W":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.WARN);
                break;
            case "E":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.ERROR);
                break;
            case "O":
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.OFF);
                break;
            default:
                loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").setLevel(Level.OFF);
                break;
        }
        Level levelLogAfter = loggerContext.getLogger("com.olexyarm.jfxfilecontenteditor").getLevel();
        LOGGER.debug("applyLogLevel After."
                + " strLogLevelBefore=\"" + strLogLevelBefore + "\""
                + " STR_LOG_LEVEL=\"" + STR_LOG_LEVEL + "\""
                + " levelLogBefore=\"" + levelLogBefore + "\""
                + " levelLogAfter=\"" + levelLogAfter + "\"");
    }

    // -------------------------------------------------------------------------------------
    public static String getLastOpenedDir() {
        return STR_SETTINGS_LAST_OPENED_DIR;
    }

    public static void setLastOpenedDir(String strLastOpenedDir) {
        if (strLastOpenedDir != null) {
            strLastOpenedDir = strLastOpenedDir.trim();
            if (!strLastOpenedDir.isEmpty()) {
                STR_SETTINGS_LAST_OPENED_DIR = strLastOpenedDir;
            }
        }
    }
    // -------------------------------------------------------------------------------------
}
