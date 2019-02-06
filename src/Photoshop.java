/*
CS-255 Getting started code for the assignment
I do not give you permission to post this code online
Do not post your solution online
Do not copy code
Do not use JavaFX functions or other libraries to do the main parts of the assignment:
	Gamma Correction
	Contrast Stretching
	Histogram calculation and equalisation
	Cross correlation
All of those functions must be written by yourself
You may use libraries to achieve a better GUI
*/
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;  
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Photoshop extends Application 
{
    private static final int BYTE_LIMIT = 256; 
    
    private int[] gammaLookupTable;

    private double gammaValue;
    
    public Photoshop()
    {
        gammaValue = 1.0;
        computeGammaLookUpTable();
    }
    
    @Override
    public void start(Stage stage) throws FileNotFoundException 
    {
		stage.setTitle("Photoshop");

		//Read the image
		Image image = new Image(new FileInputStream("Far Cry 3.png"));  

		//Create the graphical view of the image
		ImageView imageView = new ImageView(image); 
		
		//Create the simple GUI
		Button invert_button = new Button("Invert");
		Button gamma_button = new Button("Gamma Correct");
		Button contrast_button = new Button("Contrast Stretching");
		Button histogram_button = new Button("Histograms");
		Button cc_button = new Button("Cross Correlation");
		Button resetButton = new Button("Reset Image");
		Button saveGammaButton = new Button("Save Gamma Value");
		Label gammaInputLabel = new Label("Gamma value:");
		TextField gammaInput = new TextField();
        
		//Add all the event handlers (this is a minimal GUI - you may try to do better)
		invert_button.setOnAction(new EventHandler<ActionEvent>() 
		{
            @Override
            public void handle(ActionEvent event) 
            {
                System.out.println("Invert");
				//At this point, "image" will be the original image
				//imageView is the graphical representation of an image
				//imageView.getImage() is the currently displayed image
				
				//Let's invert the currently displayed image by calling the invert function later in the code
				Image inverted_image=imageInverter(imageView.getImage());
				//Update the GUI so the new image is displayed
				imageView.setImage(inverted_image);
            }
        });

		gamma_button.setOnAction(new EventHandler<ActionEvent>() 
		{
            @Override
            public void handle(ActionEvent event) 
            {
                System.out.println("Gamma Correction");
                
                Image correctedImage = gammaCorrecter(image);
                imageView.setImage(correctedImage);
            }
        });

		contrast_button.setOnAction(new EventHandler<ActionEvent>() 
		{
            @Override
            public void handle(ActionEvent event) 
            {
                System.out.println("Contrast Stretching");
            }
        });
		
		histogram_button.setOnAction(new EventHandler<ActionEvent>() 
		{
            @Override
            public void handle(ActionEvent event) 
            {
                System.out.println("Histogram");
            }
        });
		
		cc_button.setOnAction(new EventHandler<ActionEvent>() 
		{
            @Override
            public void handle(ActionEvent event) 
            {
                System.out.println("Cross Correlation");
            }
        });
		
		resetButton.setOnAction(new EventHandler<ActionEvent>()
		{
		    @Override
		    public void handle(ActionEvent event)
		    {
		        imageView.setImage(image);
		    }
		});
		
		saveGammaButton.setOnAction(new EventHandler<ActionEvent>()
		{
		    @Override
		    public void handle(ActionEvent event)
		    {
		        String gammaText = gammaInput.getText();
		        if(!gammaText.equals(""))
		        {
		            gammaValue = Double.parseDouble(gammaInput.getText());
	                computeGammaLookUpTable();
		        }
		    }
		});
		
		//Using a flow pane
		FlowPane root = new FlowPane();
		//Gaps between buttons
		root.setVgap(10);
        root.setHgap(5);

		//Add all the buttons and the image for the GUI
		root.getChildren().addAll(invert_button, gamma_button, contrast_button,
		    histogram_button, cc_button, resetButton, gammaInputLabel, gammaInput,
		    saveGammaButton,  imageView);

		//Display to user
        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }

	//Example function of invert
	public Image imageInverter(Image image) 
	{
		//Find the width and height of the image to be process
		int width = (int)image.getWidth();
        int height = (int)image.getHeight();
		//Create a new image of that width and height
		WritableImage inverted_image = new WritableImage(width, height);
		//Get an interface to write to that image memory
		PixelWriter inverted_image_writer = inverted_image.getPixelWriter();
		//Get an interface to read from the original image passed as the parameter to the function
		PixelReader image_reader=image.getPixelReader();
		
		//Iterate over all pixels
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				//For each pixel, get the colour
				Color color = image_reader.getColor(x, y);
				//Do something (in this case invert) - the getColor function returns colours as 0..1 doubles (we could multiply by 255 if we want 0-255 colours)
				color=Color.color(1.0-color.getRed(), 1.0-color.getGreen(), 1.0-color.getBlue());
				//Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.
				
				//Apply the new colour
				inverted_image_writer.setColor(x, y, color);
			}
		}
		return inverted_image;
	}
	
	private void computeGammaLookUpTable()
	{
	    gammaLookupTable = new int[BYTE_LIMIT];
	    for(int i=0; i < BYTE_LIMIT; i++)
	    {
	        int correctedValue = (int)(255*Math.pow(i/255.0, 1.0/gammaValue));
	        gammaLookupTable[i] = correctedValue;
	    }
	}
	
	private int getCorrectedValue(double colour)
	{
	    int integerColour = (int)(colour*255);
	    return gammaLookupTable[integerColour];
	}
	
	public Image gammaCorrecter(Image image)
	{
		int width = (int)image.getWidth();
		int height = (int)image.getHeight();
		
		WritableImage correctedImage = new WritableImage(width, height);
		
		PixelWriter correctedImageWriter = correctedImage.getPixelWriter();
		PixelReader imageReader = image.getPixelReader();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				Color pixelColor = imageReader.getColor(x, y);
				
				int correctedRed = getCorrectedValue(pixelColor.getRed());
				int correctedGreen = getCorrectedValue(pixelColor.getGreen());
				int correctedBlue = getCorrectedValue(pixelColor.getBlue());
				
				Color correctedColor = Color.rgb(correctedRed, correctedGreen, correctedBlue);
				
				correctedImageWriter.setColor(x, y, correctedColor);
			}
		}
		
		return correctedImage;
	}
		
    public static void main(String[] args) 
    {
        launch();
    }

}
