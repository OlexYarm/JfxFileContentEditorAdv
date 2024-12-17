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

import static com.olexyarm.jfxfilecontenteditor.Utils.showMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxFileContentEditorMenuController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxFileContentEditorMenuController.class);

    // -------------------------------------------------------------------------------------
    private JfxFileContentEditorController jfxEditorController;
    private BorderPane borderPaneEditor;

    // -------------------------------------------------------------------------------------
    @FXML
    private HBox hboxMenu;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFile;

    @FXML
    private MenuItem miSaveFile;

    @FXML
    private MenuItem miSaveFileAs;

    @FXML
    private MenuItem miSaveFilesAll;

    @FXML
    private MenuItem miPrint;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuEdit;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFont;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem menuFontFamily;

    // -------------------------------------------------------------------------------------
    @FXML
    private ListView menuListViewFontFamily;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem menuFontSize;

    // -------------------------------------------------------------------------------------
    @FXML
    private TextField menuTextFielsFontSize;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem miCharsetRead;

    // -------------------------------------------------------------------------------------
    @FXML
    private CustomMenuItem miCharsetWrite;

    // -------------------------------------------------------------------------------------
    @FXML
    private ListView menuListViewCharsetRead;

    // -------------------------------------------------------------------------------------
    @FXML
    private ListView menuListViewCharsetWrite;

    // -------------------------------------------------------------------------------------
    @FXML
    private Menu menuFavorites;

    // -------------------------------------------------------------------------------------
    @FXML
    private HBox hboxToolbars;

    // -------------------------------------------------------------------------------------
    @FXML
    private ToolBar tbFile;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonNewFile;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonOpenFile;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonSaveFile;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonSaveFileAs;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonSaveFileAll;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonPrint;

    // -------------------------------------------------------------------------------------
    @FXML
    CheckBox cbTextWrap;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonFontIncrease;

    // -------------------------------------------------------------------------------------
    @FXML
    private Button buttonFontDecrease;

    // -------------------------------------------------------------------------------------
    //    @FXML
    //    private ToolBar tbFont;
    // -------------------------------------------------------------------------------------
    private TabPane tabPane;
    private ObservableList<Tab> lstTabs;

    private long lngAutoSaveCount = 0;
    private Timeline timeline;

    private boolean booTextWrap;

    // -------------------------------------------------------------------------------------
    // JFX constructor
    // -------------------------------------------------------------------------------------
    public void initialize() {

        LOGGER.debug("### Initialize JfxFileContentEditorMenuController."
                + " this=\"" + this + "\""
                + " hboxMenu=\"" + hboxMenu + "\""
                + " menuFavorites=\"" + menuFavorites + "\"");
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    public void setParentController(JfxFileContentEditorController jfxEditorController) {

        this.jfxEditorController = jfxEditorController;
        this.borderPaneEditor = this.jfxEditorController.borderPaneEditor;

        this.tabPane = this.jfxEditorController.tabPaneEditor;
        this.lstTabs = this.tabPane.getTabs();

        this.tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab tabFrom, Tab tabTo) {

                if (tabTo == null) {
                    LOGGER.debug("TabPane ChangeListener tabTo is null."
                            + " this=\"" + this + "\"");
                    return;
                }
                String strTabToId = tabTo.getId();
                FileContentEditor fileEditor = (FileContentEditor) tabTo.getContent();

                String strCharsetName = fileEditor.getCharsetName();
                updateTextFieldLineCharsetName(strCharsetName);

                String strLineEnding = fileEditor.getLineEnding();
                updateTextFieldLineEnding(strLineEnding);

                boolean booTextWrap = fileEditor.isTextWrap();
                cbTextWrap.setSelected(booTextWrap);

                LOGGER.debug("TabPane ChangeListener."
                        + " this=\"" + this + "\""
                        //+ " strTabFromId=\"" + strTabFromId + "\""
                        + " strTabToId=\"" + strTabToId + "\""
                        + " strCharsetName=\"" + strCharsetName + "\""
                        + " strLineEnding=\"" + strLineEnding + "\""
                        + " booTextWrap=" + booTextWrap
                        + " observable=\"" + observable + "\""
                        + " tabFrom=\"" + tabFrom + "\""
                        + " tabTo=\"" + tabTo + "\"");
            }
        });

        EventHandler<EventFileRead> eventHandlerFileRead = new EventHandler() {
            @Override
            public void handle(Event event) {

                EventType eventType = event.getEventType();
                Object eventSource = event.getSource();
                EventTarget eventTarget = event.getTarget();

                if (!(event instanceof EventFileRead)) {
                    LOGGER.debug("eventHandlerFileRead unknown event."
                            + " eventType=\"" + eventType + "\""
                            + " eventSource=\"" + eventSource + "\""
                            + " eventTarget=\"" + eventTarget + "\""
                            + " event=\"" + event + "\"");
                    return;
                }
                EventFileRead eventFileRead = (EventFileRead) event;

                String strId = eventFileRead.getId();
                String strLineEnding = eventFileRead.getLineEnding();
                String strCharsetName = eventFileRead.getCharsetName();

                LOGGER.debug("eventHandlerFileRead."
                        + " strId=\"" + strId + "\""
                        + " strLineEnding=\"" + strLineEnding + "\""
                        + " strCharsetName=\"" + strCharsetName + "\""
                        + " eventType=\"" + eventType + "\""
                        + " eventSource=\"" + eventSource + "\""
                        + " eventTarget=\"" + eventTarget + "\""
                        + " event=\"" + event + "\""
                );
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                String strTabId = tab.getId();
                if (strTabId != null && strTabId.equals(strId)) {
                    updateTextFieldLineCharsetName(strCharsetName);
                    updateTextFieldLineEnding(strLineEnding);
                }

                event.consume();
            }
        };
        this.tabPane.addEventHandler(EventFileRead._FILE_READ, eventHandlerFileRead);

        this.cbTextWrap.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == null) {
                    LOGGER.error("Change TextWrap before any tab created.");
                    return;
                }
                FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
                fileEditor.setTextWrap(newValue);

                LOGGER.debug("cbTextWrap ChangeListener."
                        + " this=\"" + this + "\""
                        + " observable=\"" + observable + "\""
                        + " oldValue=\"" + oldValue + "\""
                        + " newValue=\"" + newValue + "\""
                );
            }
        });

        VBox jfxEditorBottom = this.jfxEditorController.jfxEditorBottom;

        LOGGER.debug("### Setting Parent Controller in JfxFileContentEditorMenuController."
                + " this=\"" + this + "\""
                + " jfxEditorController=\"" + jfxEditorController + "\""
                + " hboxMenu=\"" + hboxMenu + "\""
                + " menuFavorites=\"" + menuFavorites + "\""
                + " borderPaneEditor=\"" + this.borderPaneEditor + "\""
                + " tabPane=\"" + this.tabPane + "\""
                + " lstTabs=\"" + this.lstTabs + "\""
                + " jfxEditorBottom=\"" + jfxEditorBottom + "\""
        );

        Settings.load();
        this.addMenuFontFamily();
        this.addMenuCharset();
        this.addFavorites();
        this.restoreOpenedTabs();
        if (Settings.BOO_SHOW_TOOLBAR_EABLED) {
            this.buttonNewFile.setVisible(true);
            this.buttonOpenFile.setVisible(true);
        } else {
            this.buttonNewFile.setVisible(false);
            this.buttonOpenFile.setVisible(false);
            this.buttonSaveFile.setVisible(false);
            this.buttonSaveFileAs.setVisible(false);
            this.buttonSaveFileAll.setVisible(false);
            this.buttonPrint.setVisible(false);
        }
        if (Settings.isAutosaveEnabled()) {
            this.startTimeline();
        }
    }

    // -------------------------------------------------------------------------------------
    private void addMenuFontFamily() {

        ObservableList<String> menuItemsFontFamily = this.menuListViewFontFamily.getItems();
        menuItemsFontFamily.addAll(Settings.OBS_LST_VIEW_FONT_FAMILIES);
    }

    // -------------------------------------------------------------------------------------
    private void addMenuCharset() {

        ObservableList<String> menuItemsCharsetsRead = this.menuListViewCharsetRead.getItems();
        menuItemsCharsetsRead.addAll(Settings.getObsLstFontCharsets());
        ObservableList<String> menuItemsCharsetsWrite = this.menuListViewCharsetWrite.getItems();
        menuItemsCharsetsWrite.addAll(Settings.getObsLstFontCharsets());
    }

    // -------------------------------------------------------------------------------------
    private void addFavorites() {

        ObservableList<MenuItem> menuItemsFavorites = this.menuFavorites.getItems();
        if (menuItemsFavorites == null) {
            // Should never happend.
            LOGGER.error("Menu Favorites is empty.");
            return;
        }

        int intMenuItemsCount = menuItemsFavorites.size();
        menuItemsFavorites.remove(3, intMenuItemsCount);

        String strFileFavoritesPath = Settings.caclulateFavoritesPath();
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPath);

        String strErrMsg = Utils.checkFileExist("addFavorites", pathFileFavorites);
        if (strErrMsg == null || strErrMsg.isBlank()) {
            Charset charset = Charset.forName(Settings.STR_CHARSET_CURRENT);
            try (BufferedReader br = Files.newBufferedReader(pathFileFavorites, charset)) {
                int intLineCount = 0;
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    intLineCount++;
                    strLine = strLine.trim();
                    if (!strLine.isEmpty()) {
                        boolean booFound = false;
                        for (MenuItem item : menuItemsFavorites) {
                            String strMenuFilePath = item.getText();
                            if (strLine.equalsIgnoreCase(strMenuFilePath)) {
                                LOGGER.debug("Favorites FilePath found in Menu"
                                        + " LineNumber=" + intLineCount
                                        + " FileFavoritesPath=\"" + strLine + "\"");
                                booFound = true;
                                break;
                            }
                        }
                        if (booFound) {
                            continue;
                        }
                        final int intLineCountFinal = intLineCount;
                        final String strLineFinal = strLine;
                        MenuItem menuItemNew = new MenuItem(strLine);
                        menuItemNew.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                event.consume();
                                Path pathFile = FileSystems.getDefault().getPath(strLineFinal);
                                if (!openFileinTab(pathFile)) {
                                    return;
                                }
                                LOGGER.debug("Opened FilePath from Favorites Menu."
                                        + " LineNumber=\"" + intLineCountFinal + "\""
                                        + " FileFavoritesPath=\"" + strLineFinal + "\"");
                            }
                        });
                        menuItemsFavorites.add(menuItemNew);
                        LOGGER.debug("Added FilePath to Favorites Menu."
                                + " LineNumber=\"" + intLineCount + "\""
                                + " FileFavoritesPath=\"" + strLine + "\"");
                    }
                }
            } catch (Throwable t) {
                LOGGER.debug("Could not read Favorites File"
                        + " FileFavoritesPath=\"" + pathFileFavorites + "\""
                        + " Throwable=\"" + t.toString() + "\"");
            }
        } else {
            LOGGER.debug("Creating Favorites File"
                    + " FileFavoritesPath=\"" + pathFileFavorites + "\"");
            Utils.createNewFile(pathFileFavorites);
        }
    }

    // -------------------------------------------------------------------------------------
    private void restoreOpenedTabs() {

        List<String> lstOpenedFiles = Utils.openTabsRestore(lstTabs);
        for (String strFilePath : lstOpenedFiles) {
            Path pathFile = Path.of(strFilePath);
            this.openFileinTab(pathFile);
        }
        if (lstOpenedFiles.isEmpty()) {
            cbTextWrap.setDisable(true);
            buttonFontIncrease.setDisable(true);
            buttonFontDecrease.setDisable(true);
        }
    }

    // -------------------------------------------------------------------------------------
    private void startTimeline() {

        int intAutosaveInterval = Settings.getAutosaveInterval();
        EventHandler timelineEventHandler = new EventHandler() {
            @Override
            public void handle(Event e) {
                lngAutoSaveCount++;
                long lngTimeCurrent = System.currentTimeMillis();
                LOGGER.debug("Autosave files EventHandler."
                        + " lngAutoSaveCount=" + lngAutoSaveCount
                        + " lngTimeCurrent=" + lngTimeCurrent);
                if (lstTabs == null || lstTabs.isEmpty()) {
                    return;
                }
                for (Tab tab : lstTabs) {
                    FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
                    boolean booFileModified = fileEditor.isFileModified();
                    if (booFileModified) {
                        String strFilePath = fileEditor.getFilePath();
                        saveFileFromTab(tab);
                        LOGGER.debug("Autosave file."
                                + " lngAutoSaveCount=" + lngAutoSaveCount
                                + " lngTimeCurrent=" + lngTimeCurrent
                                + " strFilePath=\"" + strFilePath + "\"");
                    }
                }
            }
        };
        KeyFrame keyFrame = new KeyFrame(Duration.minutes(intAutosaveInterval), timelineEventHandler);
        timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    private boolean openFileinTab(Path pathFile) {

        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return false;
        }

        String strErrMsg = Utils.checkFileExist("openFileinTab", pathFile);
        if (strErrMsg != null && !strErrMsg.isBlank()) {
            Utils.showMessage(AlertType.ERROR, "Opening File", "", "File \"" + pathFile + "\" does not exist.", null, null);
            return false;
        }

        LOGGER.debug("Opening File in Tab."
                + " FILES_OPEN_COUNT=" + Settings.INT_FILES_OPENED_TOTAL_COUNT
                + " pathFile=\"" + pathFile + "\"");
        Tab tab = this.createNewTab(pathFile);
        if (tab == null) {
            return false;
        }

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.setTabPane(this.tabPane);

        strErrMsg = fileEditor.openFile();
        if (strErrMsg != null && !strErrMsg.isBlank()) {
            Utils.showMessage(AlertType.ERROR, "Opening File \"" + pathFile, "\"", strErrMsg, null, null);
            return false;
        }
        this.booTextWrap = Settings.BOO_TEXT_WRAP_DEFAULT;
        fileEditor.setTextWrap(this.booTextWrap);

        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);

        Settings.INT_FILES_OPENED_TOTAL_COUNT++;
        String strFileDir = fileEditor.getFileDir();
        Settings.setLastOpenedDir(strFileDir);

        LOGGER.info("Opened File in Tab."
                + " FILES_OPEN_COUNT=\"" + Settings.INT_FILES_OPENED_TOTAL_COUNT
                + " pathFile=\"" + pathFile + "\"");

        return true;
    }

    // -------------------------------------------------------------------------------------
    // FXML Action Methods
    // -------------------------------------------------------------------------------------
    @FXML
    private void newFile(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            this.logActionEventId("New File", actionEvent);
        }

        Tab tab = this.createNewTab(null);
        if (tab == null) {
            return;
        }

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.newFile();

        this.booTextWrap = Settings.BOO_TEXT_WRAP_DEFAULT;
        fileEditor.setTextWrap(this.booTextWrap);

        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void openFile(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            this.logActionEventId("Open File", actionEvent);
        }

        FileChooser fileChooser = new FileChooser();
        // TODO: Use last open directory and keep it in Settings.
        fileChooser.setInitialDirectory(new File(Settings.getLastOpenedDir()));
        fileChooser.setTitle("Select a file to open");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.*", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.txt", "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.log", "*.log"));
        //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.encrypted","*.encrypted"));

        File file = fileChooser.showOpenDialog(this.borderPaneEditor.getScene().getWindow());
        if (file == null) {
            LOGGER.info("Opening File. File is not selected.");
            Utils.showMessage(AlertType.WARNING, "Opening File", "", "File is not selected.", null, null);
            return;
        }
        Path pathFile = file.toPath();

        this.openFileinTab(pathFile);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFile(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Can't save File from Tab null.");
            return;
        }
        this.saveFileFromTab(tab);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFileAs(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Saving file AS before any tab created.");
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileName = fileEditor.getFileName();
        String strFilePath = fileEditor.getFilePath();
        String strFileNameExt = fileEditor.getFileExt();
        String strFileDir = fileEditor.getFileDir();

        FileChooser fileChooser = new FileChooser();
        if (strFileNameExt != null && !strFileNameExt.isBlank()) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files (*." + strFileNameExt + ")", "*." + strFileNameExt));
        }

        if (strFileDir != null && !strFileDir.isBlank()) {
            fileChooser.setInitialDirectory(new File(strFileDir));
        } else {
            fileChooser.setInitialDirectory(new File(Settings.STR_DIRECTORY_USER_HOME_PATH));
        }

        File fileSaveAs = fileChooser.showSaveDialog(null);
        if (fileSaveAs == null) {
            LOGGER.info("Saving file As canceled."
                    + " TabId=\"" + strTabId + "\""
                    + " FilePathOld=\"" + strFilePath + "\"");
            return;
        }
        String strFileNameAs = fileSaveAs.getName();
        String strFilePathAs = fileSaveAs.getAbsolutePath();
        Path pathFileSaveAs = fileSaveAs.toPath();

        LOGGER.info("Saving file As."
                + " TabId=\"" + strTabId + "\""
                + " FileNameOld=\"" + strFileName + "\""
                + " FilePathOld=\"" + strFilePath + "\""
                + " FileNameAs=\"" + strFileNameAs + "\""
                + " FilePathAs=\"" + strFilePathAs + "\""
                + " pathFileSaveAs=\"" + pathFileSaveAs + "\"");
        if (fileEditor.saveFile(pathFileSaveAs)) {
            strFileNameAs = fileEditor.getFileName();
            tab.setText(strFileNameAs);
            strFilePathAs = fileEditor.getFilePath();
            Tooltip tltp = tab.getTooltip();
            tltp.setText(strFilePathAs);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void saveFilesAll(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        ObservableList<Tab> tabs = this.tabPane.getTabs();
        if (tabs == null || tabs.isEmpty()) {
            LOGGER.error("Saving file before any tab created.");
            return;
        }
        for (Tab tab : tabs) {
            this.saveFileFromTab(tab);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void printFile(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileName = fileEditor.getFileName();
        String strFilePath = fileEditor.getFilePath();

// TODO: move print to Custom Control, or find better way to print
        TextArea ta = fileEditor.getTexArea();

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            Window window = this.borderPaneEditor.getScene().getWindow();
            boolean booPrinterSelected = printerJob.showPrintDialog(window);
            if (!booPrinterSelected) {
                LOGGER.info("Printing canceled."
                        + " TabId=\"" + strTabId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
                return;
            }
            Printer printer = printerJob.getPrinter();
            String strPrinterName = printer.getName();
            boolean booPageSetup = printerJob.showPageSetupDialog(window);
            if (!booPageSetup) {
                LOGGER.info("Printing canceled."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
                return;
            }

            boolean booPrinted = printerJob.printPage(ta);
            if (booPrinted) {
                printerJob.endJob();
                LOGGER.info("Printed File."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
            } else {
                LOGGER.error("Printing failed."
                        + " TabId=\"" + strTabId + "\""
                        + " PrinterName=\"" + strPrinterName + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\"");
            }
        } else {
            LOGGER.error("Printing failed, could not create PrinterJob."
                    + " TabId=\"" + strTabId + "\""
                    + " FileName=\"" + strFileName + "\""
                    + " FilePath=\"" + strFilePath + "\"");
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void find(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        if (lstTabs.isEmpty()) {
            // Should never happen, but ...
            Utils.showMessage(AlertType.INFORMATION, "Find", "", "No one file open for editing.", null, null);
            return;
        }

        HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
        node.setVisible(true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
        node.setVisible(true);

        TextField tfFind = (TextField) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.tfBottomFind.toString());
        tfFind.requestFocus();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void replace(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        if (lstTabs.isEmpty()) {
            // Should never happen, but ...
            Utils.showMessage(AlertType.INFORMATION, "Replace", "", "No one file open for editing.", null, null);
            return;
        }

        HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
        node.setVisible(true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
        node.setVisible(true);

        node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomReplace.toString());
        node.setVisible(true);

        TextField tfReplace = (TextField) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomReplace.toString());
        tfReplace.requestFocus();
        node.requestFocus();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void lineEndingWin(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();
        if (strTabId != null) {
            FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
            fileEditor.setLineEndingWin();
            String strLineEnding = fileEditor.getLineEnding();
            updateTextFieldLineEnding(strLineEnding);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void lineEndingUnix(ActionEvent actionEvent) throws IOException {
        actionEvent.consume();
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        String strTabId = tab.getId();
        if (strTabId != null) {
            FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
            fileEditor.setLineEndingUnix();
            String strLineEnding = fileEditor.getLineEnding();
            updateTextFieldLineEnding(strLineEnding);
        }
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void exit(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        boolean booReturn;
        boolean booFileModyfied = Utils.checkAnyoneFileModified(this.lstTabs);
        if (booFileModyfied) {
            booReturn = Utils.showMessage(AlertType.CONFIRMATION,
                    "Exit Confirmation", "There is File modified and not saved. Do you want to exit?",
                    "Click Yes to exit", "Yes", "No");
            if (!booReturn) {
                // do not exit app
                return;
            }
        }
        Platform.exit();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSelect(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        String strFontFamily;
        double dblFontSize;

        Object source = actionEvent.getSource();
        MenuItem menuItem = (MenuItem) source;
        String strMenuItemId = menuItem.getId();
        switch (strMenuItemId) {
            case "miFontOsDefault":
                strFontFamily = Settings.STR_FONT_FAMILY_OS_DEFAULT;
                dblFontSize = Settings.DOUBLE_FONT_SIZE_OS_DEFAULT;
                break;
            case "miFontDefault":
            default:
                strFontFamily = Settings.getFontFamilyDefault();
                dblFontSize = Settings.getFontSizeDefault();
        }

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Select Font before any tab created.");
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = new Font(strFontFamily, dblFontSize);
        fileEditor.setFont(font);
        LOGGER.debug("Changed Font."
                + " TabId=\"" + strTabId + "\""
                + " MenuId=\"" + strMenuItemId + "\""
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    public void charsetSelectRead(MouseEvent event) {

        event.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Select charset before any tab created.");
            return;
        }
        String strTabId = tab.getId();
        String strCharset = (String) this.menuListViewCharsetRead.getSelectionModel().getSelectedItem();
        LOGGER.debug("Changed Charset."
                + " strTabId=\"" + strTabId + "\""
                + " strCharset=\"" + strCharset + "\"");

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.setCharsetName(strCharset);
        String strErrMsg = fileEditor.openFile();
        if (strErrMsg != null && !strErrMsg.isBlank()) {
            // TODO: add error message on screen
            Utils.showMessage(AlertType.ERROR, "Opening File", "", strErrMsg, null, null);
            return;
        }

        LOGGER.debug("Opened File with new charset."
                + " strTabId=\"" + strTabId + "\""
                + " strCharset=\"" + strCharset + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    public void charsetSelectWrite(MouseEvent event) {

        event.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Select charset before any tab created.");
            return;
        }
        String strTabId = tab.getId();
        String strCharset = (String) this.menuListViewCharsetWrite.getSelectionModel().getSelectedItem();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        fileEditor.setCharsetName(strCharset);
        this.updateTextFieldLineCharsetName(strCharset);
        LOGGER.debug("Changed charset for saving file."
                + " strTabId=\"" + strTabId + "\""
                + " strCharset=\"" + strCharset + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    public void fontFamilySelect(MouseEvent event) {

        event.consume();
        String strFontFamily = (String) this.menuListViewFontFamily.getSelectionModel().getSelectedItem();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Select Font Family before any tab created.");
            return;
        }
        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        double dblFontSize;
        if (font == null) {
            dblFontSize = Settings.getFontSizeDefault();
        } else {
            dblFontSize = font.getSize();
        }
        font = new Font(strFontFamily, dblFontSize);
        fileEditor.setFont(font);
        LOGGER.info("Changed Font Family."
                + " TabId=\"" + strTabId + "\""
                + " FontFamily=\"" + strFontFamily + "\""
                + " FontSize=" + dblFontSize
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontSizeSelect(ActionEvent actionEvent) {//throws IOException {

        actionEvent.consume();
        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Select Font Size before any tab created.");
            return;
        }
        String strTabId = tab.getId();
        String strFontSize = menuTextFielsFontSize.getText();
        if (strFontSize == null || strFontSize.isEmpty()) {
            LOGGER.error("Incorrect Font Size."
                    + " TabId=\"" + strTabId + "\""
                    + " FontSize=\"" + strFontSize + "\"");
            return;
        }
        double dblFontSizeValidated = Settings.changeFontSize(strFontSize);
        if (dblFontSizeValidated == 0) {
            LOGGER.error("Incorrect Font Size."
                    + " TabId=\"" + strTabId + "\""
                    + " FontSize=\"" + strFontSize + "\"");
            return;
        }
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        String strFontFamily;
        if (font == null) {
            strFontFamily = Settings.getFontFamilyDefault();
        } else {
            strFontFamily = font.getFamily();
        }
        font = new Font(strFontFamily, dblFontSizeValidated);
        fileEditor.setFont(font);
        LOGGER.debug("Changed Font size in a tab."
                + " TabId=\"" + strTabId + "\""
                + " FontFamily=\"" + strFontFamily + "\""
                + " FontSize=" + dblFontSizeValidated
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void favoritesEdit(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();
        if (!Utils.checkNewTabsAllowed(this.lstTabs)) {
            return;
        }
        String strFileFavoritesPathCalc = Settings.caclulateFavoritesPath();
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPathCalc);
        Tab tab = this.createNewTab(pathFileFavorites);
        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        String strFileFavoritesPath = fileEditor.getFilePath();

        String strErrMsg = Utils.checkFileExist(strTabId, pathFileFavorites);
        if (strErrMsg == null || strErrMsg.isBlank()) {
            LOGGER.info("Opening Favorites File for Editing."
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
            strErrMsg = fileEditor.openFile();
            if (strErrMsg != null && !strErrMsg.isBlank()) {
                Utils.showMessage(AlertType.ERROR, "Opening File", "", strErrMsg, null, null);
                return;
            }
            fileEditor.setFont(Settings.getFontDefault());
            this.addFavorites();
        } else {
            //TODO: create file.
            LOGGER.info("Favorites File does not exist."
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
            if (Utils.createFile(pathFileFavorites) > 0) {
                LOGGER.info("Favorites File created."
                        + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
            }
        }

        this.lstTabs.add(tab);
        this.tabPane.getSelectionModel().select(tab);

        this.changeMenuVisibility(true);
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void favoritesAdd(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Could not add File to Favorites before any tab created.");
            return;
        }
        String strTabId = tab.getId();

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        boolean booFileModified = fileEditor.isFileModified();
        if (booFileModified) {
            fileEditor.saveFile(null);
        }

        String strFileFavoritesPath = Settings.caclulateFavoritesPath();
        Path pathFileFavorites = FileSystems.getDefault().getPath(strFileFavoritesPath);
        String strFilePathToAdd = fileEditor.getFilePath();

        String strErrMsg = Utils.checkFileExist(strTabId, pathFileFavorites);
        if (strErrMsg != null && !strErrMsg.isBlank()) {
            LOGGER.info("Create Favorites File."
                    + " TabId=\"" + strTabId + "\""
                    + " strFileFavoritesPath=\"" + strFileFavoritesPath + "\""
                    + " pathFileFavorites=\"" + pathFileFavorites + "\"");
            if (Utils.createFile(pathFileFavorites) > 0) {
                strErrMsg = Utils.checkFileExist(strTabId, pathFileFavorites);
                if (strErrMsg != null && !strErrMsg.isBlank()) {
                    LOGGER.error("Could not create Favorites File."
                            + " TabId=\"" + strTabId + "\""
                            + " strFileFavoritesPath=\"" + strFileFavoritesPath + "\""
                            + " pathFileFavorites=\"" + pathFileFavorites + "\"");
                    return;
                }
            }
        }

        // TODO: Check if File Path is in Favorites list already.
        Charset charset = Charset.forName(Settings.STR_CHARSET_CURRENT);
        try (BufferedReader br = Files.newBufferedReader(pathFileFavorites, charset)) {
            int intLineCount = 0;
            String strLine;
            while ((strLine = br.readLine()) != null) {
                intLineCount++;
                if (!strLine.isEmpty()) {
                    if (strLine.equals(strFilePathToAdd)) {
                        LOGGER.info("FilePath found in Favorites File."
                                + " TabId=\"" + strTabId + "\""
                                + " LineNumber=\"" + intLineCount + "\""
                                + " FilePath=\"" + strFilePathToAdd + "\""
                                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
                        return;
                    }
                }
            }
        }
        LOGGER.debug("Adding FilePath to Favorites File."
                + " TabId=\"" + strTabId + "\""
                + " FilePath=\"" + strFilePathToAdd + "\""
                + " FileFavoritesPathCalc=\"" + strFileFavoritesPath + "\""
                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");

        try (BufferedWriter bw = Files.newBufferedWriter(pathFileFavorites, charset, StandardOpenOption.APPEND)) {
            bw.append(strFilePathToAdd);
            bw.append(System.lineSeparator()); //"\n");
        } catch (Throwable t) {
            LOGGER.error("Could not add FilePath to Favorites File."
                    + " TabId=\"" + strTabId + "\""
                    + " FilePath=\"" + strFilePathToAdd + "\""
                    + " FileFavoritesPath=\"" + strFileFavoritesPath + "\""
                    + " Throwable=\"" + t.toString() + "\"");
        }
        LOGGER.info("Added FilePath to Favorites File."
                + " TabId=\"" + strTabId + "\""
                + " FilePath=\"" + strFilePathToAdd + "\""
                + " FileFavoritesPath=\"" + strFileFavoritesPath + "\"");
        this.addFavorites();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void settingsEdit(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        Settings.load();

        FXMLLoader fxmlLoader = Utils.loadFXML("jfxEditorSettings");
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, Settings.INT_WINDOW_SETTINGS_WIDTH, Settings.INT_WINDOW_SETTINGS_HIGH);

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int intWindowWidth = newValue.intValue();
                Settings.INT_WINDOW_SETTINGS_WIDTH = intWindowWidth;
            }
        });

        scene.heightProperty().addListener(
                new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int intWindowHigh = newValue.intValue();
                Settings.INT_WINDOW_SETTINGS_HIGH = intWindowHigh;
            }
        });

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void about(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        FXMLLoader fxmlLoader = Utils.loadFXML("jfxEditorAbout");
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, Settings.INT_WINDOW_ABOUT_WIDTH, Settings.INT_WINDOW_ABOUT_HIGH);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("About");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontIncrease(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Font increase before any tab created.");
            return;
        }

        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        double dblFontSize = font.getSize();
        double dblFontSizeValidated = Settings.increaseFontSize(dblFontSize, true);
        String strFontFamily = font.getFamily();
        font = new Font(strFontFamily, dblFontSizeValidated);
        fileEditor.setFont(font);
        LOGGER.debug("Increased Font size in a tab."
                + " TabId=\"" + strTabId + "\""
                + " FontSize=" + dblFontSizeValidated
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    @FXML
    private void fontDecrease(ActionEvent actionEvent) throws IOException {

        actionEvent.consume();

        Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) {
            LOGGER.error("Font Decrease before any tab created.");
            return;
        }
        String strTabId = tab.getId();
        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        Font font = fileEditor.getFont();
        double dblFontSize = font.getSize();
        double dblFontSizeValidated = Settings.increaseFontSize(dblFontSize, false);
        String strFontFamily = font.getFamily();
        font = new Font(strFontFamily, dblFontSizeValidated);
        fileEditor.setFont(font);
        LOGGER.debug("Decreased Font size in a tab."
                + " TabId=\"" + strTabId + "\""
                + " FontSize=" + dblFontSizeValidated
                + " Font=\"" + font + "\"");
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    public Timeline getTimeline() {
        return this.timeline;
    }

    // -------------------------------------------------------------------------------------
    // Helpers 
    // -------------------------------------------------------------------------------------
    private void logActionEventId(String strAction, ActionEvent actionEvent) {

        Object objActionEventSource = actionEvent.getSource();
        String strItemId;
        if (objActionEventSource instanceof javafx.scene.control.Button) {
            Button btn = (Button) objActionEventSource;
            strItemId = btn.getId();
        } else if (objActionEventSource instanceof javafx.scene.control.MenuItem) {
            MenuItem menuitemSource = (MenuItem) objActionEventSource;
            strItemId = menuitemSource.getId();
        } else {
            strItemId = null;
        }

        LOGGER.debug(strAction
                + " FILES_OPENED=" + Settings.INT_FILES_NEW_COUNT
                + " FILES_OPENED_TOTAL=" + Settings.INT_FILES_OPENED_TOTAL_COUNT
                + " ActionEventSource=\"" + objActionEventSource + "\""
                + " ItemId=\"" + strItemId + "\""
        );
    }

    // -------------------------------------------------------------------------------------
    private Tab createNewTab(Path pathFile) {

        Settings.INT_TABS_OPENED_TOTAL_COUNT++;
        Settings.INT_TABS_OPENED_COUNT++;
        String strTabId = "" + Settings.INT_TABS_OPENED_TOTAL_COUNT;

        if (pathFile == null) {
            Settings.INT_FILES_NEW_COUNT++;
            pathFile = Paths.get(Settings.getLastOpenedDir(),
                    Settings.STR_NEW_FILENAME_DEFAULT + Settings.INT_FILES_NEW_COUNT + "." + Settings.STR_FILENAME_EXT_DEFAULT);
        }

        FileContentEditor fileEditor = new FileContentEditor(strTabId, pathFile);
        String strFilePath = fileEditor.getFilePath();
        String strFileName = fileEditor.getFileName();
        String strFileNameExt = fileEditor.getFileExt();

        Tab tab = new Tab(strFileName, fileEditor);
        tab.setId(strTabId);

        Tooltip tltp = new Tooltip(strFilePath);
        tab.setTooltip(tltp);

        tab.setClosable(true);
        tab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {

                // Don't do that here:event.consume();
                EventType eventType = event.getEventType();
                LOGGER.debug("Closing Tab."
                        + " TabId=\"" + strTabId + "\""
                        + " TabsOpenedTotal=" + Settings.INT_TABS_OPENED_TOTAL_COUNT
                        + " TabsOpen=" + Settings.INT_TABS_OPENED_COUNT
                        + " TabsClosed=" + Settings.INT_TABS_CLOSED_COUNT
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\""
                        + " FileNameExt=\"" + strFileNameExt + "\""
                        + " eventType=\"" + eventType + "\"");
                Settings.INT_TABS_OPENED_COUNT--;
                Settings.INT_TABS_CLOSED_COUNT++;
                if (Settings.INT_TABS_OPENED_COUNT == 0) {
                    changeMenuVisibility(false);

                    HBox node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomSearchResult.toString());
                    Utils.changeNodeVisibility(node, false);

                    node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomFind.toString());
                    Utils.changeNodeVisibility(node, false);

                    node = (HBox) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.hboxBottomReplace.toString());
                    Utils.changeNodeVisibility(node, false);

                }
                boolean booModified = fileEditor.isFileModified();
                if (!booModified) {
                    //fileEditor
                    return;
                }
                if (!showMessage(Alert.AlertType.WARNING, "Saving File on Close",
                        "The File was modified and not saved.\nFilePath=" + strFilePath,
                        "Do you want to save file?", "Yes", "No")) {
                    LOGGER.info("Saving modified File while closing Tab was denied."
                            + " TabId=\"" + strTabId + "\""
                            + " FileName=\"" + strFileName + "\""
                            + " FilePath=\"" + strFilePath + "\""
                            + " FileNameExt=\"" + strFileNameExt + "\""
                            + " eventType=\"" + eventType + "\"");
                    return;
                }
                LOGGER.debug("Saving File while closing Tab."
                        + " TabId=\"" + strTabId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " FilePath=\"" + strFilePath + "\""
                        + " FileNameExt=\"" + strFileNameExt + "\""
                        + " eventType=\"" + eventType + "\"");
                fileEditor.saveFile(null);
            }
        });

        LOGGER.info("Created Tab."
                + " TabId=\"" + strTabId + "\""
                + " FileName=\"" + strFileName + "\""
                + " FilePath=\"" + strFilePath + "\""
                + " FileNameExt=\"" + strFileNameExt + "\"");
        return tab;
    }

    // ---------------------------------------------------------------------------
    private void saveFileFromTab(Tab tab) {

        if (tab == null) {
            LOGGER.error("Can't save File from Tab null.");
            return;
        }

        FileContentEditor fileEditor = (FileContentEditor) tab.getContent();
        boolean booFileModified = fileEditor.isFileModified();
        if (!booFileModified) {
            String strTabId = tab.getId();
            String strFilePath = fileEditor.getFilePath();
            LOGGER.debug("No needs to save existing File if it was not modified."
                    + " TabId=\"" + strTabId + "\""
                    + " FilePath=\"" + strFilePath + "\"");
            return;
        }
        fileEditor.saveFile(null);
    }

    // -------------------------------------------------------------------------------------
    private void changeMenuVisibility(boolean booVisible) {

        this.miSaveFile.setVisible(booVisible);
        this.miSaveFileAs.setVisible(booVisible);
        this.miSaveFilesAll.setVisible(booVisible);
        this.menuEdit.setVisible(booVisible);
        this.menuFont.setVisible(booVisible);
        this.miPrint.setVisible(booVisible);

        this.cbTextWrap.setDisable(!booVisible);
        this.buttonFontIncrease.setDisable(!booVisible);
        this.buttonFontDecrease.setDisable(!booVisible);

        if (Settings.BOO_SHOW_TOOLBAR_EABLED) {
            this.buttonSaveFile.setVisible(booVisible);
            this.buttonSaveFileAs.setVisible(booVisible);
            this.buttonSaveFileAll.setVisible(booVisible);
            this.buttonPrint.setVisible(booVisible);
        }
    }

    // -------------------------------------------------------------------------------------
    private void updateTextFieldLineEnding(String strLineEnding) {

        TextField tfLineEnding = (TextField) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.tfBottomLineEnding.toString());
        tfLineEnding.setText(strLineEnding);
        // TODO: make set width better
        tfLineEnding.setPrefWidth(strLineEnding.length() * 8);
    }

    // -------------------------------------------------------------------------------------
    private void updateTextFieldLineCharsetName(String strCharsetName) {

        TextField tfCharset = (TextField) Utils.MAP_NODE_REFS.get(Utils.NODE_NAMES.tfBottomCharset.toString());
        tfCharset.setText(strCharsetName);
        // TODO: make set width better
        tfCharset.setPrefWidth(tfCharset.getText().length() * 10);
    }
    // -------------------------------------------------------------------------------------
}
