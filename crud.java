package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class crud extends Application {

    
    private static final String DB_URL = "";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = ""; 

 
    private TextField txtName = new TextField();
    private TextField txtAge = new TextField();
    private TextField txtNumber = new TextField();
    private TextField txtEmail = new TextField();

    private TableView<User> tableView = new TableView<>();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

      
        HBox inputFields = new HBox(10);
        inputFields.setAlignment(Pos.CENTER);
        txtName.setPromptText("Name");
        txtAge.setPromptText("Age");
        txtNumber.setPromptText("Number");
        txtEmail.setPromptText("Email");
        inputFields.getChildren().addAll(txtName, txtAge, txtNumber, txtEmail);

     
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        Button btnInsert = new Button("Insert");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");
        Button btnClear = new Button("Clear");
        buttons.getChildren().addAll(btnInsert, btnUpdate, btnDelete, btnClear);

        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, Integer> colAge = new TableColumn<>("Age");
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<User, String> colNumber = new TableColumn<>("Number");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.getColumns().addAll(colId, colName, colAge, colNumber, colEmail);
        tableView.setItems(userList);
        tableView.setPrefHeight(300);

     
        root.getChildren().addAll(inputFields, buttons, tableView);

       
        btnInsert.setOnAction(e -> insertUser());
        btnUpdate.setOnAction(e -> updateUser());
        btnDelete.setOnAction(e -> deleteUser());
        btnClear.setOnAction(e -> clearFields());

        
        loadUsers();

    
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX CRUD Application");
        primaryStage.show();
    }

    
    private void loadUsers() {
        userList.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("number"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
    private void insertUser() {
        String name = txtName.getText();
        String ageText = txtAge.getText();
        String number = txtNumber.getText();
        String email = txtEmail.getText();

        if (name.isEmpty() || ageText.isEmpty() || number.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, age, number, email) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, name);
                stmt.setInt(2, age);
                stmt.setString(3, number);
                stmt.setString(4, email);
                stmt.executeUpdate();
                clearFields();
                loadUsers();
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Age must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
    private void updateUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "No user selected.");
            return;
        }

        String name = txtName.getText();
        String ageText = txtAge.getText();
        String number = txtNumber.getText();
        String email = txtEmail.getText();

        if (name.isEmpty() || ageText.isEmpty() || number.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE users SET name = ?, age = ?, number = ?, email = ? WHERE id = ?")) {
                stmt.setString(1, name);
                stmt.setInt(2, age);
                stmt.setString(3, number);
                stmt.setString(4, email);
                stmt.setInt(5, selectedUser.getId());
                stmt.executeUpdate();
                clearFields();
                loadUsers();
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Age must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private void deleteUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "No user selected.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, selectedUser.getId());
            stmt.executeUpdate();
            clearFields();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
    private void clearFields() {
        txtName.clear();
        txtAge.clear();
        txtNumber.clear();
        txtEmail.clear();
    }

 
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    public static class User {
        private int id;
        private String name;
        private int age;
        private String number;
        private String email;

        public User(int id, String name, int age, String number, String email) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.number = number;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getNumber() {
            return number;
        }

        public String getEmail() {
            return email;
        }
    }
}
