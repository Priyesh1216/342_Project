# SOEN 342 Project - Section II

## Group Members

- Priyesh Patel (40283049)
- Matthew Lucas Santiago (40284787)
- Sasha Klein-Charland (40281076)



## How to run

1. Check your Java version in the terminal with `java -version`
2. Ensure the the IDE is using the UTF-8 encoding.
3. If JavaFX is not already installed
   - Download JavaFX SDK from: https://gluonhq.com/products/javafx/ (choose a version that is lower than your current Java version)
   - Extract the zip file to a location on your computer
   - Note the path to the lib folder in the javafx-sdk folder (example: C:\javafx-sdk-17\lib or /path/to/javafx-sdk-17/lib
4. Add the launch.json and settings.json files in the .vscode folder (see sample code below)
5. Change the FILE_PATH to the file path of you JavaFX lib folder in the launch.json and settings.json files.
6. Run the following commands in the main folder of the code (342_Project/src/main)<br>
     **To compile:** `javac -encoding UTF-8 --module-path "FILE_PATH" --add-modules javafx.controls *.java`<br>
     **To run:** `java --module-path "FILE_PATH" --add-modules javafx.controls RailConnectGUI`

#### Create the following files in the rooot of the project if they don't exist:

**`.vscode/launch.json`**
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch RailConnectGUI",
            "request": "launch",
            "mainClass": "RailConnectGUI",
            "projectName": "Railway_Connection_Search",
            "vmArgs": "--module-path \"FILE_PATH\" --add-modules javafx.controls",
            "encoding": "UTF-8"
        }
    ]
}
```

**`.vscode/settings.json`**
```json
{
    "java.project.referencedLibraries": [
        "FILE_PATH/*.jar",
        "lib/**/*.jar"
    ]
}
```

