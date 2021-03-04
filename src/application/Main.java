package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;



public class Main extends Application {
	private VBox vBox;

	@Override
	public void start(Stage primaryStage) {
		try {
			vBox = new VBox();
			
			Label chooseFileLabel = new Label("Click button to view file: ");
			Button chooseFileButton = new Button("View File");
			Button changeFileButton = new Button("Change File");
			
			
			chooseFileButton.setOnAction(evt -> {
				FileChooser fileChooser = new FileChooser();
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				Thread t = new Thread(new FileViewer(selectedFile));
				t.start();
			});

			changeFileButton.setOnAction(evt -> {
				Stage popupWindow = new Stage();
				popupWindow.initModality(Modality.APPLICATION_MODAL);
				popupWindow.initOwner(primaryStage);
				FlowPane flowPane = new FlowPane();

				ComboBox<String> comboDateOfBirthBox = new ComboBox<String>();
				comboDateOfBirthBox.getItems().addAll(
						"Date of Birth < 1850",
						"1850 > Date of Birth < 1950",
						"Date of Birth > 1950"
						);
				Button submitButton = new Button("Submit");
				submitButton.setOnAction(e -> {
					FileChooser fileChooser = new FileChooser();
					File selectedFile = fileChooser.showOpenDialog(primaryStage);
					File newFile = fileChooser.showSaveDialog(primaryStage);
					Thread t = null;
					for(int i = 0; i < comboDateOfBirthBox.getItems().size(); i++) {
						if(comboDateOfBirthBox.getItems().get(i).equals(comboDateOfBirthBox.getValue())) {
							t = new Thread(new FileChanger(selectedFile, newFile, i));
						}
					}
					t.start();
				});

				flowPane.getChildren().addAll(comboDateOfBirthBox, submitButton);
				
				Scene popupWindowScene = new Scene(flowPane, 500, 200);
				popupWindow.setScene(popupWindowScene);
				popupWindow.show();

				/*Stage popupWindow = new Stage();
				popupWindow.initModality(Modality.APPLICATION_MODAL);
				popupWindow.initOwner(primaryStage);
				FlowPane flowPane = new FlowPane();

				ComboBox<String> comboSortBox = new ComboBox<String>();
				comboSortBox.getItems().addAll(
						"Author, Date of Birth, Country",
						"Date of Birth", "Author", "Country",
						"Date of Birth", "Country", "Author"
						);

				Button submitButton = new Button("Submit");
				submitButton.setOnAction(e -> {
					FileChooser fileChooser = new FileChooser();
					File selectedFile = fileChooser.showOpenDialog(popupWindow);

				});

				flowPane.getChildren().addAll(comboSortBox, submitButton);
				Scene popupWindowScene = new Scene(flowPane, 500, 200);
				popupWindow.setScene(popupWindowScene);
				popupWindow.show();*/
			});

			vBox.getChildren().addAll(chooseFileLabel, chooseFileButton, changeFileButton);

			Scene scene = new Scene(vBox, 400, 400);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public class FileViewer implements Runnable {
		private File selectedFile;

		public FileViewer(File selectedFile) {
			this.selectedFile = selectedFile;
		}

		@Override
		public void run() {
			List<String> data = getDataFromFile(selectedFile);
			GridPane table = generateTable(data);
			GridPane.setConstraints(table, 0, 1);

			// Sleep this thread to show that we can still interact with our program
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable updater = new Runnable() {
				@Override
				public void run() {
					vBox.getChildren().add(table);
				}
			};
			Platform.runLater(updater);

		}
		
		private GridPane generateTable(List<String> str) {
			GridPane tablePane = new GridPane();
			int column, row;
			for(int i = 0; i < str.size(); i++) {
				column = 0;
				row = i;
				String[] arr = str.get(i).split(" ");
				Label writerId = new Label(arr[0]);
				Label writerName = new Label(arr[1]);
				Label writerDataOfBirth = new Label(arr[2]);
				Label writerCountry = new Label(arr[3]);
				GridPane.setConstraints(writerId, column++, row);
				GridPane.setConstraints(writerName, column++, row);
				GridPane.setConstraints(writerDataOfBirth, column++, row);
				GridPane.setConstraints(writerCountry, column, row);

				tablePane.getChildren().addAll(writerId, writerName, writerDataOfBirth, writerCountry);
			}

			return tablePane;
		}
	}

	public class FileChanger implements Runnable {
		private File selectedFile;
		private File newFile;
		private int minYear, maxYear;

		public FileChanger(File selectedFile, File newFile, int indexOfOption) {
			this.selectedFile = selectedFile;
			this.newFile = newFile;
			switch(indexOfOption) {
				case 0:
					minYear = Integer.MIN_VALUE;
					maxYear = 1850;
					break;
				case 1:
					minYear = 1850;
					maxYear = 1950;
					break;
				case 2:
					minYear = 1950;
					maxYear = Integer.MAX_VALUE;
			}
		}

		@Override
		public void run() {
			List<String> data = getDataFromFile(selectedFile);
			List<String> list = filterData(data);
			writeToANewFile(list);
		}
		
		private List<String> filterData(List<String> data) {
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < data.size(); i++) {
				int temp = Integer.parseInt(data.get(i).split(" ")[2]);
				if(temp > minYear && temp < maxYear) {
					list.add(data.get(i));
				}
				
			}
			return list;
		}
		
		private void writeToANewFile(List<String> list) {
			try {
				PrintWriter writer = new PrintWriter(newFile);
				for(int i = 0; i < list.size(); i++) {
					writer.write(list.get(i));
					writer.write("\r\n");
				}
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	private List<String> getDataFromFile(File file) {
		List<String> data = new ArrayList<String>();
		try {
			FileReader fInputStream = new FileReader(file);
			BufferedReader bInputStream = new BufferedReader(fInputStream);

			String temp;
			while((temp = bInputStream.readLine()) != null) {
				data.add(temp);
			}
			bInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}


}
