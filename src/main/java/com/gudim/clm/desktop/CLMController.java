package com.gudim.clm.desktop;

import com.gudim.clm.desktop.service.CLMService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@FxmlView("consulLootMaster.fxml")
public class CLMController {

    private final CLMService clmService;
    @FXML
    public Label statusBar;
    @FXML
    public Button uploadJson;
    @FXML
    public Button downloadJson;
    @FXML
    public Button directoryChooser;
    @FXML
    public Label directoryPath;

    @Autowired
    public CLMController(CLMService clmService) {
        this.clmService = clmService;
    }

    @FXML
    public void convertXLSToJSON() {
        clmService.downloadXLSXFromDrive();
        clmService.generateLuaTableWishlist(directoryPath.getText(), clmService.convertXLSXToJson());
    }

    @FXML
    public void convertJSONTonXLS() {
        // TODO need fix Chine API and ACE3 addon
    }

    @FXML
    public void openFile() {
        File selectedDirectory = clmService.getFile();
        if (selectedDirectory != null && selectedDirectory.getAbsolutePath()
                .endsWith("Interface\\AddOns\\ConsulLootMaster")) {
            directoryPath.setText(selectedDirectory.getAbsolutePath());
            downloadJson.setDisable(false);
            uploadJson.setDisable(true); //todo wait for update WOW API after SET FALSE
            directoryChooser.setDisable(true);
        } else {
            showError(AlertType.WARNING, "Incorrect directory",
                    "The path is incorrect, please enter the correct path to the Consul Loot Master addon");
        }
    }

    public void showError(AlertType warning, String header, String contentText) {
        Alert errorAlert = new Alert(warning);
        errorAlert.setHeaderText(header);
        errorAlert.setContentText(contentText);
        errorAlert.showAndWait();
    }

}
