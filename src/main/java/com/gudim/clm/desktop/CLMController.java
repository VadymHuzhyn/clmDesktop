package com.gudim.clm.desktop;

import static com.gudim.clm.desktop.util.CLMConstant.ADDON_PATH;
import static com.gudim.clm.desktop.util.CLMConstant.INCORRECT_DIRECTORY_HEADER;
import static com.gudim.clm.desktop.util.CLMConstant.INCORRECT_DIRECTORY_MESSAGE;

import com.gudim.clm.desktop.service.CLMService;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.AllArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("consulLootMaster.fxml")
@AllArgsConstructor
public class CLMController {
	
	private final CLMService clmService;
	@FXML
	public        Label      statusBar;
	@FXML
	public        Button     uploadJson;
	@FXML
	public        Button     downloadJson;
	@FXML
	public        Button     directoryChooser;
	@FXML
	public        Label      directoryPath;
	
	@FXML
	public void convertXLSToJSON() {
		clmService.downloadXLSXFromDrive();
		clmService.generateLuaTableWishlist(directoryPath.getText(),
		                                    clmService.convertXLSXToJson());
		
	}
	
	@FXML
	public void convertJSONTonXLS() {
		// TODO need fix Chine API and ACE3 addon
	}
	
	@FXML
	public void openFile() {
		File selectedDirectory = clmService.getFile();
		if (selectedDirectory != null && selectedDirectory.getAbsolutePath().endsWith(ADDON_PATH)) {
			directoryPath.setText(selectedDirectory.getAbsolutePath());
			downloadJson.setDisable(false);
			uploadJson.setDisable(true); //todo wait for update WOW API after SET FALSE
			directoryChooser.setDisable(true);
		} else {
			showError(AlertType.WARNING, INCORRECT_DIRECTORY_HEADER, INCORRECT_DIRECTORY_MESSAGE);
		}
	}
	
	public void showError(AlertType warning, String header, String contentText) {
		Alert errorAlert = new Alert(warning);
		errorAlert.setHeaderText(header);
		errorAlert.setContentText(contentText);
		errorAlert.showAndWait();
	}
	
}
