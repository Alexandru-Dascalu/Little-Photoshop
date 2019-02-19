
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
import javafx.scene.chart.XYChart;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Photoshop extends Application
{
	private static final int BYTE_LIMIT = 255;

	private int[] gammaLookupTable;

	private int[] contrastLookupTable;
	        
	private double gammaValue;
	
	private int r1;
	private int s1;
	private int r2;
	private int s2;

	public Photoshop()
	{
		gammaValue = 1.0;
		gammaLookupTable = new int[BYTE_LIMIT+1];
		computeGammaLookUpTable();
		
		contrastLookupTable = new int[BYTE_LIMIT+1];
		computeContrastLookUpTable();
		r1 = 40;
		s1 = 40;
		r2 = 215;
		s2 = 215;
	}

	@Override
	public void start(Stage stage) throws FileNotFoundException
	{
		stage.setTitle("Photoshop");

		// Read the image
		Image image = new Image(new FileInputStream("raytrace.jpg"));

		// Create the graphical view of the image
		ImageView imageView = new ImageView(image);

		// Create the simple GUI
		Button invert_button = new Button("Invert");
		Button gamma_button = new Button("Gamma Correct");
		Button contrast_button = new Button("Contrast Stretching");
		Button histogramButton = new Button("Histograms");
		Button cc_button = new Button("Cross Correlation");
		Button resetButton = new Button("Reset Image");
		Button setContrastValueBtn = new Button("Set contrast values");
		Label gammaInputLabel = new Label("Gamma value:");
		TextField gammaInput = new TextField();
		gammaInput.setText("1");

		// Add all the event handlers (this is a minimal GUI - you may try to do
		// better)
		invert_button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Invert");
				// At this point, "image" will be the original image
				// imageView is the graphical representation of an image
				// imageView.getImage() is the currently displayed image

				// Let's invert the currently displayed image by calling the
				// invert function later in the code
				Image inverted_image = imageInverter(imageView.getImage());
				// Update the GUI so the new image is displayed
				imageView.setImage(inverted_image);
			}
		});

		gamma_button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Gamma Correction");

				long start = System.currentTimeMillis();
				Image correctedImage = gammaCorrecter(image);
				
				gammaValue = Double.parseDouble(gammaInput.getText());
				computeGammaLookUpTable();
				
				imageView.setImage(correctedImage);
				System.out.println(System.currentTimeMillis() - start);
			}
		});

		contrast_button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Contrast Stretching");
				
				long start = System.currentTimeMillis();
				Image stretchedImage = contrastStretcher(image);
				imageView.setImage(stretchedImage);
				System.out.println(System.currentTimeMillis() - start);
			}
		});
		
		setContrastValueBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				makeContrastInputWindow();
			}
		});

		histogramButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				showHistograms(image);
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

		// Using a flow pane
		BorderPane root = new BorderPane();

		HBox topElements = new HBox(5);
		topElements.setAlignment(Pos.CENTER);
		// Gaps between buttons
		
		// Add all the buttons and the image for the GUI
		topElements.getChildren().addAll(invert_button, gamma_button, contrast_button,
				histogramButton, cc_button, resetButton);

		VBox inputs = new VBox();
		inputs.getChildren().addAll(gammaInputLabel, gammaInput, setContrastValueBtn);
		
		root.setTop(topElements);
		root.setCenter(imageView);
		root.setRight(inputs);

		// Display to user
		Scene scene = new Scene(root, 1024, 768);
		stage.setScene(scene);
		stage.show();
	}

	// Example function of invert
	public Image imageInverter(Image image)
	{
		// Find the width and height of the image to be process
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		// Create a new image of that width and height
		WritableImage inverted_image = new WritableImage(width, height);
		// Get an interface to write to that image memory
		PixelWriter inverted_image_writer = inverted_image.getPixelWriter();
		// Get an interface to read from the original image passed as the
		// parameter to the function
		PixelReader image_reader = image.getPixelReader();

		// Iterate over all pixels
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				// For each pixel, get the colour
				Color color = image_reader.getColor(x, y);
				// Do something (in this case invert) - the getColor function
				// returns colours as 0..1 doubles (we could multiply by 255 if
				// we want 0-255 colours)
				color = Color.color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue());
				// Note: for gamma correction you may not need the divide by 255
				// since getColor already returns 0-1, nor may you need multiply
				// by 255 since the Color.color function consumes 0-1 doubles.

				// Apply the new colour
				inverted_image_writer.setColor(x, y, color);
			}
		}
		return inverted_image;
	}

	private void computeGammaLookUpTable()
	{
		for (int i = 0; i <= BYTE_LIMIT; i++)
		{
			int correctedValue = (int) (BYTE_LIMIT * Math.pow(i / 255.0, 1.0 / gammaValue));
			gammaLookupTable[i] = correctedValue;
		}
	}

	private int getCorrectedValue(double colour)
	{
		int integerColour = (int) (colour * BYTE_LIMIT);
		return gammaLookupTable[integerColour];
	}

	public Image gammaCorrecter(Image image)
	{
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();

		WritableImage correctedImage = new WritableImage(width, height);

		PixelWriter correctedImageWriter = correctedImage.getPixelWriter();
		PixelReader imageReader = image.getPixelReader();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
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

	public Image gammaCorrecter(Image image, double gammaValue)
	{
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();

		WritableImage correctedImage = new WritableImage(width, height);

		PixelWriter correctedImageWriter = correctedImage.getPixelWriter();
		PixelReader imageReader = image.getPixelReader();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Color pixelColor = imageReader.getColor(x, y);

				int correctedRed = (int) (255 * Math.pow(pixelColor.getRed(), 1.0 / gammaValue));
				int correctedGreen = (int) (255 * Math.pow(pixelColor.getGreen(), 1.0 / gammaValue));
				int correctedBlue = (int) (255 * Math.pow(pixelColor.getBlue(), 1.0 / gammaValue));

				Color correctedColor = Color.rgb(correctedRed, correctedGreen, correctedBlue);

				correctedImageWriter.setColor(x, y, correctedColor);
			}
		}

		return correctedImage;
	}
	
	public Image contrastStretcher(Image image)
	{
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();

		WritableImage correctedImage = new WritableImage(width, height);

		PixelWriter correctedImageWriter = correctedImage.getPixelWriter();
		PixelReader imageReader = image.getPixelReader();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Color pixelColor = imageReader.getColor(x, y);

				int correctedRed = getContrastStretchedValue(pixelColor.getRed());
				int correctedGreen = getContrastStretchedValue(pixelColor.getGreen());
				int correctedBlue = getContrastStretchedValue(pixelColor.getBlue());

				Color correctedColor = Color.rgb(correctedRed, correctedGreen, correctedBlue);

				correctedImageWriter.setColor(x, y, correctedColor);
			}
		}

		return correctedImage;
	}

	public int getContrastStretchedValue(double colourValue)
	{
		int colour = (int)(colourValue * BYTE_LIMIT);
		return contrastLookupTable[colour]; 
	}
	
	private void computeContrastLookUpTable()
    {
        for (int colour = 0; colour <= BYTE_LIMIT; colour++)
        {
            int correctedColour;
            if(colour < r1)
            {
                correctedColour = (int)(((double)s1)/r1 * colour);
            }
            else if(r1 <= colour && colour <= r2)
            {
                double multiplier = ((double)(s2 - s1))/(r2 - r1);
                correctedColour = (int) (multiplier * (colour - r1) + s1);
            }
            else
            {
                double multiplier = ((double)(BYTE_LIMIT - s2)) / 
                        (BYTE_LIMIT - r2);
                correctedColour = (int) (multiplier * (colour - r2) + s2);
            }
            
            contrastLookupTable[colour]  = correctedColour;
        }
    }
	
	public void makeContrastInputWindow()
	{
		Stage contrastInput = new Stage();
		contrastInput.setHeight(800);
		contrastInput.setWidth(800);
		contrastInput.setTitle("Contrast Stretch Values Input");
		
		NumberAxis xAxis = new NumberAxis("Input", 0, 255, 25);
		NumberAxis yAxis = new NumberAxis("Output", 0, 255, 25);
		
		LineChart<Number, Number> contrastInputChart = new LineChart<>(xAxis, yAxis);
		
		XYChart.Series<Number, Number> inputPoints = new XYChart.Series<>();
		
		XYChart.Data<Number, Number> originPoint = new XYChart.Data<>(0, 0);
		XYChart.Data<Number, Number> endPoint = new XYChart.Data<>(255, 255);
		XYChart.Data<Number, Number> point1 = new XYChart.Data<>(r1, s1);
		XYChart.Data<Number, Number> point2 = new XYChart.Data<>(r2, s2);
		
		inputPoints.getData().add(originPoint);
		inputPoints.getData().add(point1);
		inputPoints.getData().add(point2);
		inputPoints.getData().add(endPoint);
		
		contrastInputChart.getData().add(inputPoints);
		contrastInputChart.setAnimated(false);
		contrastInputChart.setLegendVisible(false);
		
		point1.getNode().setOnMouseDragged(new EventHandler<MouseEvent>()
		{
		    @Override
		    public void handle(MouseEvent m)
		    {
		        Number newX = xAxis.getValueForDisplay(m.getSceneX() - 60);
                Number newY = yAxis.getValueForDisplay(m.getSceneY() - 8);
                
                point1.setXValue(newX.intValue());
                point1.setYValue(newY.intValue());
                
                r1 = newX.intValue();
                s1 = newY.intValue();
                
                computeContrastLookUpTable();
		    }
		});
		
		point2.getNode().setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent m)
            {
                Number newX = xAxis.getValueForDisplay(m.getSceneX() - 60);
                Number newY = yAxis.getValueForDisplay(m.getSceneY() - 8);
                
                point2.setXValue(newX.intValue());
                point2.setYValue(newY.intValue());
                
                r2 = newX.intValue();
                s2 = newY.intValue();
                
                computeContrastLookUpTable();
            }
        });
		
		Scene contrastInputScene = new Scene(contrastInputChart, 800, 800);
		contrastInput.setScene(contrastInputScene);
		contrastInput.show();
	}
	
	public int[][] getHistogram(Image image)
	{
        // Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        
        // Get an interface to read from the original image passed as the
        // parameter to the function
        PixelReader image_reader = image.getPixelReader();
        
        int[][] histogram = new int[BYTE_LIMIT+1][4];
        
        for(int i=0; i<BYTE_LIMIT+1; i++)
        {
            for(int j=0; j<4; j++)
            {
                histogram[i][j] = 0;
            }
        }
        
        for(int y=0; y<height; y++)
        {
            for(int x=0; x<width; x++)
            {
                Color color = image_reader.getColor(x, y);
                
                int red = (int)(color.getRed()*BYTE_LIMIT);
                histogram[red][0]++;
                
                int green = (int)(color.getGreen()*BYTE_LIMIT);
                histogram[green][1]++;
                
                int blue = (int)(color.getBlue()*BYTE_LIMIT);
                histogram[blue][2]++;
                
                int brightness = (red + blue + green)/3;
                histogram[brightness][3]++;
            }
        }
        
        return histogram;
	}
	
	public int getMaxFromHistogram(int[] histogram)
	{
	    int max=0;
	    for(int i=0; i<histogram.length; i++)
	    {
	        if(max < histogram[i])
	        {
	            max = histogram[i];
	        }
	    }
	    
	    return max;
	}
	
	public XYChart.Series<Number, Number> getHistogramSeries(int[][] histogram, Color color)
	{
	    XYChart.Series<Number, Number> intensityLevelCount = new XYChart.Series<>();
	    
	    if(color.equals(Color.RED))
	    {
	        for(int i=0; i<BYTE_LIMIT+1; i++)
	        {
	            XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[i][0]);
	            intensityLevelCount.getData().add(intensityCount);
	        }
	    }
	    else if(color.equals(Color.GREEN))
	    {
	        for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[i][1]);
                intensityLevelCount.getData().add(intensityCount);
            }
	    }
	    else if(color.equals(Color.BLUE))
        {
            for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[i][2]);
                intensityLevelCount.getData().add(intensityCount);
            }
        }
	    else if(color.equals(Color.GREY))
        {
            for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[i][3]);
                intensityLevelCount.getData().add(intensityCount);
            }
        }
	    else
	    {
	        throw new IllegalArgumentException();
	    }
	    
	    return intensityLevelCount;
	}
	
	public void showHistograms(Image image)
	{
	    Stage histogramWindow = new Stage();
	    histogramWindow.setWidth(700);
	    histogramWindow.setHeight(700);
	    histogramWindow.setTitle("Histogram View");
	    
	    int[][] histogram = getHistogram(image);
	    int maxValue = getMaxFromHistogram(histogram[0]);
	    
	    ValueAxis<Number> xAxis = new NumberAxis("Intensity Levels", 0, BYTE_LIMIT,25);
        ValueAxis<Number> yAxis = new NumberAxis("Number of pixels", 0, maxValue, 25);
        
        AreaChart<Number, Number> histogramChart = new AreaChart<>(xAxis, yAxis);
        
        XYChart.Series<Number, Number> redLevelCount = getHistogramSeries(histogram, Color.RED);
        XYChart.Series<Number, Number> greenLevelCount = getHistogramSeries(histogram, Color.GREEN);
        XYChart.Series<Number, Number> blueLevelCount = getHistogramSeries(histogram, Color.BLUE);
        XYChart.Series<Number, Number> brightnessLevelCount = getHistogramSeries(histogram, Color.GREY);
        
        histogramChart.getData().addAll(redLevelCount, new XYChart.Series<Number, Number>(), 
            greenLevelCount, blueLevelCount, new XYChart.Series<>(),  brightnessLevelCount);
        
        histogramChart.setLegendVisible(false);
        histogramChart.setCreateSymbols(false);
        
        Scene histogramView = new Scene(histogramChart, 700, 700);
        histogramWindow.setScene(histogramView);
        histogramWindow.show();
	}
	
	public static void main(String[] args)
	{
		launch();
	}

}
