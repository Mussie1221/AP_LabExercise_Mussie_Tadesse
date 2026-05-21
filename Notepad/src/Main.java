import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application{
    @Override
    public void start(Stage stage){
        TextArea textArea=new TextArea();
        textArea.setPromptText("Start typing ...");
        textArea.setPrefSize(700,500);
        StackPane root = new StackPane();
        root.getChildren().add(textArea);
        Scene scene=new Scene(root,700,500);
        stage.setTitle("Notepad");
        stage.setScene(scene);
        stage.show();
    }
public static void main(String[] args){
        launch();
    }
}