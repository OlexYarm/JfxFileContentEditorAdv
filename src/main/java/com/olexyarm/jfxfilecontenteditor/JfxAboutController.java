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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JfxAboutController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxAboutController.class);

    @FXML
    public GridPane gridPaneAbout;

    // -------------------------------------------------------------------------------------
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {

        LOGGER.debug("### JfxAboutController initialize.");

        int intRowIndex = 0;
        Label lblAppNameValue = new Label(Settings.STR_APP_TITLE);
        lblAppNameValue.setStyle("-fx-font: NORMAL 15 Tahoma;");
        gridPaneAbout.add(lblAppNameValue, 0, intRowIndex, 2, 1);

        Label lblVersionValue = new Label(Settings.STR_VERSION);
        gridPaneAbout.add(lblVersionValue, 0, ++intRowIndex, 2, 1);

        Label lblBuildValue = new Label(Settings.STR_BUILD_TIME);
        gridPaneAbout.add(lblBuildValue, 0, ++intRowIndex, 2, 1);

        Label lblBuildJavaHome = new Label(Settings.STR_BUILD_JAVA_HOME);
        gridPaneAbout.add(lblBuildJavaHome, 0, ++intRowIndex, 2, 1);

        Label lblBuildOs = new Label(Settings.STR_BUILD_OS);
        gridPaneAbout.add(lblBuildOs, 0, ++intRowIndex, 2, 1);

        ++intRowIndex;
        Label lblRuntime = new Label("Current Enviroment");
        gridPaneAbout.add(lblRuntime, 0, ++intRowIndex, 2, 1);

        Label lblOS = new Label("OS:");
        gridPaneAbout.add(lblOS, 0, ++intRowIndex);
        String strOSValue = Settings.osName();
        Label lblOsValue = new Label(strOSValue);
        gridPaneAbout.add(lblOsValue, 1, intRowIndex);

        Label lblJavaVersion = new Label("Java Version:");
        gridPaneAbout.add(lblJavaVersion, 0, ++intRowIndex);
        String strJavaVersion = Settings.javaVersion();
        Label lblJavaVersionValue = new Label(strJavaVersion);
        gridPaneAbout.add(lblJavaVersionValue, 1, intRowIndex);

        Label lblJavaFXVersion = new Label("Java FX Version:");
        gridPaneAbout.add(lblJavaFXVersion, 0, ++intRowIndex);
        String strJavafxVersion = Settings.javafxVersion();
        Label lblJavaFXVersionValue = new Label(strJavafxVersion);
        gridPaneAbout.add(lblJavaFXVersionValue, 1, intRowIndex);

        intRowIndex += 2;
        Label lblAutor = new Label("Autor:");
        gridPaneAbout.add(lblAutor, 0, ++intRowIndex);
        Label lblAuthorValue = new Label("Oleksandr Yarmolenko");
        gridPaneAbout.add(lblAuthorValue, 1, intRowIndex);

        intRowIndex += 2;
        Label lblIcons = new Label("Icons by:");
        gridPaneAbout.add(lblIcons, 0, ++intRowIndex);
        Label lblIconsValue = new Label("https://icons8.com/");
        gridPaneAbout.add(lblIconsValue, 1, intRowIndex);

    }
}
