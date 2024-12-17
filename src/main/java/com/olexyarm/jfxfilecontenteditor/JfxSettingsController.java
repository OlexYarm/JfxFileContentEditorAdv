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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxSettingsController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxSettingsController.class);

    @FXML
    public GridPane gridPaneSettings;

    @FXML
    public RadioButton settingsShowToolbarEnable;

    @FXML
    public RadioButton settingsShowToolbarDisable;

    @FXML
    public RadioButton settingsAutosaveEnable;

    @FXML
    public RadioButton settingsAutosaveDisable;

    @FXML
    public TextField settingsAutosaveNum;

    @FXML
    public RadioButton settingsBackupEnable;

    @FXML
    public RadioButton settingsBackupDisable;

    @FXML
    public TextField settingsBackupNum;

    @FXML
    public TextField settingsTabsNum;

    @FXML
    public TextField settingsFontSize;

    @FXML
    public ListView settingsFontFamily;

    @FXML
    public ChoiceBox cbSettingsLogLevel;

    @FXML
    public Button lblSettingsDone;

    // -------------------------------------------------------------------------------------
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {

        LOGGER.debug("### JfxSettingsController initialize.");

        // -------------------------------------------------------------------------------------
        // Show Toolbar
        final ToggleGroup grpShowToolbar = new ToggleGroup();
        this.settingsShowToolbarEnable.setToggleGroup(grpShowToolbar);
        this.settingsShowToolbarDisable.setToggleGroup(grpShowToolbar);

        if (Settings.BOO_SHOW_TOOLBAR_EABLED) {
            this.settingsShowToolbarEnable.setToggleGroup(grpShowToolbar);
            this.settingsShowToolbarEnable.setSelected(true);
            this.settingsShowToolbarEnable.setFocusTraversable(true);
        } else {
            this.settingsShowToolbarDisable.setToggleGroup(grpShowToolbar);
            this.settingsShowToolbarDisable.setSelected(true);
            this.settingsShowToolbarDisable.setFocusTraversable(true);
        }

        grpShowToolbar.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton rb = (RadioButton) new_toggle;
                String strID = rb.getId();
                if (strID == null) {
                    LOGGER.error("Could not get RadioButton ID of settings Show Toolbar."
                            + " ov=" + ov
                            + " old_toggle=" + old_toggle
                            + " new_toggle=" + new_toggle);
                } else {
                    if (strID.equalsIgnoreCase("settingsShowToolbarEnable")) {
                        settingsShowToolbarEnable.setSelected(true);
                        settingsShowToolbarEnable.setFocusTraversable(true);
                        Settings.setShowToolbarEnabled(true);
                        LOGGER.info("Changed settings Autosave-Files Enable."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle
                                + " ID=" + strID);
                    } else if (strID.equalsIgnoreCase("settingsShowToolbarDisable")) {
                        settingsShowToolbarDisable.setSelected(true);
                        settingsShowToolbarDisable.setFocusTraversable(true);
                        Settings.setShowToolbarEnabled(false);
                        LOGGER.info("Changed settings Autosave-Files Disable."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle
                                + " ID=" + strID);
                    }
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Autosave Files
        final ToggleGroup grpAutosaveFiles = new ToggleGroup();
        this.settingsAutosaveEnable.setToggleGroup(grpAutosaveFiles);
        this.settingsAutosaveDisable.setToggleGroup(grpAutosaveFiles);

        if (Settings.BOO_AUTO_SAVE_FILES_EABLED) {
            this.settingsAutosaveEnable.setToggleGroup(grpAutosaveFiles);
            this.settingsAutosaveEnable.setSelected(true);
            this.settingsAutosaveEnable.setFocusTraversable(true);
        } else {
            this.settingsAutosaveDisable.setToggleGroup(grpAutosaveFiles);
            this.settingsAutosaveDisable.setSelected(true);
            this.settingsAutosaveDisable.setFocusTraversable(true);
        }

        grpAutosaveFiles.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton rb = (RadioButton) new_toggle;
                String strID = rb.getId();
                if (strID == null) {
                    LOGGER.error("Could not get RadioButton ID of settings Autosave-Files."
                            + " ov=" + ov
                            + " old_toggle=" + old_toggle
                            + " new_toggle=" + new_toggle);
                } else {
                    if (strID.equalsIgnoreCase("settingsAutosaveEnable")) {
                        settingsAutosaveEnable.setSelected(true);
                        settingsAutosaveEnable.setFocusTraversable(true);
                        Settings.setAutosaveEnabled(true);
                        settingsAutosaveNum.setDisable(false);
                        LOGGER.info("Changed settings Autosave-Files Enable."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle
                                + " ID=" + strID);
                    } else if (strID.equalsIgnoreCase("settingsAutosaveDisable")) {
                        settingsAutosaveDisable.setSelected(true);
                        settingsAutosaveDisable.setFocusTraversable(true);
                        Settings.setAutosaveEnabled(false);
                        settingsAutosaveNum.setDisable(true);
                        LOGGER.info("Changed settings Autosave-Files Disable."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle
                                + " ID=" + strID);
                    }
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Autosave Files Interval
        if (Settings.isAutosaveEnabled()) {
            this.settingsAutosaveNum.setText("" + Settings.getAutosaveInterval());
            this.settingsAutosaveNum.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        if (newValue != null && !newValue.isEmpty()) {
                            int intNewValue = Integer.parseInt(newValue);
                            Settings.setAutosaveInterval(intNewValue);
                            LOGGER.info("Changed settings Autosave-Files Number."
                                    + " observable=" + observable
                                    + " oldValue=\"" + oldValue + "\""
                                    + " newValue=\"" + newValue + "\"");
                        }
                    } catch (Exception e) {
                        //observable.setValue(oldValue);
                        LOGGER.error("Couls not changed settings Autosave-Files Number."
                                + " observable=" + observable
                                + " oldValue=\"" + oldValue + "\""
                                + " newValue=\"" + newValue + "\""
                                + "Exception=\"" + e.toString() + "\"");
                        settingsAutosaveNum.setText(oldValue);
                    }
                }
            });
        }

        // -------------------------------------------------------------------------------------
        // Backup Files Enable/Disable
        final ToggleGroup grpBackupFiles = new ToggleGroup();
        this.settingsBackupEnable.setToggleGroup(grpBackupFiles);
        this.settingsBackupDisable.setToggleGroup(grpBackupFiles);

        if (Settings.BOO_BACKUP_FILES_EABLED) {
            this.settingsBackupEnable.setToggleGroup(grpBackupFiles);
            this.settingsBackupEnable.setSelected(true);
            this.settingsBackupEnable.setFocusTraversable(true);
        } else {
            this.settingsBackupDisable.setToggleGroup(grpBackupFiles);
            this.settingsBackupDisable.setSelected(true);
            this.settingsBackupDisable.setFocusTraversable(true);
        }

        grpBackupFiles.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                LOGGER.debug("Changed settings Backup-Files."
                        + " ov=" + ov
                        + " old_toggle=" + old_toggle
                        + " new_toggle=" + new_toggle);
                if (new_toggle != null) {
                    RadioButton rb = (RadioButton) new_toggle;
                    String strID = rb.getId();
                    if (strID == null) {
                        LOGGER.error("Could not get RadioButton ID of settings Backup-Files."
                                + " ov=" + ov
                                + " old_toggle=" + old_toggle
                                + " new_toggle=" + new_toggle);
                    } else {
                        if (strID.equalsIgnoreCase("settingsBackupEnable")) {
                            settingsBackupEnable.setSelected(true);
                            settingsBackupEnable.setFocusTraversable(true);
                            Settings.BOO_BACKUP_FILES_EABLED = true;
                            settingsBackupNum.setDisable(false);
                            LOGGER.info("Changed settings Backup-Files Enable."
                                    + " ov=" + ov
                                    + " old_toggle=" + old_toggle
                                    + " new_toggle=" + new_toggle
                                    + " ID=" + strID);
                        } else if (strID.equalsIgnoreCase("settingsBackupDisable")) {
                            settingsBackupDisable.setSelected(true);
                            settingsBackupDisable.setFocusTraversable(true);
                            Settings.BOO_BACKUP_FILES_EABLED = false;
                            settingsBackupNum.setDisable(true);
                            LOGGER.info("Changed settings Backup-Files Disable."
                                    + " ov=" + ov
                                    + " old_toggle=" + old_toggle
                                    + " new_toggle=" + new_toggle
                                    + " ID=" + strID);
                        }
                    }
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Backup Files Number
        this.settingsBackupNum.setText("" + Settings.INT_BACKUP_FILES_MAX);
        if (Settings.BOO_BACKUP_FILES_EABLED == false) {
            this.settingsBackupNum.setDisable(true);
        }
        this.settingsBackupNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    if (newValue != null && !newValue.isEmpty()) {
                        int intNewValue = Integer.parseInt(newValue);
                        Settings.INT_BACKUP_FILES_MAX = intNewValue;
                        LOGGER.info("Changed settings Backup-Files Number."
                                + " observable=" + observable
                                + " oldValue=\"" + oldValue + "\""
                                + " newValue=\"" + newValue + "\"");
                    }
                } catch (Exception e) {
                    //observable.setValue(oldValue);
                    LOGGER.error("Couls not changed settings Backup-Files Number."
                            + " observable=" + observable
                            + " oldValue=\"" + oldValue + "\""
                            + " newValue=\"" + newValue + "\""
                            + "Exception=\"" + e.toString() + "\"");
                    settingsBackupNum.setText(oldValue);
                }
            }
        });

        // -------------------------------------------------------------------------------------
        // Tabs Number
        this.settingsTabsNum.setText("" + Settings.INT_TABS_COUNT_MAX);
        this.settingsTabsNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    if (newValue != null && !newValue.isEmpty()) {
                        int intNewValue = Integer.parseInt(newValue);
                        Settings.INT_TABS_COUNT_MAX = intNewValue;
                        LOGGER.info("Changed settings Tabs Number."
                                + " observable=" + observable
                                + " oldValue=\"" + oldValue + "\""
                                + " newValue=\"" + newValue + "\"");
                    }
                } catch (Exception e) {
                    //observable.setValue(oldValue);
                    LOGGER.error("Could not changed settings Tabss Number."
                            + " observable=" + observable
                            + " oldValue=\"" + oldValue + "\""
                            + " newValue=\"" + newValue + "\""
                            + "Exception=\"" + e.toString() + "\"");
                    settingsTabsNum.setText(oldValue);
                }
            }
        });

        // -------------------------------------------------------------------------------------
        this.settingsFontSize.setText("" + Settings.getFontSizeDefault());

        // -------------------------------------------------------------------------------------
        this.settingsFontFamily.setItems(Settings.OBS_LST_VIEW_FONT_FAMILIES);
        this.settingsFontFamily.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Settings.setFontFamilyDefault(newValue);
                LOGGER.info("Salected Font Family."
                        + " FontFamily=" + newValue);
            }
        });
        // -------------------------------------------------------------------------------------

        ObservableList<String> olstLogLevel = cbSettingsLogLevel.getItems();
        this.cbSettingsLogLevel.setValue(Settings.getLogLevel());
        olstLogLevel.addAll("OFF", "Error", "Info", "Warning", "Debug", "Trace");
        this.cbSettingsLogLevel.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Settings.setFontFamilyDefault(newValue);
                LOGGER.info("Salected Log Level."
                        + " LogLevel=" + newValue);
                Settings.applyLogLevel(newValue);
            }
        });

        //cbSettingsLogLevel.addEventHandler(EventType.ROOT, eh);
        // -------------------------------------------------------------------------------------
    }

    // -------------------------------------------------------------------------------------
    // Methodss
    // -------------------------------------------------------------------------------------
    @FXML
    private void settingsSave(ActionEvent actionEvent) throws IOException {

        fontSizeSelect(null);
        Settings.save();
        LOGGER.info("Saved settings.");
        Stage stage = (Stage) this.lblSettingsDone.getScene().getWindow();
        stage.close();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSizeSelect(ActionEvent actionEvent) {

        String strFontSize = this.settingsFontSize.getText();
        if (strFontSize == null || strFontSize.isEmpty()) {
            return;
        }
        double dblFontSize;
        try {
            dblFontSize = Double.parseDouble(strFontSize);
        } catch (Exception e) {
            LOGGER.error("Incorrect Font size."
                    + " strFontSize=\"" + strFontSize + "\""
                    + " Exception=\"" + e.toString() + "\"");
            return;
        }
        dblFontSize = Settings.setFontSizeDefault(dblFontSize);
        this.settingsFontSize.setText("" + dblFontSize);
        LOGGER.debug("Changed Font size."
                + " FontSize=" + dblFontSize);
    }
    // -------------------------------------------------------------------------------------
}
