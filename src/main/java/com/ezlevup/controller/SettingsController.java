package com.ezlevup.controller;

import com.ezlevup.util.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private TextField apiUrlField;

    @FXML
    private Button saveButton;

    @FXML
    private Button resetButton;

    public void initialize() {
        String currentApiUrl = Settings.getApiUrl();
        apiUrlField.setText(currentApiUrl);
    }

    @FXML
    private void handleSave() {
        String apiUrl = apiUrlField.getText().trim();

        if (apiUrl.isEmpty()) {
            showAlert("오류", "API URL을 입력해주세요.");
            return;
        }

        if (!isValidUrl(apiUrl)) {
            showAlert("오류", "올바른 URL 형식이 아닙니다.\n예: http://localhost:8580");
            return;
        }

        try {
            Settings.setApiUrl(apiUrl);
            showAlert("성공", "API URL이 저장되었습니다.\n" + apiUrl);
        } catch (Exception e) {
            showAlert("오류", "설정 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        String defaultUrl = Settings.getDefaultApiUrl();
        apiUrlField.setText(defaultUrl);
        showAlert("정보", "기본값으로 초기화되었습니다.\n저장하려면 '저장' 버튼을 클릭하세요.");
    }

    private boolean isValidUrl(String url) {
        try {
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}