module com.olexyarm.jfxfilecontenteditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires java.desktop;

    opens com.olexyarm.jfxfilecontenteditor to javafx.fxml;
    exports com.olexyarm.jfxfilecontenteditor;
}
