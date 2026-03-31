package com.example.devtracker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class HelloApplication extends Application {

    // --- Apple LIGHT MODE Farbpalette (iOS Style) ---
    private final String BG_COLOR_LIGHT = "#f2f2f7"; // Systemhintergrund hellgrau
    private final String CARD_BG = "#ffffff";        // Weiße Karten für Elemente
    private final String TEXT_PRIMARY = "#000000";   // Schwarz
    private final String TEXT_SECONDARY = "#8e8e93"; // iOS Gray
    private final String APPLE_BLUE = "#007aff";      // iOS Blue
    private final String APPLE_RED = "#ff3b30";       // iOS Red
    private final String FONT_FAMILY = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif";

    // Formatter für die Anzeige in der Liste (z.B. 14. Okt, 14:30)
    private final DateTimeFormatter listDateFormatter = DateTimeFormatter.ofPattern("dd. MMM, HH:mm", Locale.GERMAN);

    private Label titleLabel;
    private ComboBox<String> langBox;
    private ResourceBundle bundle;

    private ListView<Task> taskListView;
    private ComboBox<String> listSelector;
    private Button newListButton;
    private Button addTaskButton;

    private Map<String, ObservableList<Task>> allTaskLists = new HashMap<>();
    private ObservableList<String> listNames = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        listNames.add("Meine Tasks");
        allTaskLists.put("Meine Tasks", FXCollections.observableArrayList());

        loadLanguage(new Locale("de", "DE"));

        // Title styling
        titleLabel = new Label("DevTracker");
        titleLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: 800; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-family: " + FONT_FAMILY + ";");

        // Sprachen Dropdown styling (sauberer)
        langBox = new ComboBox<>();
        langBox.getItems().addAll("🇩🇪 DE", "🇬🇧 EN", "🇵🇱 PL");
        langBox.setValue("🇩🇪 DE");
        langBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 10; -fx-padding: 5; -fx-font-family: " + FONT_FAMILY + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        langBox.setOnAction(e -> switchLanguage(langBox.getValue()));

        // Listen-Auswahl
        listSelector = new ComboBox<>(listNames);
        listSelector.setValue("Meine Tasks");
        listSelector.setStyle("-fx-font-size: 15px; -fx-background-color: " + CARD_BG + "; -fx-background-radius: 10; -fx-padding: 8; -fx-font-family: " + FONT_FAMILY + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        listSelector.setOnAction(e -> updateTaskView());

        newListButton = new Button("Neu");
        newListButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + APPLE_BLUE + "; -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: " + FONT_FAMILY + ";");
        newListButton.setOnAction(e -> createNewList());

        HBox listControlBox = new HBox(15, listSelector, newListButton);
        listControlBox.setAlignment(Pos.CENTER_LEFT);
        listControlBox.setPadding(new Insets(10, 0, 10, 0));

        // Die visuelle Liste (Hintergrund transparent für iOS Look)
        taskListView = new ListView<>();
        taskListView.setItems(allTaskLists.get("Meine Tasks"));
        VBox.setVgrow(taskListView, Priority.ALWAYS);
        taskListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0; -fx-background-insets: 0;");

        // --- DIE NEUE CELL FACTORY (Sauber, Lesbar, Vektor-Icons) ---
        setupModernListCells();

        // Großer Button unten (iOS Style)
        addTaskButton = new Button("+ Neuer Task");
        addTaskButton.setStyle("-fx-background-color: " + APPLE_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px; -fx-padding: 15; -fx-background-radius: 15; -fx-font-family: " + FONT_FAMILY + "; -fx-cursor: hand;");
        addTaskButton.setMaxWidth(Double.MAX_VALUE);
        addTaskButton.setOnAction(e -> openTaskDialog(null));

        updateTexts();

        HBox topBar = new HBox(langBox);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        VBox root = new VBox(10, topBar, titleLabel, listControlBox, taskListView, addTaskButton);
        root.setPadding(new Insets(20, 25, 20, 25)); // Mehr Padding an den Seiten
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: " + BG_COLOR_LIGHT + ";");

        Scene scene = new Scene(root, 480, 700);
        primaryStage.setTitle("DevTracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupModernListCells() {
        taskListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Die Karte für einen Task (Weiß, Schatten, Runde Ecken)
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(15));
                    card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2);");

                    // Linker Bereich: Titel und Info
                    VBox textContainer = new VBox(4);
                    Label title = new Label(task.title);
                    // Schrift ist jetzt SCHWARZ und fett
                    title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 17px; -fx-font-weight: 700; -fx-font-family: " + FONT_FAMILY + ";");

                    // Datums- und Zeit-String bauen
                    String dateTimeStr = "";
                    if (task.dateTime != null) {
                        dateTimeStr = "🗓 " + task.dateTime.format(listDateFormatter);
                    }

                    Label infoLabel = new Label(
                            dateTimeStr +
                                    (task.description != null && !task.description.isEmpty() ? "  •  " + task.description : "")
                    );
                    // Info-Text ist GRAU
                    infoLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-font-family: " + FONT_FAMILY + ";");
                    infoLabel.setWrapText(false); // Verhindert Umbruch für sauberen Look

                    textContainer.getChildren().addAll(title, infoLabel);
                    HBox.setHgrow(textContainer, Priority.ALWAYS); // Nimmt Platz ein, drückt Buttons nach rechts

                    // --- SVG ICONS STATT EMOJIS ---

                    // Bearbeiten Icon (Stift)
                    SVGPath editIcon = new SVGPath();
                    editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                    editIcon.setStyle("-fx-fill: " + APPLE_BLUE + ";"); // Unser iOS Blau

                    Button editBtn = new Button("", editIcon);
                    editBtn.setStyle("-fx-background-color: rgba(0,122,255,0.1); -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 10 6 10;");
                    editBtn.setOnAction(e -> openTaskDialog(task));

                    // Löschen Icon (Mülleimer)
                    SVGPath deleteIcon = new SVGPath();
                    deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                    deleteIcon.setStyle("-fx-fill: " + APPLE_RED + ";"); // Unser iOS Rot

                    Button deleteBtn = new Button("", deleteIcon);
                    deleteBtn.setStyle("-fx-background-color: rgba(255,59,48,0.1); -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 10 6 10;");
                    deleteBtn.setOnAction(e -> allTaskLists.get(listSelector.getValue()).remove(task));

                    card.getChildren().addAll(textContainer, editBtn, deleteBtn);

                    setGraphic(card);
                    setText(null);
                    // Abstand zwischen den Karten
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 12 0;");
                }
            }
        });
    }

    private void openTaskDialog(Task existingTask) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingTask == null ? "Neuen Task erstellen" : "Task bearbeiten");

        // Styling des DialogPanes
        dialog.getDialogPane().setStyle("-fx-font-family: " + FONT_FAMILY + ";");

        ButtonType saveType = new ButtonType("Sichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField titleInput = new TextField();
        titleInput.setPromptText("Titel");
        titleInput.setStyle("-fx-background-radius: 8; -fx-padding: 8;");

        // --- DATUM UND UHRZEIT IM DIALOG ---
        DatePicker dateInput = new DatePicker();
        dateInput.setPromptText("Datum wählen");
        dateInput.setStyle("-fx-background-radius: 8;");

        // Spinner für Stunden (0-23) und Minuten (0-59)
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        hourSpinner.setPrefWidth(70);
        hourSpinner.setEditable(true);
        hourSpinner.setStyle("-fx-background-radius: 8;");

        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
        minuteSpinner.setPrefWidth(70);
        minuteSpinner.setEditable(true);
        minuteSpinner.setStyle("-fx-background-radius: 8;");

        HBox timeInputBox = new HBox(5, new Label("um"), hourSpinner, new Label(":"), minuteSpinner);
        timeInputBox.setAlignment(Pos.CENTER_LEFT);

        TextArea descInput = new TextArea();
        descInput.setPromptText("Notizen...");
        descInput.setPrefRowCount(3);
        descInput.setStyle("-fx-background-radius: 8; -fx-padding: 8;");

        ComboBox<String> listChoice = new ComboBox<>(listNames);
        listChoice.setValue(listSelector.getValue());
        listChoice.setStyle("-fx-background-radius: 8;");

        // Wenn wir BEARBEITEN, füllen wir die Felder
        if (existingTask != null && existingTask.dateTime != null) {
            titleInput.setText(existingTask.title);
            dateInput.setValue(existingTask.dateTime.toLocalDate());
            hourSpinner.getValueFactory().setValue(existingTask.dateTime.getHour());
            minuteSpinner.getValueFactory().setValue(existingTask.dateTime.getMinute());
            descInput.setText(existingTask.description);
            listChoice.setDisable(true);
        }

        grid.add(new Label("Was?"), 0, 0); grid.add(titleInput, 1, 0);

        VBox dateTimeBox = new VBox(8, dateInput, timeInputBox);
        grid.add(new Label("Wann?"), 0, 1); grid.add(dateTimeBox, 1, 1);

        grid.add(new Label("Notizen:"), 0, 2); grid.add(descInput, 1, 2);

        if (existingTask == null) {
            grid.add(new Label("Liste:"), 0, 3); grid.add(listChoice, 1, 3);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveType) {
                if (titleInput.getText().trim().isEmpty()) return;

                // Datum und Zeit kombinieren
                LocalDateTime selectedDateTime = null;
                if (dateInput.getValue() != null) {
                    selectedDateTime = LocalDateTime.of(dateInput.getValue(), LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
                }

                if (existingTask == null) {
                    // NEUER TASK
                    Task newTask = new Task(titleInput.getText().trim(), descInput.getText(), selectedDateTime);
                    allTaskLists.get(listChoice.getValue()).add(newTask);
                    listSelector.setValue(listChoice.getValue());
                } else {
                    // BEARBEITEN
                    existingTask.title = titleInput.getText().trim();
                    existingTask.dateTime = selectedDateTime;
                    existingTask.description = descInput.getText();
                    taskListView.refresh();
                }
            }
        });
    }

    private void createNewList() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Neue Liste");
        dialog.setHeaderText("Name der neuen Liste:");
        // Dialog styling
        dialog.getDialogPane().setStyle("-fx-font-family: " + FONT_FAMILY + ";");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty() && !allTaskLists.containsKey(name)) {
                allTaskLists.put(name, FXCollections.observableArrayList());
                listNames.add(name);
                listSelector.setValue(name);
            }
        });
    }

    private void updateTaskView() {
        String selected = listSelector.getValue();
        if (selected != null && allTaskLists.containsKey(selected)) {
            taskListView.setItems(allTaskLists.get(selected));
        }
    }

    private void loadLanguage(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle("messages", locale);
        } catch (Exception e) {
            bundle = null;
        }
    }

    private void switchLanguage(String selection) {
        if (selection.contains("EN")) {
            loadLanguage(new Locale("en", "US"));
        } else if (selection.contains("PL")) {
            loadLanguage(new Locale("pl", "PL"));
        } else {
            loadLanguage(new Locale("de", "DE"));
        }
        updateTexts();
    }

    private void updateTexts() {
        if (bundle != null) {
            titleLabel.setText(bundle.getString("title"));
            addTaskButton.setText(bundle.getString("btn.add"));
        } else {
            // Fallback (damit man überhaupt was liest, falls keine Datei da ist)
            titleLabel.setText("DevTracker");
            addTaskButton.setText("+ Neuer Task");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ==========================================
    // UPDATED TASK KLASSE (Mit LocalDateTime)
    // ==========================================
    public static class Task {
        public String title;
        public String description;
        public LocalDateTime dateTime; // Jetzt Datum UND Uhrzeit

        public Task(String title, String description, LocalDateTime dateTime) {
            this.title = title;
            this.description = description;
            this.dateTime = dateTime;
        }
    }
}