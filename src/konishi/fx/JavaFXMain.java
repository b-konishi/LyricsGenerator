package konishi.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXMain extends Application {
	
	Stage stage;

    public static void main(String[] args) {
        // JavaFX の実行
        Application.launch(args);
    }

    @Override
    public void start(Stage _stage) throws Exception {
    	stage = _stage;
        // hello.fxml の読み込み
        Parent root = FXMLLoader.load(getClass().getResource("LyricsGenerator.fxml"));

        stage.setResizable(false);
        // Scene の作成・登録
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Lyrics Generator");

        // 表示
        stage.show();
    }
    
    public Stage getStage() {
    	return stage;
    }
}