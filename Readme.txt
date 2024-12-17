The project is improved version of my JavaFX minimalistic text editor (https://github.com/OlexYarm/JfxFileContentEditor).
I added a few useful features, but did not fixed major problem - did not switch from TextArea to better control.
So, still limit on file size, no highlighting for selection, no line numbers ...

1) Overview
The editor has all minimum and necessary features for editing test files small/medium size:
- create new file;
- open existing file with different charsets available for JavaFX installation;
- edit file, search and replace substring in it;
- change font size and font family to view file content;
- print file content;
- save file and keep backup copied of old version of files (up to defined number of copies);
- change line ending and charset while saving file;
- add file to favorites menu;
- edit favorites menu;
- re-open files not closed when editor was terminated;
- accesing all functionality with menu and toolbar;
- use tabs to open/modify/save files;
- adjust a few editor settings (number of backups, view font size and family).

2) Prerequisites
For building editor from source files following programs should be installed:
- Java SDK 22;
- Apache Maven 3.9.3 or later.
After image is created it could be copied and running on any computed supported by Java 22.

3) Building and running editor

3.1) Build and run from IDE using Maven
Download project source code from GitHub, import it to your IDE as project with existing pom.xml file, build and run editor.

3.2) Build from command-line using Maven
Download project source code from GitHub, change directory to project root directory "...\JfxFileContentEditor".
Run below command from command-line to create image:

mvn clean compile javafx:jlink

It will create directory "...\JfxFileContentEditor\target\jfxfilecontenteditor" with all files needed to run editor.
It will also compress all files to Zip archive jfxfilecontenteditor.zip in directory "...\JfxFileContentEditor\target".

3.3) Run image from command-line command
Before running editor the image should be created as explained above.
Copy archive jfxfilecontenteditor.zip to desired location and unzip all files from it.
Or you can copy all files from directory "...\JfxFileContentEditor\target\jfxfilecontenteditor" to desired location.
Change directory to "...\jfxfilecontenteditor\bin".
Run below command from command-line to run editor:

java -m com.olexyarm.jfxfilecontenteditor/com.olexyarm.jfxfilecontenteditor.App %*

or on Windows run bat-file

jfxfilecontenteditor.bat

The editor will create directory "...\JfxEditor" in user's home directory with files "Settings.properties" and "Favorites.txt" in it.
The editor will create directory "...\jfxfilecontenteditor\bin\logs" with log files in it.
Log files will be rolled out and Zip compressed daily. The Zip archive files will be deleted after 60 days.

3.4) Build modular jar file from command-line using Maven
Download project source code from GitHub, change directory to project root directory "...\JfxFileContentEditor".
Run below command from command-line to create modular jar file:

mvn clean package

The directory "...\JfxFileContentEditor\target\release" will be created.
It will contain editor modular jar file and all dependencies jar files (JavaFX and logback).

3.5) Run modular jar file from command-line command
Before running editor modular jar file and dependencies jar files should be created as explained above.
Copy all files from directory "...\JfxFileContentEditor\target\release" to desired location.
Change directory to that directory.
Run below command from command-line to run editor:

java --module-path "." --module com.olexyarm.jfxfilecontenteditor/com.olexyarm.jfxfilecontenteditor.App %*

4) Known limitations
- the editor uses TextArea control to show and edit content of file and it could be slow for large files;
- the "print" menu item always print only first page of file opened in editor because of limitation on implementation print functionality in JavaFX;
- the editor uses java.nio.channels.FileChannel for reading files and it lock opened files until editor is terminated,
this is well known bug in Java since 2002 (https://bugs.openjdk.org/browse/JDK-4724038)
and I did not want to use workaround because it could not work in next Java version (https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java).
