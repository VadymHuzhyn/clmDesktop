package com.gudim.clm.desktop;

import static com.gudim.clm.desktop.util.CLMConstant.ADDON_PATH;
import static com.gudim.clm.desktop.util.CLMConstant.INCORRECT_DIRECTORY_HEADER;
import static com.gudim.clm.desktop.util.CLMConstant.INCORRECT_DIRECTORY_MESSAGE;
import static com.gudim.clm.desktop.util.CLMConstant.PATH_CLM_ITEMS_LUA;
import static com.gudim.clm.desktop.util.CLMConstant.PATH_CLM_WISHLISTS_LUA;

import com.gudim.clm.desktop.dto.CLMLuaTableDTO;
import com.gudim.clm.desktop.dto.CLMMapDTO;
import com.gudim.clm.desktop.service.CLMService;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("CLM.fxml")
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
	
	public CLMController(CLMService clmService) {
		this.clmService = clmService;
	}
	
	@FXML
	public void convertXLSToJSON() {
		clmService.downloadXLSXFromDrive();
		CLMMapDTO clmMapDTO = clmService.getDataFromXLSX();
		CLMLuaTableDTO clmLuaTableDTO = clmService.luaTableMapper(clmMapDTO);
		clmService.saveLuaTableFile(clmLuaTableDTO.getSbWishlists(),
		                            directoryPath.getText() + PATH_CLM_WISHLISTS_LUA);
		clmService.saveLuaTableFile(clmLuaTableDTO.getSbCLMItems(),
		                            directoryPath.getText() + PATH_CLM_ITEMS_LUA);
		clmService.removeTempFile();
		
	}
	
	@FXML
	public void convertJSONTonXLS() {
		//todo need fix WoW API addon libs
	}
	
	@FXML
	public void openFile() {
		File selectedDirectory = new DirectoryChooser().showDialog(new Stage());
		if (selectedDirectory != null && selectedDirectory.getAbsolutePath().endsWith(ADDON_PATH)) {
			directoryPath.setText(selectedDirectory.getAbsolutePath());
			downloadJson.setDisable(false);
			uploadJson.setDisable(true);
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
