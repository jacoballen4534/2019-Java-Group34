package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import model.Utilities;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.*;
import java.lang.String;

public class Main extends Application {
    private static Stage stage;
    private static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        Utilities.initializeFiles();

        //Play background music
        //String musicFile = "resources/music/theme.wav"; //Must be a .wav!
        InputStream musicSrc = this.getClass().getResourceAsStream("/music/theme.wav");
        InputStream music = new BufferedInputStream(musicSrc);
        AudioInputStream audioInput = AudioSystem.getAudioInputStream(music);//AudioSystem.getAudioInputStream(new File(musicFile).getAbsoluteFile());
        Clip clip = AudioSystem.getClip();
        clip.open(audioInput);
        clip.loop(Clip.LOOP_CONTINUOUSLY);

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(-5.0f);

        clip.start();

        //Attempts to load a custom font
        Font.loadFont(Main.class.getResourceAsStream("/fonts/beon.otf"), 10);

        stage = primaryStage;
        Parent root = (Parent) new FXMLLoader().load(Main.class.getResourceAsStream("/fxmls/mainMenu.fxml"));
        primaryStage.setTitle("Main Menu");

        //Loads a global stylesheet
//        File styleSheet = new File("resources/css/globalStyle.css");
        String url = Main.class.getResource("/css/globalStyle.css").toExternalForm();

        mainScene = new Scene(root, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT, false);
        mainScene.getStylesheets().add(url);

        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    static Stage getStage() {
        return stage;
    }

    static Scene getMainScene() {return mainScene;}
}
