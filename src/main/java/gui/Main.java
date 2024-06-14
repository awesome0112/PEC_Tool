package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.FilePath;
import utils.cloneProjectUtil.CloneProjectUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        setUp();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ChooseToolScene.fxml")));        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setTitle("NTD-AUT tool");
        stage.setScene(scene);
        stage.show();
    }

    public void setUp() throws IOException {
        File concreteExecuteResultFile = new File(FilePath.concreteExecuteResultPath);
        File generatedTestDataFile = new File(FilePath.generatedTestDataPath);
        if (!concreteExecuteResultFile.exists()) {
            concreteExecuteResultFile.createNewFile();
        }

        if (!generatedTestDataFile.exists()) {
            generatedTestDataFile.createNewFile();
        }
    }

    public static void main(String[] args) throws IOException {
        launch();
        CloneProjectUtil.deleteFilesInDirectory(FilePath.clonedProjectPath);
    }
}