# SOEN 342 Project - Section II

## Group Members

- Priyesh Patel (40283049)
- Matthew Lucas Santiago (40284787)
- Sasha Klein-Charland (40281076)



## How to run

1. Check your Java version in the terminal with `java -version`
2. If JavaFX is not already installed
   - Download JavaFX SDK from: https://gluonhq.com/products/javafx/ (choose a version that is lower than your current Java version)
   - Extract the zip file to a location on your computer
   - Note the path to the lib folder (example: C:\javafx-sdk-17\lib or /path/to/javafx-sdk-17/lib)
4. Change the FILE_PATH to the file path of you JavaFX lib folder in the launch.json and settings.json files.
5. Run the following commands in the main folder of the code (342_Project/src/main)<br>
  **To compile:** `javac --module-path "FILE_PATH" --add-modules javafx.controls RailConnectGUI.java`<br>
  **To run:** `java --module-path "FILE_PATH" --add-modules javafx.controls RailConnectGUI`
