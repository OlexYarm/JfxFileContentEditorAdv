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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContentEditor extends VBox {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileContentEditor.class);
    private static final int INT_PROGRESS_BAR_STEPS = 20;
    private static final byte BYT_CR = 0x0D;
    private static final byte BYT_LF = 0x0A;
    private static final String STR_CR_LF_WIN = "Win CRLF";
    private static final String STR_LF_UNIX = "Unix LF";
    private static final String STR_CR_LF_MIX = "Mix CR LF";
    private static final String STR_NO_CR_LF = "no CR LF)";
    private static final String STR_CR_LF_WIP = "WIP";

    private Path pathFile;
    private String strFilePath;
    private String strFileName;
    private String strFileExt;
    private String strFileDir;
    private boolean booBinary;
    private String strCharsetName;
    private boolean booFileModified;
    private boolean booTextWrap;

    private final String strId;

    // ---------- Graphics - Begin -----------------------------------------------------
    private final TextArea textArea = new TextArea();

    private final HBox hboxState;
    private final Label lblFileState;
    private final Label lblFileName;
    private final ProgressBar progressBar;

    private Task<String> taskFileLoad;

    private Service<String> serviceFileSave;
    private static final int INT_FILE_LEN_SPLIT = 10;
    private int intFileSaveCount = 0;

    private final ReadOnlyBooleanProperty booPropFocusedProperty;
    private final ChangeListener<Boolean> focusedPropertyChangeListener;

    private final InvalidationListener invalidationListenerFileContent;

    private final IntegerProperty intPropCaretPosition = new SimpleIntegerProperty();
    private final ReadOnlyIntegerProperty intPropCaretPositionProperty;
    private final ChangeListener<Number> caretPositionChangeListener;

    private Font font;

    private TabPane tabPane;

    private final StringProperty spLineEnding = new SimpleStringProperty(STR_CR_LF_WIP);

    // ---------- Graphics - End -----------------------------------------------------
    // -------------------------------------------------------------------------------------
    // Construstors
    // -------------------------------------------------------------------------------------
    public FileContentEditor(final String strId, final Path pathFile) {

        // TODO: Add CSS
        //getStyleClass().add("fileContentEditor.css");
        this.strId = strId;
        this.pathFile = pathFile;
        this.parseFilePath(strId, pathFile);

        // TODO: Add binary file show/edit
        this.booBinary = false;
        this.font = Settings.getFontDefault();

        this.strCharsetName = Settings.STR_CHARSET_CURRENT;

        // ---------- initGraphics - begin -----------------------------------------------------
        this.textArea.setPromptText("Enter Text here.");
        this.textArea.setFont(this.font);
        VBox.setVgrow(this.textArea, Priority.ALWAYS);

        this.hboxState = new HBox();
        Insets insHboxPadd = new Insets(5, 5, 5, 20);
        this.hboxState.setPadding(insHboxPadd);
        this.hboxState.setSpacing(5);
        this.hboxState.setVisible(false);

        this.lblFileState = new Label("");
        this.lblFileName = new Label(this.strFileName);
        this.progressBar = new ProgressBar(0);

        this.hboxState.getChildren().addAll(this.lblFileName, this.progressBar, this.lblFileState);
        this.hboxState.managedProperty().bind(this.hboxState.visibleProperty());

        this.getChildren().addAll(this.textArea, this.hboxState);

        Insets insVboxPadd = new Insets(5, 5, 5, 5);
        this.setPadding(insVboxPadd);

        // ---------- initGraphics - end -------------------------------------------------------
        //
        // ---------- registerListeners - begin ------------------------------------------------
        this.intPropCaretPositionProperty = this.textArea.caretPositionProperty();
        this.caretPositionChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldNumberValue, Number newNumberValue) {
                hboxState.setVisible(false);
                intPropCaretPosition.set(newNumberValue.intValue());
                LOGGER.debug("caretPositionChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " observable=" + observable
                        + " oldNumberValue=" + oldNumberValue + " newNumberValue=" + newNumberValue);
            }
        };
        this.intPropCaretPositionProperty.addListener(this.caretPositionChangeListener);

        // -------------------------------------------------------------------------------------
        this.invalidationListenerFileContent = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                booFileModified = true;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The FileContent binding is invalid."
                            + " Id=\"" + strId + "\""
                            + " FileName=\"" + strFileName + "\""
                            + " Observable=\"" + o + "\"");
                }
            }
        };
        this.textArea.textProperty().addListener(this.invalidationListenerFileContent);

        // -------------------------------------------------------------------------------------
        this.focusedPropertyChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                hboxState.setVisible(false);
                int intCaretPosition = intPropCaretPositionProperty.getValue();
                LOGGER.debug("focusedPropertyChangeListener."
                        + " Id=\"" + strId + "\""
                        + " FileName=\"" + strFileName + "\""
                        + " CaretPosition=\"" + intCaretPosition + "\""
                        + " observable=" + observable
                        + " oldValue=" + oldValue + " newValue=" + newValue);
            }
        };
        this.booPropFocusedProperty = this.textArea.focusedProperty();
        this.booPropFocusedProperty.addListener(this.focusedPropertyChangeListener);

        // ---------- registerListeners - end ------------------------------------------------
        // -------------------------------------------------------------------------------------
        LOGGER.debug("## Created ContentEditor."
                + " Id=\"" + strId + "\""
                + " FilePath=\"" + strFilePath + "\""
        );
    }

    // -------------------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------------------
    public void newFile() {

        LOGGER.debug("# New File.");
        if (this.pathFile == null) {
            this.hboxState.visibleProperty().set(true);
            LOGGER.error("Could not create file null.");
            return;
        }
        this.hboxState.visibleProperty().set(false);
    }

    // -------------------------------------------------------------------------------------
    public String openFile() {

        this.hboxState.visibleProperty().set(true);
        LOGGER.debug("# openFile."
                + " booBinary=" + this.booBinary
                + " strCharsetName=\"" + this.strCharsetName + "\""
                + " pathFile=\"" + this.pathFile + "\"");
        if (this.pathFile == null) {
            String strMsg = "Could not open file with Path null.";
            LOGGER.error(strMsg);
            lblFileState.textProperty().set(strMsg);
            return strMsg;
        }

        if (!Files.exists(this.pathFile)) {
            String strMsg = "File does not exist."
                    + " pathFile=\"" + pathFile + "\"";
            LOGGER.error(strMsg);
            lblFileState.textProperty().set(strMsg);
            LOGGER.error(strMsg);
            return strMsg;
        }

        if (Files.isDirectory(this.pathFile)) {
            String strMsg = "Could not read file, it's a directory."
                    + " pathFile=\"" + pathFile + "\"";
            LOGGER.error(strMsg);
            lblFileState.textProperty().set(strMsg);
            LOGGER.error(strMsg);
            return strMsg;
        }

        if (!Files.isReadable(this.pathFile)) {
            String strMsg = "File is not readable."
                    + " pathFile=\"" + pathFile + "\"";
            LOGGER.error(strMsg);
            lblFileState.textProperty().set(strMsg);
            LOGGER.error(strMsg);
            return strMsg;
        }

        File file = this.pathFile.toFile();
        long lngFileSize = file.length();
        if (lngFileSize > Integer.MAX_VALUE) {
            String strMsg = "File is too big."
                    + " FileSize=" + lngFileSize
                    + " pathFile=\"" + pathFile + "\"";
            LOGGER.error(strMsg);
            lblFileState.textProperty().set(strMsg);
            return strMsg;
        }
        int intFileSize = (int) lngFileSize;
        int intFileSizeKb = 0;
        int intFileSizeMb = 0;
        String strFileSize = "";
        if (intFileSize > 1024) {
            intFileSizeKb = intFileSize / 1024;
            strFileSize = "" + intFileSizeKb + "KB";
        }
        if (intFileSizeKb > 1024) {
            intFileSizeMb = intFileSizeKb / 1024;
            strFileSize = "" + intFileSizeMb + "MB";
        }
        LOGGER.debug("# openFile."
                + " lngFileSize=" + lngFileSize
                + " intFileSizeKb=" + intFileSizeKb
                + " intFileSizeMb=" + intFileSizeMb
                + " strFileSize=" + strFileSize
                + " pathFile=\"" + this.pathFile + "\""
                + " Binary=" + this.booBinary
                + " strCharsetName=\"" + this.strCharsetName + "\"");

        this.taskFileLoad = new Task<>() {
            @Override
            protected String call() throws Exception {

                updateMessage("Task File loading started.");
                updateProgress(0, intFileSize);

                Charset charset = Charset.forName(strCharsetName);
                CharsetDecoder charsetDecoder = charset.newDecoder();
                charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
                charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
                charsetDecoder.replaceWith(Settings.STR_CHARSET_REPLACE_WITH_DEFAULT);

                LOGGER.debug("Using charset."
                        + " Id=\"" + strId + "\""
                        + " Charset=\"" + charset + "\""
                        + " CharsetDecoder=\"" + charsetDecoder + "\""
                        + " charsetDecoder.malformedInputAction()=\"" + charsetDecoder.malformedInputAction() + "\""
                        + " charsetDecoder.unmappableCharacterAction()=\"" + charsetDecoder.unmappableCharacterAction() + "\""
                        + " charsetDecoder.replacement()=\"" + charsetDecoder.replacement() + "\""
                        + " pathFile=\"" + pathFile + "\""
                );

                int intProgressStep = intFileSize / INT_PROGRESS_BAR_STEPS;
                LOGGER.debug("Loading file."
                        + " Id=\"" + strId + "\""
                        + " intFileSize=" + intFileSize
                        + " pathFile=\"" + pathFile + "\""
                        + " intProgressStep=" + intProgressStep);

                int intProgressCounter = 1;
                long lngBytesReadTotal = 0;
                int intRemaining = 0;
                int intBytesReadLast = -1;
                long lngLinesLoaded = 0;
                int intErrors = 0;
                int intErrorsMissingLF = 0;
                int intOsWinCrCount = 0;
                int intOsWinLfCount = 0;
                int intOsUnixCount = 0;
                StringBuilder sbFileContent = new StringBuilder();
                long lngTimeStart = System.currentTimeMillis();

                try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(pathFile, EnumSet.of(StandardOpenOption.READ))) {

                    MappedByteBuffer mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, lngFileSize);
                    mbb.mark();
                    int intPosStart = mbb.position();
                    int intPosEnd;
                    boolean booEolFound = false;
                    while (true) {
                        intRemaining = mbb.remaining();
                        if (intRemaining == 0) {
                            break;
                        }
                        byte b = mbb.get();
                        intRemaining = mbb.remaining();
                        lngBytesReadTotal++;
                        if (b == BYT_CR) {
                            booEolFound = true;
                            lngLinesLoaded++;
                            intOsWinCrCount++;
                            b = mbb.get();
                            intRemaining = mbb.remaining();
                            lngBytesReadTotal++;
                            if (b == BYT_LF) {
                                intOsWinLfCount++;
                            } else {
                                intErrors++;
                                intErrorsMissingLF++;
                            }
                        } else if (b == BYT_LF) {
                            booEolFound = true;
                            lngLinesLoaded++;
                            intOsUnixCount++;
                        }
                        if (booEolFound) {
                            booEolFound = false;
                            intPosEnd = mbb.position();
                            int intBytesInLine = intPosEnd - intPosStart;
                            byte[] abytLine = new byte[intBytesInLine];
                            mbb.reset();
                            mbb.get(abytLine);
                            ByteBuffer byteBufferLine = ByteBuffer.wrap(abytLine);
                            CharBuffer charBufferLine = charsetDecoder.decode(byteBufferLine);
                            String strLine = charBufferLine.toString();
                            //lstLines.add(strLine);
                            sbFileContent.append(strLine);
                            intBytesReadLast = strLine.length();
                            mbb.mark();
                            intPosStart = intPosEnd;
                        }
                        if (intProgressStep == 1
                                || lngBytesReadTotal >= intProgressStep * intProgressCounter) {
                            long lngBytesTotal = lngBytesReadTotal + intRemaining;
                            //int intListSize = lstLines.size();
                            LOGGER.debug("Reading..."
                                    + " intProgressCounter=" + intProgressCounter
                                    + " lngBytesReadTotal=" + lngBytesReadTotal
                                    + " intRemaining=" + intRemaining
                                    + " lngBytesTotal=" + lngBytesTotal
                                    + " lngLinesLoaded=" + lngLinesLoaded
                                    //+ " intListSize=" + intListSize
                                    + " intOsWinCrCount=" + intOsWinCrCount
                                    + " intOsWinLfCount=" + intOsWinLfCount
                                    + " intErrorsMissingLF=" + intErrorsMissingLF
                                    + " intOsUnixCount=" + intOsUnixCount);
                            updateProgress(lngBytesReadTotal, intFileSize);
                            updateMessage("File Loading " + " steps=" + intProgressCounter * INT_PROGRESS_BAR_STEPS + " bytes=" + lngBytesReadTotal);
                            intProgressCounter++;
                        }
                        /*
                        // For testing only !
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException interrupted) {
                            if (isCancelled()) {
                                updateMessage("Cancelled");
                                break;
                            }
                        }
                         */
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }
                    fileChannel.close();
                } catch (Throwable t) {
                    updateMessage("Error loading File." + t.getMessage());
                    LOGGER.error("Could not Read File."
                            + " Id=\"" + strId + "\""
                            + " intUpdateProgressCounter=" + intProgressCounter
                            + " lngBytesReadTotal=" + lngBytesReadTotal
                            + " intBytesReadLast=" + intBytesReadLast
                            + " lngLinesLoaded=" + lngLinesLoaded
                            + " intOsWinCrCount=" + intOsWinCrCount
                            + " intOsWinLfCount=" + intOsWinLfCount
                            + " intErrorsMissingLF=" + intErrorsMissingLF
                            + " intOsUnixCount=" + intOsUnixCount
                            + " pathFile=\"" + pathFile + "\""
                            + " Throwable=\"" + t.toString() + "\"");
                    return sbFileContent.toString();
                }
                updateMessage("File Loaded (" + lngBytesReadTotal + " bytes).");

                String strLineEnding;
                if (intOsWinCrCount > 0 && intOsUnixCount > 0) {
                    strLineEnding = STR_CR_LF_MIX;
                } else if (intOsWinCrCount > 0) {
                    strLineEnding = STR_CR_LF_WIN;
                } else if (intOsUnixCount > 0) {
                    strLineEnding = STR_LF_UNIX;
                } else {
                    strLineEnding = STR_NO_CR_LF;
                }
                spLineEnding.set(strLineEnding);

                long lngTimeFinish = System.currentTimeMillis();
                long lngTimeTaken = lngTimeFinish - lngTimeStart;
                LOGGER.debug("Loaded file."
                        + " Id=\"" + strId + "\""
                        + " lngBytesReadTotal=" + lngBytesReadTotal
                        + " lngLinesLoaded=" + lngLinesLoaded
                        + " intOsWinCrCount=" + intOsWinCrCount
                        + " intOsWinLfCount=" + intOsWinLfCount
                        + " intErrorsMissingLF=" + intErrorsMissingLF
                        + " intOsUnixCount=" + intOsUnixCount
                        + " strLineEnding=\"" + strLineEnding + "\""
                        + " TimeTaken=" + (float) lngTimeTaken / 1000.00 + " sec" + " (" + lngTimeTaken + " ms}"
                        + " pathFile=\"" + pathFile + "\"");

                return sbFileContent.toString();
            }
        };
        this.processTask();

        LOGGER.debug("# openFile-Task starting."
                + " Id=\"" + strId + "\""
                + " task=\"" + taskFileLoad + "\"");
        new Thread(taskFileLoad).start();
        LOGGER.debug("# openFile-Task started."
                + " Id=\"" + strId + "\""
                + " task=\"" + taskFileLoad + "\"");
        return "";
    }

    // -------------------------------------------------------------------------------------
    public void openFileBinary() {

        this.booBinary = true;
        this.openFile();
    }

    // -------------------------------------------------------------------------------------
    public boolean saveFile(Path pathFileSaveAs) {
        // Parameters:
        // pathFileSaveAs == null for saving new of existing file.
        // pathFileSaveAs != null for saving File As.

        this.hboxState.visibleProperty().set(true);

        if (pathFileSaveAs == null) {
            String strReason = canSaveFile(this.strId, this.pathFile);
            if (strReason != null) {
                lblFileState.textProperty().set(strReason);
                LOGGER.debug("Could not save File."
                        + " pathFile=\"" + pathFile + "\""
                        + " strReason=\"" + strReason + "\"");
                return false;
            }
        } else {
            this.pathFile = pathFileSaveAs;
            this.parseFilePath(this.strId, this.pathFile);
        }

        if (Settings.BOO_BACKUP_FILES_EABLED) {
//Old            renameFileToBackupReverted(strTabID, fileToSave);
            renameFileToBackup(this.strId, this.pathFile);
        }

        if (this.serviceFileSave == null) {
            this.serviceFileSave = new Service<>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<String>() {
                        @Override
                        protected String call() throws InterruptedException {
                            LOGGER.debug("Service File Save started."
                                    + " pathFile=\"" + pathFile + "\"");
                            updateMessage("File Save started.");
                            String strText = getContent();
                            if (strText == null) {
                                LOGGER.info("Service saving null string."
                                        + " pathFile=\"" + pathFile + "\"");
                                strText = "";
                            }
                            int intTextLen = strText.length();
                            updateProgress(0, intTextLen);
                            String strComment;
                            if (intTextLen == 0) {
                                strComment = " empty file";
                            } else {
                                strComment = "";
                            }
                            LOGGER.info("Service File saving" + strComment + "."
                                    + " Id=\"" + strId + "\""
                                    + " intLen=" + intTextLen
                                    + " pathFile=\"" + pathFile + "\"");
                            int intStep;
                            if (intTextLen < INT_FILE_LEN_SPLIT) {
                                intStep = 0;
                            } else {
                                intStep = intTextLen / INT_PROGRESS_BAR_STEPS;
                            }
                            int intFrom = 0;
                            LOGGER.debug("Service Saving file."
                                    + " Id=\"" + strId + "\""
                                    + " intLen=" + intTextLen
                                    + " intStep=" + intStep
                                    + " pathFile=\"" + pathFile + "\"");

                            Charset charset = Charset.forName(strCharsetName);
                            try (BufferedWriter writer = Files.newBufferedWriter(pathFile, charset)) {
                                if (intStep == 0) {
                                    writer.write(strText, 0, intTextLen);
                                    LOGGER.debug("Saving file."
                                            + " Id=\"" + strId + "\""
                                            + " charset=\"" + charset + "\""
                                            + " intLen=" + intTextLen
                                            + " intFrom=" + intFrom
                                            + " pathFile=\"" + pathFile + "\"");
                                } else {
                                    int intProgressCounter = 0;
                                    long lngBytesWriteTotal = 0;
                                    while (intFrom <= intTextLen - intStep) {
                                        intProgressCounter++;
                                        writer.write(strText, intFrom, intStep);
                                        intFrom += intStep;
                                        lngBytesWriteTotal += intStep;
                                        updateProgress(intFrom, intTextLen);
                                        updateMessage("File Saving " + " step=" + intProgressCounter + " bytes=" + lngBytesWriteTotal);

                                        LOGGER.debug("Saving file."
                                                + " Id=\"" + strId + "\""
                                                + " pathFile=\"" + pathFile + "\""
                                                + " intProgressCounter=" + intProgressCounter
                                                + " intFrom=" + intFrom);
                                        /* 
                                        // For testing only !
                                        try {
                                            Thread.sleep(300);
                                        } catch (InterruptedException interrupted) {
                                            if (isCancelled()) {
                                                updateMessage("Cancelled");
                                                break;
                                            }
                                        }
                                         */
                                    }
                                    int intRest = intTextLen - intFrom;
                                    if (intRest != 0) {
                                        writer.write(strText, intFrom, intRest);
                                        intProgressCounter++;
                                        LOGGER.debug("Saving file last step."
                                                + " Id=\"" + strId + "\""
                                                + " pathFile=\"" + pathFile + "\""
                                                + " intProgressCounter=" + intProgressCounter
                                                + " intFrom=" + intFrom
                                                + " intRest=" + intRest);
                                    }
                                }
                            } catch (Throwable t) {
                                LOGGER.error("Could not save file."
                                        + " Id=\"" + strId + "\""
                                        + " pathFile=\"" + pathFile + "\""
                                        + " Throwable=\"" + t.toString() + "\"");
                            }
                            updateProgress(intTextLen, intTextLen);
                            String strMsg = "File Save finished (" + intTextLen + " bytes).";
                            updateMessage(strMsg);
                            LOGGER.debug(strMsg + " pathFile=\"" + pathFile + "\"");
                            intFileSaveCount++;
                            return "OK";
                        }
                    };
                }
            };
            this.serviceFileSave.onScheduledProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    event.consume();
                    textArea.textProperty().removeListener(invalidationListenerFileContent);
                    intPropCaretPositionProperty.removeListener(caretPositionChangeListener);
                    booPropFocusedProperty.removeListener(focusedPropertyChangeListener);
                    hboxState.visibleProperty().set(true);
                    lblFileState.textProperty().set("");
                    progressBar.progressProperty().bind(serviceFileSave.progressProperty());
                    lblFileState.textProperty().bind(serviceFileSave.messageProperty());
                    LOGGER.debug("onScheduledProperty serviceFileSave."
                            + " Id=\"" + strId + "\""
                            + " intFileSaveCount=" + intFileSaveCount
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                }
            });

            this.serviceFileSave.onRunningProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    event.consume();
                    LOGGER.debug("onRunningProperty serviceFileSave."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                }
            });

            this.serviceFileSave.onFailedProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    textArea.textProperty().addListener(invalidationListenerFileContent);
                    intPropCaretPositionProperty.addListener(caretPositionChangeListener);
                    booPropFocusedProperty.addListener(focusedPropertyChangeListener);
                    booFileModified = false;
                    progressBar.progressProperty().unbind();
                    lblFileState.textProperty().unbind();
                    String strMsg = serviceFileSave.messageProperty().get();
                    lblFileState.textProperty().set(strMsg);
                    LOGGER.debug("onFailedProperty serviceFileSave."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                    event.consume();
                }
            });

            this.serviceFileSave.onSucceededProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    event.consume();
                    textArea.textProperty().addListener(invalidationListenerFileContent);
                    intPropCaretPositionProperty.addListener(caretPositionChangeListener);
                    booPropFocusedProperty.addListener(focusedPropertyChangeListener);
                    booFileModified = false;
                    ReadOnlyObjectProperty<Worker.State> stateProperty = serviceFileSave.stateProperty();
                    Worker.State state = stateProperty.getValue();
                    String stateName = state.name();
                    progressBar.progressProperty().unbind();
                    lblFileState.textProperty().unbind();
                    String strResult = (String) serviceFileSave.getValue();
                    String strMsg = serviceFileSave.getMessage();
                    lblFileState.textProperty().set(strMsg);

                    LOGGER.debug("onSucceededProperty serviceFileSave."
                            + " Id=\"" + strId + "\""
                            + " intFileSaveCount=" + intFileSaveCount
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\""
                            + "\nstateProperty=\"" + stateProperty + "\""
                            + "\nstate=\"" + state + "\""
                            + " stateName=\"" + stateName + "\""
                            + "\nstrMsg=\"" + strMsg + "\""
                            + "\nstrResult=\"" + strResult + "\"");
                }
            });
            LOGGER.debug("Created serviceFileSave."
                    + " Id=\"" + this.strId + "\""
                    + " pathFile=\"" + this.pathFile + "\""
                    + " serviceFileSave=\"" + this.serviceFileSave + "\"");
        }

        this.serviceFileSave.reset();
        LOGGER.info("Saving file."
                + " Id=\"" + this.strId + "\""
                + " pathFile=\"" + this.pathFile + "\"");
        this.serviceFileSave.start();

        this.booFileModified = false;
        return true;
    }

    // -------------------------------------------------------------------------------------
    public boolean closeFile() {

        this.textArea.clear();
        return true;
    }

    // -------------------------------------------------------------------------------------
    public int find(String strTextFind, boolean booFindAll) {

        if (strTextFind == null || strTextFind.isEmpty()) {
            return -1;
        }
        String strContent = this.getContent();
        if (strContent == null || strContent.isEmpty()) {
            return -1;
        }
        int intTextFindLen = strTextFind.length();
        int intCount = 0;
        int intPosFound;
        if (booFindAll) {
            // Find all occurrences of text in file content.
            intPosFound = 0;
            while (intPosFound != -1) {
                intPosFound = strContent.indexOf(strTextFind, intPosFound);
                if (intPosFound != -1) {
                    intCount++;
                    LOGGER.trace("Find Text."
                            + " Id=\"" + this.strId + "\""
                            + " TextFind=\"" + strTextFind + "\""
                            + " Count=" + intCount
                            + " PosFound=" + intPosFound);
                    intPosFound += intTextFindLen;
                }
            }
        } else {
            // Find first occurent of text from Cursor position
            int intCaretPos = this.intPropCaretPosition.getValue();
            intPosFound = strContent.indexOf(strTextFind, intCaretPos);
            if (intPosFound >= 0) {
                intCount = 1;

                this.requestFocus();

            } else {
                intPosFound = strContent.indexOf(strTextFind, 0);
                if (intPosFound >= 0) {
                    intCount = 1;
                }
            }
        }
        LOGGER.debug("Found Text."
                + " Id=\"" + this.strId + "\""
                + " TextFind=\"" + strTextFind + "\""
                + " Count=" + intCount);
        return intCount;
    }

    // -------------------------------------------------------------------------------------
    public int replace(String strTextFind, String strTextReplace) {

        if (strTextFind == null || strTextFind.isEmpty()) {
            return -1;
        }
        if (strTextReplace == null || strTextReplace.isEmpty()) {
            return -1;
        }
        String strContent = this.getContent();
        if (strContent == null || strContent.isEmpty()) {
            return -1;
        }
        int intTextFindLen = strTextFind.length();
        int intContentLen = strContent.length();

        int intPos = strContent.indexOf(strTextFind, this.intPropCaretPosition.getValue());
        if (intPos < 0) {
            LOGGER.debug("Replace Text not found from cursor position."
                    + " Id=\"" + this.strId + "\""
                    + " TextFind=\"" + strTextFind + "\""
                    + " TextReplace=\"" + strTextReplace + "\"");
            intPos = strContent.indexOf(strTextFind, 0);
            if (intPos < 0) {
                LOGGER.debug("Replace Text not found in content."
                        + " Id=\"" + this.strId + "\""
                        + " TextFind=\"" + strTextFind + "\""
                        + " TextReplace=\"" + strTextReplace + "\"");
                return 0;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(strContent.substring(0, intPos));
        sb.append(strTextReplace);
        int intPosEndFoundFromCursor = intPos + intTextFindLen;
        if (intContentLen > intPosEndFoundFromCursor) {
            sb.append(strContent.substring(intPosEndFoundFromCursor));
        }
        String strTextUpdated = sb.toString();
        this.setFileContent(strTextUpdated);
        LOGGER.debug("Replace Text found."
                + " Id=\"" + this.strId + "\""
                + " TextFind=\"" + strTextFind + "\""
                + " TextReplace=\"" + strTextReplace + "\""
                + " intPos" + intPos
                + " intTextFindLen" + intTextFindLen
                + " intPosEndFoundFromCursor" + intPosEndFoundFromCursor);
        return 1;
    }

    // -------------------------------------------------------------------------------------
    public int replaceAll(String strTextFind, String strTextReplace) {

        int intReplacedCount = 0;
        int intReplaced;
        while ((intReplaced = this.replace(strTextFind, strTextReplace)) > 0) {
            intReplacedCount += intReplaced;
        }
        return intReplacedCount;
    }

    // -------------------------------------------------------------------------------------
    public long getFileSize() {

        if (this.pathFile == null) {
            return 0;
        } else {
            long lngFileSize;
            try {
                lngFileSize = Files.size(pathFile);
            } catch (IOException ex) {
                LOGGER.error("Could not get File size."
                        + " Id=\"" + this.strId + "\""
                        + " pathFile=\"" + this.pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return 0;
            }
            return lngFileSize;
        }
    }

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------
    private void parseFilePath(final String strId, Path pathFile) {

        if (pathFile == null) {
            this.strFilePath = "";
            this.strFileName = "";
            this.strFileExt = "";
            this.strFileDir = "";
            LOGGER.error("File Path is null."
                    + " Id=\"" + strId + "\"");
        } else {
            this.strFilePath = pathFile.toString();
            final int intFileNamePos = this.strFilePath.lastIndexOf(File.separator);
            if (intFileNamePos < 0) {
                this.strFileDir = "";
                this.strFileName = strFilePath;
            } else {
                this.strFileDir = this.strFilePath.substring(0, intFileNamePos);
                this.strFileName = this.strFilePath.substring(intFileNamePos + 1);
            }

            final int intFileNameExtPos = strFileName.lastIndexOf(".");
            final String strFileNameExt;
            if (intFileNameExtPos < 0 || intFileNameExtPos == 0 || intFileNameExtPos == this.strFileName.length()) {
                strFileNameExt = "";
            } else {
                strFileNameExt = this.strFileName.substring(intFileNameExtPos + 1);
            }
            this.strFileExt = strFileNameExt;
            LOGGER.info("Parsed File."
                    + " Id=\"" + strId + "\""
                    + " FilePath=\"" + this.pathFile + "(" + this.strFilePath + ")\""
                    + " FileNamePos=\"" + intFileNamePos + "\""
                    + " FileName=\"" + this.strFileName + "\""
                    + " FileDir=\"" + this.strFileDir + "\""
                    + " FileNameExtPos=\"" + intFileNameExtPos + "\""
                    + " FileNameExt=\"" + this.strFileExt + "\"");
        }
    }

    // -------------------------------------------------------------------------------------
    private static String canSaveFile(String strId, Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Cannot save null File."
                    + " TabId=\"" + strId + "\"");
            return "File is null";
        }

        if (!Files.exists(pathFile)) {
            try {
                Path pathNewFile = Files.createFile(pathFile);
                LOGGER.info("Create New File."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " pathNewFile=\"" + pathNewFile + "\"");
            } catch (IOException ex) {
                LOGGER.error("Could not create File."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return "Create File exception:" + ex.toString();
            }
        }
        try {
            if (Files.isDirectory(pathFile)) {
                // It should never happen, but ...
                LOGGER.error("File is directory."
                        + " TabId=\"" + strId + "\""
                        + " pathFile=\"" + pathFile + "\"");
                return "File is directory";
            }
            if (Files.isWritable(pathFile)) {
                return null;
            } else {
                if (Files.isReadable(pathFile)) {
                    return "Could not save Read-only File";
                }
                return "Could not save not writable File";
            }
        } catch (Throwable t) {
            LOGGER.error("Could not analize File because of security violation."
                    + " TabId=\"" + strId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " Throwable=\"" + t.toString() + "\"");
            return "File save exception:" + t.toString();
        }
    }

    // -------------------------------------------------------------------------------------
    private static void renameFileToBackup(String strTabId, Path pathFile) {

        if (pathFile == null) {
            LOGGER.error("Could not create *bak File for null File."
                    + " TabId=\"" + strTabId + "\"");
            return;
        }

        if (Files.isDirectory(pathFile)) {
            LOGGER.error("Could not create *bak File for directory."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (!Files.exists(pathFile)) {
            LOGGER.error("Could not create *bak File because File does not exist."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (!Files.isRegularFile(pathFile)) {
            LOGGER.error("Could not create *bak File because it's not a Regular File."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\"");
            return;
        }
        if (Settings.BOO_BACKUP_FILES_DAILY_ONLY) {
            FileTime ft;
            try {
                ft = Files.getLastModifiedTime(pathFile);
            } catch (IOException ex) {
                LOGGER.error("Could not get getLastModifiedTime."
                        + " TabId=\"" + strTabId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " IOException=\"" + ex.toString() + "\"");
                return;
            }
            long lngFileModifiedDays = ft.to(TimeUnit.DAYS);

            LocalDate localDate = LocalDate.now();
            long lngLocalDateEpochDay = localDate.toEpochDay();

            LOGGER.debug("Compare File Modified Days and Current Day."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " FileTime=\"" + ft + "\""
                    + " lngFileModifiedDays=\"" + lngFileModifiedDays + "\""
                    + " localDate=\"" + localDate + "\""
                    + " lngLocalDateEpochDay=\"" + lngLocalDateEpochDay + "\"");
            if (lngFileModifiedDays - lngLocalDateEpochDay >= 0) {
                LOGGER.debug("Skip updating backup files."
                        + " TabId=\"" + strTabId + "\""
                        + " pathFile=\"" + pathFile + "\""
                        + " lngFileModifiedDays=\"" + lngFileModifiedDays + "\""
                        + " lngLocalDateEpochDay=\"" + lngLocalDateEpochDay + "\"");
                return;
            }
        }

        // Compute FilePath string without file extension.
        String strFilePath = pathFile.toString();
        String strFilePathNoExt;
        int intPos = strFilePath.lastIndexOf(".");
        if (intPos <= 0) {
            strFilePathNoExt = strFilePath;
        } else {
            strFilePathNoExt = strFilePath.substring(0, intPos);
        }

// TODO:  Always backup Favorites file after editing.
        boolean booFileBackupOldest = true;
        Path pathFileBackupOld = null;
        Path pathFileBackup = null;
        for (int i = Settings.INT_BACKUP_FILES_MAX - 1; i >= 0; i--) {
            String strFileNameBackupCount;
            if (i == 0) {
                strFileNameBackupCount = "";
            } else {
                strFileNameBackupCount = "(" + i + ")";
            }
            String strFilenameBackup = strFilePathNoExt + strFileNameBackupCount + "." + Settings.STR_BACKUP_FILES_EXT;
            pathFileBackup = FileSystems.getDefault().getPath(strFilenameBackup);
            if (Files.exists(pathFileBackup)) {
                if (booFileBackupOldest) {
                    try {
                        Files.deleteIfExists(pathFileBackup);
                    } catch (Throwable t) {
                        LOGGER.error("Could not delete oldest *.bak File."
                                + " TabId=\"" + strTabId + "\""
                                + " FilePathBak=\"" + pathFileBackup + "\""
                                + " Throwable=\"" + t.toString() + "\"");
                        return;
                    }
                } else {
                    try {
                        Files.move(pathFileBackup, pathFileBackupOld);
                    } catch (Throwable t) {
                        LOGGER.error("Could not rename *.bak File."
                                + " TabId=\"" + strTabId + "\""
                                + " pathFile=\"" + pathFile + "\""
                                + " pathFileBackup=\"" + pathFileBackup + "\""
                                + " pathFileBackupOld=\"" + pathFileBackupOld + "\""
                                + " Throwable=\"" + t.toString() + "\"");
                        return;
                    }
                    LOGGER.debug("Renamed *.bak File."
                            + " TabId=\"" + strTabId + "\""
                            + " pathFile=\"" + pathFile + "\""
                            + " pathFileBackup=\"" + pathFileBackup + "\""
                            + " pathFileBackupOld=\"" + pathFileBackupOld + "\"");
                }
            }
            booFileBackupOldest = false;
            pathFileBackupOld = pathFileBackup;
        }
        try {
            Files.move(pathFile, pathFileBackup);
        } catch (Throwable t) {
            LOGGER.error("Could not rename File to *.bak File."
                    + " TabId=\"" + strTabId + "\""
                    + " pathFile=\"" + pathFile + "\""
                    + " pathFileBackup=\"" + pathFileBackup + "\""
                    + " Throwable=\"" + t.toString() + "\"");
            return;
        }
        LOGGER.debug("Renamed *.bak File."
                + " TabId=\"" + strTabId + "\""
                + " pathFile=\"" + pathFile + "\""
                + " pathFileBackup=\"" + pathFileBackup + "\"");
    }

    // -------------------------------------------------------------------------------------
    private void processTask() {

        if (this.taskFileLoad == null) {
        } else {
            ReadOnlyObjectProperty<Worker.State> stateProperty = this.taskFileLoad.stateProperty();
            Worker.State state = stateProperty.getValue();
            String stateName = state.name();

            LOGGER.debug("Got Task."
                    + " Id=\"" + this.strId + "\""
                    + " taskFileLoad=\"" + this.taskFileLoad + "\""
                    + " stateProperty=\"" + stateProperty + "\""
                    + " state=\"" + state + "\""
                    + " stateName=\"" + stateName + "\""
            );

            this.taskFileLoad.onScheduledProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    progressBar.progressProperty().bind(taskFileLoad.progressProperty());
                    lblFileState.textProperty().bind(taskFileLoad.messageProperty());
                    textArea.textProperty().removeListener(invalidationListenerFileContent);
                    LOGGER.debug("onScheduledProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                    event.consume();

                }
            });

            this.taskFileLoad.onRunningProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    LOGGER.debug("onRunningProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\"");
                    event.consume();

                }
            });

            this.taskFileLoad.onFailedProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    EventType eventType = event.getEventType();
                    String strErrMsg = lblFileState.textProperty().getValue()
                            + "\nFile=" + lblFileName.getText()
                            + "\nTry to Open File with different Charset or Open File Binary.";
                    lblFileState.textProperty().unbind();
                    lblFileState.textProperty().setValue(strErrMsg);
                    textArea.setText(strErrMsg);
                    textArea.setEditable(false);
                    booFileModified = false;
                    LOGGER.debug("onFailedProperty."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\""
                            + " ErrMsg=\"" + strErrMsg + "\"");
                    event.consume();
                }
            });

            this.taskFileLoad.onSucceededProperty().set(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ReadOnlyObjectProperty<Worker.State> stateProperty = taskFileLoad.stateProperty();
                    Worker.State state = stateProperty.getValue();
                    String stateName = state.name();
                    EventType eventType = event.getEventType();
                    String strText;
                    int intTextLen = 0;
                    try {
                        strText = (String) taskFileLoad.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        LOGGER.error("onSucceededProperty."
                                + " Id=\"" + strId + "\""
                                + " Exception=\"" + ex.toString() + "\"");
                        strText = "";
                    }
                    // TODO: filter text !!!???
                    int intTextLogLimit = 100;
                    String strTextPart = "";
                    if (strText != null) {
                        intTextLen = strText.length();
                        int intTrim = Math.min(intTextLen, intTextLogLimit);
                        strTextPart = strText.substring(0, intTrim) + "\n...";
                    }
                    String strLineEnding = spLineEnding.getValue();
                    LOGGER.trace("onSucceededProperty got File content."
                            + " Id=\"" + strId + "\""
                            + " event=\"" + event + "\""
                            + " stateProperty=\"" + stateProperty + "\""
                            + " state=\"" + state + "\""
                            + " stateName=\"" + stateName + "\""
                            + " strLineEnding=\"" + strLineEnding + "\""
                            + "\nintTextLen=\"" + intTextLen + "\""
                            + "\nstrText=\"" + strTextPart + "\""
                    );
                    event.consume();
                    setFileContent(strText);
                    textArea.setText(strText);
                    textArea.setWrapText(booTextWrap);
                    textArea.textProperty().addListener(invalidationListenerFileContent);
                    lblFileState.textProperty().unbind();
                    String strMsg = taskFileLoad.getMessage();
                    lblFileState.textProperty().set(strMsg);//.unbind();
                    booFileModified = false;
                    LOGGER.debug("onSucceededProperty set text to textArea."
                            + " Id=\"" + strId + "\""
                            + " eventType=\"" + eventType + "\""
                            + " event=\"" + event + "\""
                            + "\nstrLineEnding=\"" + strLineEnding + "\""
                            + "\nstrMsg=\"" + strMsg + "\""
                            + "\nintTextLen=\"" + intTextLen + "\""
                            + "\nstrText=\"" + strTextPart + "\"");

                    if (tabPane == null) {
                        LOGGER.error("onSucceededProperty tabPane is null."
                                + " Id=\"" + strId + "\""
                                + " eventType=\"" + eventType + "\""
                                + " event=\"" + event + "\"");
                    } else {
                        EventTarget eventTarget = tabPane;
                        EventFileRead eventFileRead = new EventFileRead(this, eventTarget, EventFileRead._FILE_READ);
                        eventFileRead.setId(strId);
                        eventFileRead.setLineEnding(strLineEnding);
                        eventFileRead.setCharsetName(strCharsetName);
                        tabPane.fireEvent(eventFileRead);
                    }
                }
            });
        }
    }

    // -------------------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------------------
    public final TextArea getTexArea() {
        return this.textArea;
    }

    public final String getContent() {
        return this.textArea.getText();
    }

    public final void setFileContent(String strFileContent) {
        this.textArea.setText(strFileContent);
    }

    public final Path getPathFile() {
        return this.pathFile;
    }

    public final String getFilePath() {
        return this.strFilePath;
    }

    public final String getFileName() {
        return this.strFileName;
    }

    public String getFileExt() {
        return this.strFileExt;
    }

    public final String getFileDir() {
        return this.strFileDir;
    }

    public final boolean isFileModified() {
        if (this.pathFile == null) {
            return false;
        } else {
            return this.booFileModified;
        }
    }

    // -------------------------------------------------------------------------------------
    public boolean isTextWrap() {
        return this.booTextWrap;
    }

    public void setTextWrap(boolean booTextWrap) {
        this.booTextWrap = booTextWrap;
        this.textArea.wrapTextProperty().set(booTextWrap);
    }

    // -------------------------------------------------------------------------------------
    public String getLineEnding() {

        return this.spLineEnding.getValue();
    }

    public void setLineEnding(String strLineEnding) {

        this.spLineEnding.setValue(strLineEnding);
    }

    public void setLineEndingWin() {

        this.spLineEnding.setValue(STR_CR_LF_WIN);
    }

    public void setLineEndingUnix() {

        this.spLineEnding.setValue(STR_LF_UNIX);
    }

    // -------------------------------------------------------------------------------------
    public String getCharsetName() {
        return this.strCharsetName;
    }

    public void setCharsetName(String strCharsetName) {
        this.strCharsetName = strCharsetName;
    }

    // -------------------------------------------------------------------------------------
    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
        this.textArea.setFont(this.font);
    }

    // -------------------------------------------------------------------------------------
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    // -------------------------------------------------------------------------------------
}
