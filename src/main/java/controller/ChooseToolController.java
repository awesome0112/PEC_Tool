package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ChooseToolController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    void chooseConcolicToolButtonClicked(MouseEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/ConcolicAUTScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setResizable(false);
        stage.setTitle("Concolic-AUT");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void chooseNTDToolButtonClicked(MouseEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/fxml/NTDAUTScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setResizable(false);
        stage.setTitle("NTD-AUT");
        stage.setScene(scene);
        stage.show();
    }

}