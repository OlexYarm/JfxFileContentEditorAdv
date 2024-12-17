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
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.Timeline;

public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static Scene SCENE;

    public static void main(String[] args) {

        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        LOGGER.debug("### Creating Scene.");

        FXMLLoader fxmlLoader = Utils.loadFXML("jfxFileContentEditor");
        Parent root = fxmlLoader.load();

        JfxFileContentEditorController controllerEditor = fxmlLoader.<JfxFileContentEditorController>getController();

        JfxFileContentEditorMenuController controllerMenu = controllerEditor.jfxEditorMenuController;
        Timeline timeline = controllerMenu.getTimeline();

        SCENE = new Scene(root, Settings.INT_WINDOW_WIDTH, Settings.INT_WINDOW_HIGH);

        SCENE.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (stage.isMaximized()) {
                    LOGGER.debug("### widthProperty event Stage Maximized.");
                } else {
                    //int intWindowWidthOld = oldValue.intValue();
                    int intWindowWidth = newValue.intValue();
                    Settings.INT_WINDOW_WIDTH = intWindowWidth;
                    Settings.save();
                    /*
                LOGGER.debug("### Changed WindowWidth."
                        + " WindowWidthOld=" + intWindowWidthOld
                        + " WindowWidthNew=" + intWindowWidth);
                     */
                }
            }
        });

        SCENE.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (stage.isMaximized()) {
                    LOGGER.debug("### heightProperty event Stage Maximized.");
                } else {
                    //int intWindowHighOld = oldValue.intValue();
                    int intWindowHigh = newValue.intValue();
                    Settings.INT_WINDOW_HIGH = intWindowHigh;
                    Settings.save();
                    /*
                LOGGER.debug("### Changed WindowHigh."
                        + " WindowHeighOld=" + intWindowHighOld
                        + " WindowHighNew=" + intWindowHigh);
                     */
                }
            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                Settings.INT_WINDOW_POSITION_X = (int) stage.getX();
                Settings.INT_WINDOW_POSITION_Y = (int) stage.getY();
                Settings.BOO_STAGE_MAXIMIZED = stage.isMaximized();
                Settings.save();

                if (Settings.BOO_AUTO_SAVE_FILES_EABLED) {
                    String strNodeID = "tabPaneEditor";
                    Parent root = SCENE.getRoot();
                    TabPane tabPane = Utils.lookupNodeByID(root, TabPane.class, strNodeID);
                    if (tabPane == null) {
                        return;
                    }
                    ObservableList<Tab> lstTabs = tabPane.getTabs();
                    if (Utils.openTabsPreserve(lstTabs)) {
                        boolean booReturn = Utils.showMessage(Alert.AlertType.CONFIRMATION,
                                "Exit Confirmation", "There is File modified and not saved. Do you want to exit?",
                                "Click Yes to exit", "Yes", "No");
                        if (!booReturn) {
                            event.consume();
                        }
                    }
                }
                timeline.stop();
            }
        });

        stage.setX(Settings.INT_WINDOW_POSITION_X);
        stage.setY(Settings.INT_WINDOW_POSITION_Y);

        stage.setMaximized(Settings.BOO_STAGE_MAXIMIZED);
        stage.setScene(SCENE);
        stage.setTitle(Settings.STR_APP_TITLE);
        stage.show();
        LOGGER.debug("### Started APP.");
    }
}
