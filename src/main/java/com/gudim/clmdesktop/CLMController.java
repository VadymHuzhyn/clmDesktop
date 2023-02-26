package com.gudim.clmdesktop;

import com.gudim.clmdesktop.dto.ItemList;
import com.gudim.clmdesktop.dto.Wishlist;
import com.gudim.clmdesktop.service.CLMService;
import com.gudim.clmdesktop.util.CLMConstant;
import com.gudim.clmdesktop.util.CLMUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@FxmlView("CLM.fxml")
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

    public CLMController(CLMService clmService) {
        this.clmService = clmService;
    }

    @FXML
    public void convertXLSToJSON() {
        String statusCode = clmService.downloadXLSXFromDrive();
        if ("200".equals(statusCode)) {
            List<Wishlist> wishlists = clmService.getWishlist();
            Map<String, List<ItemList>> itemList = clmService.getItemList();
            StringBuilder wishlistTable = clmService.generateWishlistTable(wishlists);
            StringBuilder itemListTable = clmService.generateItemListTable(itemList);
            CLMUtil.saveLuaTableFile(wishlistTable, directoryPath.getText() + CLMConstant.PATH_CLM_WISHLISTS_LUA);
            CLMUtil.saveLuaTableFile(itemListTable, directoryPath.getText() + CLMConstant.PATH_CLM_ITEMS_LUA);
            CLMUtil.removeFile(CLMConstant.TEMP_FILE_NAME);
            showPopup(AlertType.INFORMATION, "Data was generated", "");
        } else {
            showPopup(AlertType.ERROR, "File not found", "Please check the correct account and if you have access to the file. if not, contact the owner of the file and request access.");
        }

    }

    @FXML
    public void convertJSONTonXLS() {
        /* need fix WoW API addon libs */
    }

    @FXML
    public void openFile() {
        File selectedDirectory = new DirectoryChooser().showDialog(new Stage());
        if (selectedDirectory != null && selectedDirectory.getAbsolutePath().endsWith(CLMConstant.ADDON_PATH)) {
            directoryPath.setText(selectedDirectory.getAbsolutePath());
            downloadJson.setDisable(false);
            uploadJson.setDisable(true);
            directoryChooser.setDisable(true);
        } else {
            showPopup(AlertType.WARNING, CLMConstant.INCORRECT_DIRECTORY_HEADER, CLMConstant.INCORRECT_DIRECTORY_MESSAGE);
        }
    }

    public void showPopup(AlertType warning, String header, String contentText) {
        Alert alert = new Alert(warning);
        alert.setHeaderText(header);
        alert.setContentText(contentText);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(this.getClass().getResource("/static/css/main.css"))
                        .toExternalForm());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(
                        this.getClass().getResourceAsStream("/static/image/clmLogo.png"))));
        alert.showAndWait();
    }

}
