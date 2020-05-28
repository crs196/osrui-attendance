package ozeret;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class SignInPane extends GridPane {

	private Stage stage;
	private Scene nextScene;
	
	private String ozeretName;
	private LocalDateTime curfew;
	private File attendanceFile;
	
	// used to track which column holds each piece of information
	private int bunkCol, nameCol, idCol, ontimeCol, lateCol, absentCol;
	
	public SignInPane(Stage s, Scene ns) {
		super();
		stage = s;
		nextScene = ns;
	}
	
	// called when InitialPane moves to this scene
	public void setPrevVars(String ozName, LocalDateTime c, File af) {
		ozeretName = ozName;
		curfew = c;
		attendanceFile = af;
		
		try {
			readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setup();
	}
	
	// reads in information from selected attendance file and stores it locally
	private void readFile() throws IOException {

		// create local workbook from attendanceFile
		FileInputStream afis = new FileInputStream(attendanceFile);
		XSSFWorkbook workbook = new XSSFWorkbook(afis);
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		// read header row and assign column trackers
		XSSFRow headerRow = sheet.getRow(0);
		boolean idColExists = false;
		for (int i = headerRow.getFirstCellNum(); i < headerRow.getLastCellNum(); i++) {
			
			if (headerRow.getCell(i).getCellType() == CellType.STRING) {
				
				switch (headerRow.getCell(i).getStringCellValue().toLowerCase()) {
				case "bunk":
					bunkCol = i;
					break;
				case "name":
					nameCol = i;
					break;
				case "id":
					idCol = i;
					idColExists = true;
					break;
				case "on time":
					ontimeCol = i;
					break;
				case "late":
					lateCol = i;
					break;
				case "absent":
					absentCol = i;
					break;
				default:
					break;
				}
			}
		}
		// if there is no column labeled "ID", set the ID column to be the name column
		if (!idColExists)
			idCol = nameCol;
		
		// TODO: from here to the next TODO comment is a debug section to print out the read-in spreadsheet.
		//  remove when done debugging
		Iterator<Row> rowIterator = sheet.iterator();
		while(rowIterator.hasNext()) {

			Iterator<Cell> cellIterator = ((XSSFRow) rowIterator.next()).iterator();

			while(cellIterator.hasNext()) {
				XSSFCell c = (XSSFCell) cellIterator.next();

				switch (c.getCellType()) {
				case NUMERIC:
					System.out.print(c.getNumericCellValue() + "\t\t");
					break;
				case STRING:
					System.out.print(c.getStringCellValue() + "\t\t");
					break;
				default:
					System.out.println("??\t\t");
				}
			}

			System.out.println();
		}
		// TODO: end debug section


		// write taken attendance back to 
		afis.close();
		FileOutputStream afos = new FileOutputStream(attendanceFile);
		workbook.write(afos);
		workbook.close();
	}

	// sets up layout and functionality of SignInPane
	private void setup() {

		// set up grid layout and sizing
		this.setAlignment(Pos.CENTER);
		this.setHgap(15);
		this.setVgap(20);
		this.setPadding(new Insets(30));
		
		// header
		Label title = new Label("Sign-In");
		title.setId("header");
		SignInPane.setHalignment(title, HPos.CENTER);
		this.add(title, 0, 0, 3, 1);
		
		
		/* left column (clock + time to curfew, view unaccounted for, mark shmira/day off) */
		
		// clock
		Clock currentTime = new Clock();
		Label clockLabel = new Label("Current Time:");
		HBox currentTimeBox = new HBox(this.getHgap());
		currentTime.setMinWidth(USE_PREF_SIZE);
		clockLabel.setMinWidth(USE_PREF_SIZE);
		currentTimeBox.setAlignment(Pos.CENTER);
		currentTimeBox.getChildren().addAll(clockLabel, currentTime);
		
		// curfew time
		Label curfewLabel = new Label("Curfew:");
		Label curfewTimeLabel = new Label();
		curfewTimeLabel.setText(curfew.format(DateTimeFormatter.ofPattern("h:mm a")));
		HBox curfewBox = new HBox(this.getHgap());
		curfewLabel.setMinWidth(USE_PREF_SIZE);
		curfewTimeLabel.setMinWidth(USE_PREF_SIZE);
		curfewBox.setAlignment(Pos.CENTER);
		curfewBox.getChildren().addAll(curfewLabel, curfewTimeLabel);
		
		// time to curfew
		CountdownTimer timeToCurfew = new CountdownTimer(curfew);
		Label countdownLabel = new Label("Time until curfew:");
		HBox countdownBox = new HBox(this.getHgap());
		timeToCurfew.setMinWidth(USE_PREF_SIZE);
		countdownLabel.setMinWidth(USE_PREF_SIZE);
		countdownBox.setAlignment(Pos.CENTER);
		countdownBox.getChildren().addAll(countdownLabel, timeToCurfew);
		
		// add clocks to VBox to hold them
		VBox clockBox = new VBox(this.getVgap() * 0.5);
		clockBox.getChildren().addAll(currentTimeBox, curfewBox, countdownBox);
		
		VBox listButtons = new VBox(this.getVgap() * 0.75);
		Button viewUnaccounted = new Button("View Unaccounted-for Staff Members");
		Button markShmira = new Button("Mark Staff on Shmira");
		Button markDayOff = new Button("Mark Staff on Day Off");
		
		viewUnaccounted.setMinWidth(USE_PREF_SIZE);
		markShmira.setMinWidth(USE_PREF_SIZE);
		markDayOff.setMinWidth(USE_PREF_SIZE);
		
		HBox.setHgrow(viewUnaccounted, Priority.ALWAYS);
		HBox.setHgrow(markShmira, Priority.ALWAYS);
		HBox.setHgrow(markDayOff, Priority.ALWAYS);
		
		viewUnaccounted.setMaxWidth(Double.MAX_VALUE);
		markShmira.setMaxWidth(Double.MAX_VALUE);
		markDayOff.setMaxWidth(Double.MAX_VALUE);
		
		HBox vu = new HBox();
		vu.getChildren().add(viewUnaccounted);
		HBox ms = new HBox();
		ms.getChildren().add(markShmira);
		HBox md = new HBox();
		md.getChildren().add(markDayOff);
		
		listButtons.getChildren().addAll(vu, ms, md);
		
		VBox leftColumn = new VBox(this.getVgap());
		leftColumn.getChildren().addAll(clockBox, listButtons);
		this.add(leftColumn, 0, 1);
		
		
		/* right column (sign-in box, confirmation area) */
		
		// sign-in instructions and entry point
		Label scanLabel = new Label("Please enter a staff member's ID");
		scanLabel.setMinWidth(USE_PREF_SIZE);
		TextField idField = new TextField();
		VBox idBox = new VBox(this.getVgap());
		idBox.getChildren().addAll(scanLabel, idField);
		
		// confirmation area
		TextArea confirmation = new TextArea();
		confirmation.setEditable(false);
		confirmation.setWrapText(true);
		confirmation.setPrefWidth(scanLabel.getWidth());
		confirmation.setPrefRowCount(3);
		idBox.getChildren().add(confirmation);
		this.add(idBox, 1, 1);
		
		// confirm button
		Button signIn = new Button("Sign In");
		signIn.setDefaultButton(true);
		HBox.setHgrow(signIn, Priority.ALWAYS);
		signIn.setMaxWidth(Double.MAX_VALUE);
		HBox signInButton = new HBox(this.getHgap());
		signInButton.getChildren().add(signIn);
		this.add(signInButton, 1, 2);
		
		// set sign-in button behavior
		signIn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO: sign in staff member who's ID is in idField
				confirmation.setText("Confirmation tk");
				
				// after signing staff member in, clear idField for next entry
				idField.setText("");
			}
		});
		
		
		
		// event handlers for right column buttons
		
		// stages for right column buttons
		Stage extraStage = new Stage();
		
		// pulls up list of staff members who have yet to sign in in this session
		viewUnaccounted.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
					
				GridPane unaccPane = new GridPane();
				// set up grid layout and sizing
				unaccPane.setAlignment(Pos.CENTER);
				unaccPane.setHgap(15);
				unaccPane.setVgap(20);
				unaccPane.setPadding(new Insets(30));

				Label temp = new Label("Unaccounted-for staff list tk");
				unaccPane.add(temp, 0, 0);

				Scene unaccScene = new Scene(unaccPane);
				unaccScene.getStylesheets().add(OzeretMain.class.getResource("ozeret.css").toExternalForm());

				extraStage.setScene(unaccScene);
				extraStage.setTitle("Unaccounted-for staff");
				extraStage.getIcons().add(new Image("file:resources/images/stage_icon.png"));
				extraStage.centerOnScreen();
				extraStage.show();
			}
		});
		
		// pulls up list of staff members who have yet to sign in in this session
		//  with option to mark them as on shmira
		markShmira.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
						
				GridPane msPane = new GridPane();
				// set up grid layout and sizing
				msPane.setAlignment(Pos.CENTER);
				msPane.setHgap(15);
				msPane.setVgap(20);
				msPane.setPadding(new Insets(30));
					
				Label temp = new Label("Mark-Shmira staff list tk");
				msPane.add(temp, 0, 0);
						
				Scene msScene = new Scene(msPane);
				msScene.getStylesheets().add(OzeretMain.class.getResource("ozeret.css").toExternalForm());
						
				extraStage.setScene(msScene);
				extraStage.setTitle("Mark Shmira");
				extraStage.getIcons().add(new Image("file:resources/images/stage_icon.png"));
				extraStage.centerOnScreen();
				extraStage.show();
			}
		});
		
		// pulls up list of staff members who have yet to sign in in this session
		//  with option to mark them as on day off
		markDayOff.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
					
				GridPane doPane = new GridPane();
				// set up grid layout and sizing
				doPane.setAlignment(Pos.CENTER);
				doPane.setHgap(15);
				doPane.setVgap(20);
				doPane.setPadding(new Insets(30));
						
				Label temp = new Label("Mark-Day-Off staff list tk");
				doPane.add(temp, 0, 0);
						
				Scene doScene = new Scene(doPane);
				doScene.getStylesheets().add(OzeretMain.class.getResource("ozeret.css").toExternalForm());
						
				extraStage.setScene(doScene);
				extraStage.setTitle("Mark Day Off");
				extraStage.getIcons().add(new Image("file:resources/images/stage_icon.png"));
				extraStage.centerOnScreen();
				extraStage.show();
			}
		});
		
		
		/* change stage close behavior */
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				event.consume(); // consume window-close event
				
				Alert alert = new Alert(AlertType.CONFIRMATION, 
						"Are you sure you want to exit?\nIf you exit now, before saving, all attendance data taken in this session will be lost",
						new ButtonType("No, Return to Sign-In", ButtonData.CANCEL_CLOSE),
						new ButtonType("Yes, Exit", ButtonData.OK_DONE));
				alert.setTitle("Exit Confirmation");
				alert.getDialogPane().getStylesheets().add(getClass().getResource("ozeret.css").toExternalForm());
				alert.showAndWait();
				
				if (alert.getResult().getButtonData() == ButtonData.OK_DONE)
					stage.close();
				
			}
		});
	}
	
}

class Clock extends Label {
	
	public Clock() {
		bindToTime();
	}
	
	private void bindToTime() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				setText(LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
			}
		}), new KeyFrame(Duration.seconds(1)));
		
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}
}

class CountdownTimer extends Label {
	
	LocalDateTime finalTime;
	
	public CountdownTimer(LocalDateTime timeToCountTo) {
		finalTime = timeToCountTo;
		bindToTime();
	}
	
	private void bindToTime() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (LocalDateTime.now().until(finalTime, ChronoUnit.MINUTES) == 1) 
					setText(LocalDateTime.now().until(finalTime, ChronoUnit.MINUTES) + " minute");
				else
					setText(LocalDateTime.now().until(finalTime, ChronoUnit.MINUTES) + " minutes");
			}
		}), new KeyFrame(Duration.seconds(1)));
		
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}
}
