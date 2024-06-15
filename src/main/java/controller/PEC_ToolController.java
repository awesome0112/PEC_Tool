package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.FilePath;
import utils.autoUnitTestUtil.autoTesting.PECTesting;
import utils.autoUnitTestUtil.testResult.PECDataSetResult;
import utils.autoUnitTestUtil.testResult.PECResult;
import utils.cloneProjectUtil.CloneProjectUtil;
import utils.cloneProjectUtil.projectTreeObjects.Folder;
import utils.cloneProjectUtil.projectTreeObjects.JavaFile;
import utils.cloneProjectUtil.projectTreeObjects.ProjectTreeObject;
import utils.cloneProjectUtil.projectTreeObjects.Unit;
import utils.uploadUtil.NTDUploadUtil;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PEC_ToolController implements Initializable {
    private FileChooser fileChooser = new FileChooser();
    private File choseFile;
    private Unit choseUnit;
    private PEC_ToolController.Coverage choseCoverage;

    public enum Coverage {
        STATEMENT,
        BRANCH
    }

    @FXML
    private Label DS1CoverageLabel;

    @FXML
    private Label DS1MemLabel;

    @FXML
    private Label DS1TCnLabel;

    @FXML
    private ListView<List<Object>> DS1TestCaseList;

    @FXML
    private ListView<List<Object>> DS2TestCaseList;

    @FXML
    private ListView<List<Object>> DS3TestCaseList;

    @FXML
    private ListView<List<Object>> DS4TestCaseList;

    @FXML
    private Label DS1TimeLabel;

    @FXML
    private Label DS2CoverageLabel;

    @FXML
    private Label DS2MemLabel;

    @FXML
    private Label DS2TCnLabel;

    @FXML
    private Label DS2TimeLabel;

    @FXML
    private Label DS3CoverageLabel;

    @FXML
    private Label DS3MemLabel;

    @FXML
    private Label DS3TCnLabel;

    @FXML
    private Label DS3TimeLabel;

    @FXML
    private Label DS4CoverageLabel;

    @FXML
    private Label DS4MemLabel;

    @FXML
    private Label DS4TCnLabel;

    @FXML
    private Label DS4TimeLabel;

    @FXML
    private Label alertLabel;

    @FXML
    private ChoiceBox<String> coverageChoiceBox;

    @FXML
    private Label filePreview;

    @FXML
    private Button generateButton;

    @FXML
    private TreeView<ProjectTreeObject> projectTreeView;

    @FXML
    private Button uploadFileButton;

    @FXML
    private AnchorPane dataSetInfoContainer;

    @FXML
    void chooseFileButtonClicked(MouseEvent event) {
        choseFile = fileChooser.showOpenDialog(new Stage());
        if (choseFile != null) {
            filePreview.setText(choseFile.getAbsolutePath());
            uploadFileButton.setDisable(false);
        }
    }

    @FXML
    void generateButtonClicked(MouseEvent event) {
        alertLabel.setText("");
        resetDataSets();

        PECResult result;
        try {
            result = PECTesting.runFullPEC(choseUnit.getPath(), choseUnit.getMethodName(), choseUnit.getClassName(), choseCoverage);
        } catch (Exception | StackOverflowError e) {
            alertLabel.setTextFill(Paint.valueOf("red"));
            alertLabel.setText("Examined unit contains cases we haven't handle yet!");
            return;
        }

        PECDataSetResult DS1 = result.getDS1();
        PECDataSetResult DS2 = result.getDS2();
        PECDataSetResult DS3 = result.getDS3();
        PECDataSetResult DS4 = result.getDS4();

        DS1TimeLabel.setText(DS1.getGenerateTime() + " ms");
        DS1MemLabel.setText(DS1.getMemoryUsed() + " MB");
        DS1CoverageLabel.setText(DS1.getCoverage() + " %");
        DS1TCnLabel.setText(DS1.getTestCaseNum() + "");
        DS1TestCaseList.getItems().addAll(DS1.getTestDataList());

        DS2TimeLabel.setText(DS2.getGenerateTime() + " ms");
        DS2MemLabel.setText(DS2.getMemoryUsed() + " MB");
        DS2CoverageLabel.setText(DS2.getCoverage() + " %");
        DS2TCnLabel.setText(DS2.getTestCaseNum() + "");
        DS2TestCaseList.getItems().addAll(DS2.getTestDataList());

        DS3TimeLabel.setText(DS3.getGenerateTime() + " ms");
        DS3MemLabel.setText(DS3.getMemoryUsed() + " MB");
        DS3CoverageLabel.setText(DS3.getCoverage() + " %");
        DS3TCnLabel.setText(DS3.getTestCaseNum() + "");
        DS3TestCaseList.getItems().addAll(DS3.getTestDataList());

        DS4TimeLabel.setText(DS4.getGenerateTime() + " ms");
        DS4MemLabel.setText(DS4.getMemoryUsed() + " MB");
        DS4CoverageLabel.setText(DS4.getCoverage() + " %");
        DS4TCnLabel.setText(DS4.getTestCaseNum() + "");
        DS4TestCaseList.getItems().addAll(DS4.getTestDataList());

        dataSetInfoContainer.setDisable(false);

    }

    private TreeItem<ProjectTreeObject> switchToTreeItem(ProjectTreeObject treeObject) {
        if (treeObject instanceof Folder) {
            TreeItem<ProjectTreeObject> item = new TreeItem<>(treeObject, new ImageView(new Image("\\img\\folder_icon.png")));
            List<ProjectTreeObject> children = ((Folder) treeObject).getChildren();
            for (ProjectTreeObject child : children) {
                item.getChildren().add(switchToTreeItem(child));
            }
            return item;
        } else if (treeObject instanceof JavaFile) {
            TreeItem<ProjectTreeObject> item = new TreeItem<>(treeObject, new ImageView(new Image("\\img\\java_file_icon.png")));
            List<Unit> units = ((JavaFile) treeObject).getUnits();
            for (Unit unit : units) {
                item.getChildren().add(switchToTreeItem(unit));
            }
            return item;
        } else if (treeObject instanceof Unit) {
            return new TreeItem<>(treeObject, new ImageView(new Image("\\img\\unit_icon.png")));
        } else {
            throw new RuntimeException("Invalid ProjectTreeObject");
        }
    }

    private void reset() {
        projectTreeView.setRoot(null);
        coverageChoiceBox.setValue("");
        coverageChoiceBox.setDisable(true);
        generateButton.setDisable(true);
        alertLabel.setText("");
        resetDataSets();
    }

    private void resetDataSets() {
        dataSetInfoContainer.setDisable(true);

        DS1TimeLabel.setText("xxx ms");
        DS1MemLabel.setText("xxx MB");
        DS1CoverageLabel.setText("xxx %");
        DS1TCnLabel.setText("xxx");
        DS1TestCaseList.getItems().clear();

        DS2TimeLabel.setText("xxx ms");
        DS2MemLabel.setText("xxx MB");
        DS2CoverageLabel.setText("xxx %");
        DS2TCnLabel.setText("xxx");
        DS2TestCaseList.getItems().clear();

        DS3TimeLabel.setText("xxx ms");
        DS3MemLabel.setText("xxx MB");
        DS3CoverageLabel.setText("xxx %");
        DS3TCnLabel.setText("xxx");
        DS3TestCaseList.getItems().clear();

        DS4TimeLabel.setText("xxx ms");
        DS4MemLabel.setText("xxx MB");
        DS4CoverageLabel.setText("xxx %");
        DS4TCnLabel.setText("xxx");
        DS4TestCaseList.getItems().clear();
    }

    @FXML
    void selectUnit(MouseEvent event) {
        TreeItem<ProjectTreeObject> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            ProjectTreeObject treeObject = selectedItem.getValue();
            if (treeObject instanceof Unit) {
                choseUnit = (Unit) treeObject;
                coverageChoiceBox.setDisable(false);
                coverageChoiceBox.setValue("");
                generateButton.setDisable(true);
            } else {
                choseUnit = null;
                coverageChoiceBox.setDisable(true);
                coverageChoiceBox.setValue("");
                generateButton.setDisable(true);
            }
        }
    }

    @FXML
    void uploadFileButtonClicked(MouseEvent event) {
        reset();
        try {
            long startTime = System.nanoTime();

            CloneProjectUtil.deleteFilesInDirectory(FilePath.uploadedProjectPath);
            NTDUploadUtil.javaUnzipFile(choseFile.getPath(), FilePath.uploadedProjectPath);

            String javaDirPath = CloneProjectUtil.getJavaDirPath(FilePath.uploadedProjectPath);
            if (javaDirPath.equals("")) throw new RuntimeException("Invalid project");

            Folder folder = CloneProjectUtil.cloneProject(javaDirPath, FilePath.clonedProjectPath);

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1000000.0;
            duration = (double) Math.round(duration * 100) / 100;

            TreeItem<ProjectTreeObject> rootFolder = switchToTreeItem(folder);

            projectTreeView.setRoot(rootFolder);
            alertLabel.setTextFill(Paint.valueOf("green"));
            alertLabel.setText("Upload time " + duration + "ms");
        } catch (Exception e) {
            alertLabel.setTextFill(Paint.valueOf("red"));
            alertLabel.setText("INVALID PROJECT ZIP FILE (eg: not a zip file, project's source code contains cases we haven't handled, ...)");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        uploadFileButton.setDisable(true);
        generateButton.setDisable(true);
        coverageChoiceBox.setDisable(true);
        coverageChoiceBox.getItems().addAll("Statement coverage", "Branch coverage");
        coverageChoiceBox.setOnAction(this::selectCoverage);
        dataSetInfoContainer.setDisable(true);
    }

    private void selectCoverage(ActionEvent actionEvent) {
        generateButton.setDisable(false);

        String coverage = coverageChoiceBox.getValue();
        if (coverage.equals("Statement coverage")) {
            choseCoverage = PEC_ToolController.Coverage.STATEMENT;
        } else if (coverage.equals("Branch coverage")) {
            choseCoverage = PEC_ToolController.Coverage.BRANCH;
        } else if (coverage.equals("")) {
            // do nothing!
        } else {
            throw new RuntimeException("Invalid coverage");
        }
    }
}
