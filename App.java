package ke.sonclinic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        Database.initialize();
        showLogin();
        stage.setTitle("SON Clinic & Pharmacy Management System");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.show();
    }

    private void showLogin() {
        Scene scene = Ui.loginScene(this::loginAttempt);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void loginAttempt(String username, String password) {
        if (AuthService.authenticate(username, password)) {
            stage.setScene(Ui.mainScene(username, this::logout));
            stage.setMaximized(true);
        } else {
            Ui.error("Login failed", "Incorrect username or password.");
        }
    }

    private void logout() {
        stage.setMaximized(false);
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
