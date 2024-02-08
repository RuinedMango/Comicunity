package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.ImageArrGen;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class Main extends Application {
	// Inner vars
	private Scene scene;
	private Button libraryButton;
	private Button chooseButton;
	private Button lastButton;
	private Button nextButton;
	private Button jumpButton;
	private ImageView mainImage;
	private FileChooser fileChooser;
	private TextField jumpField;
	private Stage primaryStageInner;

	// File filter
	private FileChooser.ExtensionFilter filter;

	// Page you're on
	private int page = 0;

	@Override
	public void start(Stage primaryStage) throws IOException {
		// Initialize variables
		nextButton = new Button();
		lastButton = new Button();
		libraryButton = new Button();
		chooseButton = new Button();
		jumpField = new TextField();
		jumpButton = new Button();

		// Initialize file filter
		// Archives can't have folders within them yet, due to folder nesting skipping not being implemented
		filter = new FileChooser.ExtensionFilter("Comic Archives (*.zip, *.cbz, *.cb7, *.cbt)", "*.zip", "*.cbz",
				"*.cb7", "*.cbt");

		// Set all useful inner vars
		primaryStageInner = primaryStage;

		try {
			// Set all the text
			nextButton.setText("Next Page");
			lastButton.setText("Last Page");
			chooseButton.setText("Choose File");
			jumpField.setPromptText("Pg#");
			libraryButton.setText("Library");
			jumpButton.setText("Jump");

			// File chooser
			fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(filter);
			fileChooser.setTitle("Open Comic");
			try {
				mainImage = new ImageView(new Image(ImageArrGen.imageToMem(fileChooser.showOpenDialog(primaryStageInner).getAbsolutePath())[page]));
			}catch(NullPointerException excp){
				System.exit(-1);
			}
			primaryStage.setAlwaysOnTop(true);
			primaryStage.setAlwaysOnTop(false);

			// Instantiate widget group
			StackPane root = new StackPane();
			root.getChildren().addAll(nextButton, lastButton, libraryButton, chooseButton, jumpButton, mainImage, jumpField);

			// Move all to front
			nextButton.toFront();
			lastButton.toFront();
			chooseButton.toFront();
			libraryButton.toFront();
			jumpField.toFront();
			jumpButton.toFront();

			// Instantiate scene for translation calculations
			scene = new Scene(root, 400, 400);

			// Set all other misc shit
			jumpField.setMaxWidth(40);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			// Initialize the scene
			primaryStage.setScene(scene);
			primaryStage.toFront();
			primaryStage.show();
			primaryStage.setOnCloseRequest(arg0 -> System.exit(0));

			// Register resize listener
			ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> imageResize(mainImage);
			scene.widthProperty().addListener(stageSizeListener);
			scene.heightProperty().addListener(stageSizeListener);

			// Initial resize for fun
			imageResize(mainImage);

			// Set all the buttons to be useful
			setButtonCallbacks();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	// Resize my massive nuts
	// Translate my balls into your mouth
	public void imageResize(ImageView mainImage) {

		// Namesake of the function
		mainImage.setFitHeight(scene.getHeight());
		mainImage.setFitWidth(scene.getWidth());
		mainImage.setPreserveRatio(true);

		// Funny buttons
		chooseButton.setTranslateX(scene.getWidth() / 2 - 35);
		libraryButton.setTranslateX(-(scene.getWidth() / 2 - 25));

		// Last button standing
		lastButton.setTranslateX(libraryButton.getTranslateX() + 7);
		lastButton.setTranslateY(libraryButton.getTranslateY() - 27);

		// Next button standing
		nextButton.setTranslateX(chooseButton.getTranslateX());
		nextButton.setTranslateY(chooseButton.getTranslateY() - 27);

		// Translate one jump man field
		jumpField.setTranslateY(-(scene.getHeight() / 2) + 15);
		jumpField.setTranslateX(-(scene.getWidth() / 2) + 25);

		// Translate jump button
		jumpButton.setTranslateY(jumpField.getTranslateY() + 27);
		jumpButton.setTranslateX(jumpField.getTranslateX());
	}

	// The function to button them all MWUAHHAHAHA
	public void setButtonCallbacks() {
		// Set next action
		nextButton.setOnAction(arg0 -> {
			// Check if next page is null
			if (ImageArrGen.getOutputter()[page + 1] != null) {
				// Change the page variable
				page++;

				// Unvoid the page you came from. If you don't do this when you come back to the
				// page it's full white.
				try {
					ImageArrGen.getOutputter()[page - 1].reset();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Set the image
				mainImage.setImage(new Image(ImageArrGen.getOutputter()[page]));

				// IDK why this is here
				// TODO remove?
				imageResize(mainImage);
			}
		});

		// Set last action
		lastButton.setOnAction(arg0 -> {
			// Check if page would go negative
			if (page != 0) {
				// Set the page variable
				page--;

				// Unvoid the page you came from. If you don't do this when you come back to the
				// page it's full white.
				try {
					ImageArrGen.getOutputter()[page + 1].reset();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Set the image
				mainImage.setImage(new Image(ImageArrGen.getOutputter()[page]));

				// Fuck here too WHY
				// TODO remove?
				imageResize(mainImage);
			}
		});

		// The best button
		// Used to choose the book
		chooseButton.setOnAction(arg0 -> {

			// Open file chooser
			fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(filter);
			fileChooser.setTitle("Open Comic");

			// Change book by overriding outputter
			try {
				ImageArrGen.imageToMem(fileChooser.showOpenDialog(primaryStageInner).getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}catch(NullPointerException excp) {
				return;
			}
			
			// Unvoid the first page
			try {
				ImageArrGen.getOutputter()[0].reset();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Set the image to the first page
			mainImage.setImage(new Image(ImageArrGen.getOutputter()[0]));
			page = 0;
		});

		// Used to jump to pages using the text box
		// minus 1 from parsed int so that page 1 isn't 0 typed in
		jumpButton.setOnAction(arg0 -> {
			// Check if text is out of bounds and return
			if (Integer.parseInt(jumpField.getText()) > ImageArrGen.getOutputter().length - 1 || Integer.parseInt(jumpField.getText()) <= 0) {
				// Fuck U
				System.out.println("Fuck U");
				return;
			} else /* Main chunk of the func */ {
				// Unvoid the jumpto page
				try {
					ImageArrGen.getOutputter()[Integer.parseInt(jumpField.getText()) - 1].reset();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Set the image
				mainImage.setImage(new Image(ImageArrGen.getOutputter()[Integer.parseInt(jumpField.getText()) - 1]));

				// Unvoid the page you jumped from
				try {
					ImageArrGen.getOutputter()[page].reset();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Change the page number to fix the last and next buttons
				page = Integer.parseInt(jumpField.getText()) - 1;
			}
		});
	}
}
