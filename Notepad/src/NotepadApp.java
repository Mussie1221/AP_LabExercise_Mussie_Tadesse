import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

import java.io.*;

public class NotepadApp extends Application {

    private File currentFile = null;
    private double fontsize = 14;
    private boolean isDirty = false;
    private String savedText = "";

    private TabPane tabPane = new TabPane();

    @Override
    public void start(Stage stage) {

        TextArea textArea = createNewTab("untitled", null);

        Label statusLabel = new Label("Words: 0");
        FileChooser fileChooser = new FileChooser();

        applyFontSize(textArea);

        Button newTabButton = new Button("+");

        // ===== Dirty tracker =====
        textArea.textProperty().addListener((obs, oldText, newText) -> {

            isDirty = !newText.equals(savedText);

            if (newText.trim().isEmpty()) {
                statusLabel.setText("Words: 0");
            } else {
                statusLabel.setText("Words: " + newText.trim().split("\\s+").length);
            }
        });

        newTabButton.setOnAction(e -> createNewTab("untitled", null));

        // ================= MENU =================
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem exitItem = new MenuItem("Exit");

        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, exitItem);

        // ===== EDIT (UNCHANGED) =====
        Menu editMenu = new Menu("Edit");
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        editMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        cutItem.setOnAction(e -> getActiveTextArea().cut());
        copyItem.setOnAction(e -> getActiveTextArea().copy());
        pasteItem.setOnAction(e -> getActiveTextArea().paste());

        cutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        copyItem.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        pasteItem.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));

        // ===== VIEW (UNCHANGED) =====
        Menu viewMenu = new Menu("View");

        MenuItem zoomInItem = new MenuItem("Zoom In");
        MenuItem zoomOutItem = new MenuItem("Zoom Out");

        viewMenu.getItems().addAll(zoomInItem, zoomOutItem);

        zoomInItem.setOnAction(e -> {
            fontsize += 2;
            applyFontSize(getActiveTextArea());
        });

        zoomOutItem.setOnAction(e -> {
            if (fontsize > 2) {
                fontsize -= 2;
                applyFontSize(getActiveTextArea());
            }
        });

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);

        // ================= ROOT =================
        BorderPane root = new BorderPane();

        HBox top = new HBox(menuBar, newTabButton);

        root.setTop(top);
        root.setCenter(tabPane);
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("Notepad");
        stage.setScene(scene);
        stage.show();

        // ================= FILE ACTIONS =================
        newItem.setOnAction(e -> {
            if (confirmIfDirty(stage)) {
                createNewTab("untitled", null);
                currentFile = null;
                isDirty = false;
                savedText = "";
            }
        });

        openItem.setOnAction(e -> openFile(stage, fileChooser));

        saveItem.setOnAction(e -> saveFile(stage, fileChooser));

        saveAsItem.setOnAction(e -> {
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                currentFile = file;
                writeFile(file, getActiveTextArea(), getActiveTab());
                isDirty = false;
                savedText = getActiveTextArea().getText();
            }
        });

        exitItem.setOnAction(e -> {
            if (confirmIfDirty(stage)) stage.close();
        });
    }

    // ================= FIXED TAB CREATION =================
    private TextArea createNewTab(String title, String content) {

        TextArea textArea = new TextArea();

        if (content != null) {
            textArea.setText(content);
        }

        Tab tab = new Tab(title);
        tab.setContent(textArea);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                currentFile = null;
            }
        });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        return textArea;
    }

    // ================= FIXED SAVE =================
    private void saveFile(Stage stage, FileChooser fileChooser) {

        File file = currentFile;

        if (file == null) {
            file = fileChooser.showSaveDialog(stage);
            currentFile = file;
        }

        if (file != null) {
            writeFile(file, getActiveTextArea(), getActiveTab());
            savedText = getActiveTextArea().getText();
            isDirty = false;
        }
    }

    // ================= WRITE =================
    private void writeFile(File file, TextArea textArea, Tab tab) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(textArea.getText());

            // 🔥 FIX: update tab title correctly
            tab.setText(file.getName());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ================= OPEN =================
    private void openFile(Stage stage, FileChooser fileChooser) {

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        TextArea textArea = createNewTab(file.getName(), content.toString());

        currentFile = file;
        savedText = textArea.getText();
        isDirty = false;
    }

    // ================= HELPERS =================
    private TextArea getActiveTextArea() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        return (TextArea) tab.getContent();
    }

    private Tab getActiveTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    private boolean confirmIfDirty(Stage stage) {
        if (!isDirty) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to continue without saving?");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void applyFontSize(TextArea textArea) {
        textArea.setStyle("-fx-font-size: " + fontsize + "px;");
    }

    public static void main(String[] args) {
        launch();
    }
}