package konishi.fx.controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class FxControl {
	
	private static final int TIME_MAX = 300;
	private static final int SIZE_MAX = 30;

	@FXML public Slider time_slider;

	@FXML public Slider size_slider;
	@FXML public Label size_label;
		
	@FXML public Label current_time_label;
	@FXML public TextArea lyrics_field;
	@FXML public Button play_button;
	@FXML public TextField file_name;
	@FXML public TextField dir_name;
	@FXML public Button color_inverse_button;


	private FileChooser openFileChooser;
	private DirectoryChooser directoryChooser;
	
	private int currentLine = 0;
	
	private double scrollValue;
	private static double SPACER = 100;
	
	private boolean active;
	private Timeline timeline;
	private final StringProperty timeSeconds = new SimpleStringProperty();
    private Duration time = Duration.ZERO;
    
    /**
     * 初期設定
     */
	public void initialize() {
		time_slider.setMin(0);
		time_slider.setMax(TIME_MAX);
		
		size_slider.setMin(10);
		size_slider.setMax(SIZE_MAX);
		
		dir_name.setText(new File(".").getAbsoluteFile().getParent()+System.getProperty("file.separator"));
		
		current_time_label.textProperty().bind(timeSeconds);
		timeReset();
		
		SPACER = lyrics_field.getFont().getSize()*8;
		System.out.println(SPACER);
		
		size_slider.setValue((int)lyrics_field.getFont().getSize());
		size_label.setText("Size: "+(int)size_slider.getValue());
		
	}
	
	/**
	 * 時間追加時の自動スクロール調整
	 */
	public void scroll() {
		lyrics_field.textProperty().addListener(new ChangeListener<Object>() {
		    @Override
		    public void changed(ObservableValue<?> observable, Object oldValue,
		            Object newValue) {
		    	if (lyrics_field.getFont().getSize()*currentLine - lyrics_field.getHeight()/2+SPACER > 0) {
		    		scrollValue = lyrics_field.getFont().getSize()*currentLine - lyrics_field.getHeight()/2+SPACER;
		    	} else {
		    		scrollValue = 0;
		    	}
		        lyrics_field.setScrollTop(scrollValue);
		    }
		});
	}
	
	/**
	 * Start/Stopボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handlePlayButton(MouseEvent event) throws Exception  {
		if (active) {
			play_button.setText("RESTART");
            timeline.stop();
            active = false;
            timeSeconds.set(makeText(time));
            return;
        }
		play_button.setText("STOP");
		active = true;
        if (timeline == null) {
			timeline = new Timeline(
	                new KeyFrame(Duration.millis(100),
	                e2 -> {
	                    final Duration duration = ((KeyFrame) e2.getSource()).getTime();
	                    time = time.add(duration);
	                    timeSeconds.set(makeText(time));
	                    time_slider.setValue(time.toSeconds());
	                    if (time.toSeconds() > TIME_MAX)
	                    	timeReset();
	                }
	            )
	        );
        }
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}
	
	/**
	 * 時間表示テキストの生成
	 * @param duration
	 * @return
	 */
	private String makeText(final Duration duration) {
        return String.format("%02d:%02d",
                (long) (duration.toMinutes() % 60.0),
                (long) (duration.toSeconds() % 60.0)
                );
//        		+ (active ? " ▶" : " ■");
    }
	
	/**
	 * 時間のリセット
	 */
	public void timeReset() {
        time = Duration.ZERO;
        timeSeconds.set(makeText(time));
        play_button.setText("START");
    }
	
	/**
	 * 時間リセットボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleResetButton(MouseEvent event) throws Exception  {
		timeReset();
	}
	
	/**
	 * 時間スライダー
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleSlider(MouseEvent event) throws Exception  {
		System.out.println(time_slider.getValue());
		time = new Duration(time_slider.getValue()*1000);
		timeSeconds.set(makeText(time));
	}
	
	/**
	 * 時間追加ボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleAddButton(MouseEvent event) throws Exception  {		
		scroll();
		
		
		String[] lyrics = lyrics_field.getText().split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lyrics.length; i++) {
			if (currentLine == i) {
				sb.append("["+makeText(time)+":00]");
			}			
			sb.append(lyrics[i]);
			lyrics[i] = sb.toString();
			sb.setLength(0);
		}
		lyrics_field.clear();
		lyrics_field.appendText(String.join("\n", lyrics));
		if (currentLine < lyrics.length)
			currentLine++;

	}
	
	/**
	 * 時間削除ボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleDeleteButton(MouseEvent event) throws Exception  {
		scroll();
		
		String[] lyrics = lyrics_field.getText().split("\n");
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < lyrics.length; i++) {
			sb.append(lyrics[i]);
			if (i == currentLine-1) {
				sb.replace(0, 10, "");
			}
			lyrics[i] = sb.toString();
			sb.setLength(0);
		}
		lyrics_field.clear();
		lyrics_field.appendText(String.join("\n", lyrics));
		if (currentLine != 0)
			currentLine--;

	}
	
	/**
	 * 間奏追加ボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleInterludeButton(MouseEvent event) throws Exception  {
		lyrics_field.appendText("----------♪----------\n");
	}
	
	/**
	 * 歌詞の時間の管理
	 */
	public void setCurrentLine() {
		String[] lyrics2 = lyrics_field.getText().split("\n");
		String regex = "\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\]";
		Pattern p = Pattern.compile(regex);
		for (int i = 0; i < lyrics2.length; i++) {
			Matcher m = p.matcher(lyrics2[i]);
			if (m.find()) {
				currentLine = i+1;
			}
		}
	}
	
	/**
	 * ファイルオープンボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleOpenFileButton(MouseEvent event) throws Exception  {
		openFileChooser = new FileChooser();
		openFileChooser.setTitle("Open Lyrics File");
		openFileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Walkman Lyrics Files", "*.lrc")
				);
		openFileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("All Files", "*.*")
				);
		
		File openFile = openFileChooser.showOpenDialog(null);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(openFile), detectFileEncoding(openFile)));
		
		String lyrics = "";
		StringBuilder sb = new StringBuilder();
		while((lyrics = br.readLine()) != null) {
			sb.append(lyrics+"\n");
		}
		lyrics = sb.toString();
		lyrics_field.setText(lyrics);
		file_name.setText(openFile.getName());
		
		br.close();
		
		setCurrentLine();
	}
	
	/**
	 * セーブするディレクトリの選択ボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleSaveDirectoryButton(MouseEvent event) throws Exception  {
		directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select Save Directory");
		
		File saveDirectory = directoryChooser.showDialog(null);
		dir_name.setText(saveDirectory.getPath()+System.getProperty("file.separator"));
	}
	
	/**
	 * 歌詞セーブボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleSaveButton(MouseEvent event) throws Exception  {
		FileWriter fw = new FileWriter(dir_name.getText() + file_name.getText());
		fw.write(lyrics_field.getText());
		fw.close();
	}

	/**
	 * 歌詞の時間クリアボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleLyricsTimeClearButton(MouseEvent event) throws Exception  {
		String[] lyrics = lyrics_field.getText().split("\n");
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < lyrics.length; i++) {
			sb.append(lyrics[i]);
			if (i < currentLine) {
				sb.replace(0, 10, "");
			}
			lyrics[i] = sb.toString();
			sb.setLength(0);
		}
		lyrics_field.clear();
		lyrics_field.appendText(String.join("\n", lyrics));
		lyrics_field.positionCaret(0);
		
		currentLine = 0;
	}
	
	/**
	 * 歌詞クリアボタン
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleAllClearButton(MouseEvent event) throws Exception  {
		lyrics_field.clear();
		currentLine = 0;
	}
	
	/**
	 * ディレクトリ表示テキストフィールド
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleDirectoryName(MouseEvent event) throws Exception  {
		dir_name.end();
	}
	
	/**
	 * 文字サイズ変更スライダー
	 * @param event
	 * @throws Exception
	 */
	@FXML public void handleSizeSlider(MouseEvent event) throws Exception  {
		lyrics_field.setFont(Font.font(size_slider.getValue()));
		size_label.setText("Size: "+(int)size_slider.getValue());
	}
	
	/**
	 * ファイルのエンコーディングを調べる
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String detectFileEncoding(File file) throws IOException  {
	    String result = null;
	    byte[] buf = new byte[4096];
	    FileInputStream fis = new FileInputStream(file);
	    UniversalDetector detector = new UniversalDetector(null);
	    
	    int nread;
	    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	        detector.handleData(buf, 0, nread);
	    }
	    detector.dataEnd();
	    
	    result =  detector.getDetectedCharset();
	    detector.reset();
	    fis.close();
	    
	    return result;
	}
	
}
