import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Name: Alexandru Dascalu
 * Student ID 965337
 * @author Alexandru Dascalu
 * 
 * I declare that this class is my own piece of work, and that I did not copy any
 * of the functions that change the graphics of the image from a colleague or 
 * from the Internet, nor have I looked at any piece of code of any of my coursemates.
 */
public class Photoshop extends Application
{
    /**The maximum value of a color channel. Used in many computations.*/
	private static final int BYTE_LIMIT = 255;

	/**The array that holds the precomputed brightness mapping for a given gamma 
	 * value.*/
	private int[] gammaLookupTable;

	/**The array that holds the pre-computed brightness mapping for the given 4 
	 * contrast stretch values.*/
	private int[] contrastLookupTable;
	   
	/**The Filter used for cross corelation.*/
	private static final int[][] laplacianFilter = {{-4,-1,0,-1,-4}, 
	                                                {-1,2,3,2,-1}, 
	                                                {0,3,4,3,0}, 
	                                                {-1,2,3,2,-1}, 
	                                                {-4,-1,0,-1,-4}};
	
	/**The value of the gamma variable used in gamma correction, set by the user.*/
	private double gammaValue;
	
	/**X axis value of the first point used for contrast stretching.*/
	private int r1;
	
	/**Y axis value of the first point used for contrast stretching.*/
	private int s1;
	
	/**X axis value of the second point used for contrast stretching.*/
	private int r2;
	
	/**Y axis value of the second point used for contrast stretching.*/
	private int s2;

	public static void main(String[] args)
    {
        launch();
    }
	
	/**
	 * Makes a new Photoshop application. Initialises the gammaValue and contrast 
	 * input values, as well the lookup tables for gamma correction and contrast stretching.
	 */
	public Photoshop()
	{
		gammaValue = 1.0;
		gammaLookupTable = new int[BYTE_LIMIT+1];
		computeGammaLookUpTable();
		
		contrastLookupTable = new int[BYTE_LIMIT+1];
		r1 = 40;
        s1 = 40;
        r2 = 215;
        s2 = 215;
		computeContrastLookUpTable();
	}

	/**
	 * Addes all the graphical elements to the main window of the application
	 *  and shows it.
	 *  @param stage The stage given by the JavaFX system.
	 */
	@Override
	public void start(Stage stage) throws FileNotFoundException
	{
		stage.setTitle("Photoshop");

		// Read the image
		Image image = new Image(new FileInputStream("raytrace.jpg"));

		// Create the graphical view of the image
		ImageView imageView = new ImageView(image);
		if(image.getHeight() > 800 && image.getWidth() > 1000)
		{
		    imageView.setFitWidth(1000);
	        imageView.setFitHeight(800);
		}
		
		// Create the simple GUI
		Button invertButton = new Button("Invert");
		Button gammaButton = new Button("Gamma Correct");
		Button contrastButton = new Button("Contrast Stretching");
		Button histogramButton = new Button("Histograms");
		Button ccButton = new Button("Cross Correlation");
		Button resetButton = new Button("Reset Image");

		invertButton.setOnAction(new EventHandler<ActionEvent>()
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

		gammaButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
			    showGammaInput(imageView, image);
			}
		});

		contrastButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Contrast Stretching");
				
				makeContrastInputWindow(imageView);
			}
		});
		
		histogramButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				showHistograms(imageView);
			}
		});

		ccButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("Cross Correlation");
				
				imageView.setImage(getCrossCorelatedImage(image));
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

		BorderPane root = new BorderPane();

		HBox topElements = new HBox(5);
		topElements.setAlignment(Pos.CENTER);
		
		// Add all the buttons and the image for the GUI
		topElements.getChildren().addAll(invertButton, gammaButton, contrastButton,
				histogramButton, ccButton, resetButton);

		root.setTop(topElements);
		root.setCenter(imageView);

		// Display to user
		Scene scene = new Scene(root, 1600, 900);
		scene.getStylesheets().add(Photoshop.class.getResource("Photoshop.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Makes an image that is the an inversion of the given image.
	 * @param image The image we want ot invert.
	 * @return A new image that is the inverted version of the given image.
	 */
	private Image imageInverter(Image image)
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
				color = Color.color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue());
				
				inverted_image_writer.setColor(x, y, color);
			}
		}
		return inverted_image;
	}

	/**
	 * Pre-computes the gamma brightness mapping for each level between 0 and 
	 * 255 based on the current gamma value.
	 */
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

	private Image gammaCorrecter(Image image)
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

	/**
	 * Makes a new image that is a gamma corrected version of the given image, 
	 * based on the given gamma value.
	 * @param image The image we want to correct
	 * @param gammaValue The gamma value we want to use.
	 * @return A new gamma corrected image based on the given image and gamma correction value.
	 */
	private Image gammaCorrecter(Image image, double gammaValue)
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
	
	private void showGammaInput(ImageView imageView, Image originalImage)
	{
	    Stage gammaWindow = new Stage();
	    gammaWindow.setWidth(800);
	    gammaWindow.setMaxWidth(1000);
	    gammaWindow.setHeight(300);
	    gammaWindow.setMaxHeight(300);
	    gammaWindow.setTitle("Gamma Input");
	    
	    BorderPane allGammaInputs = new BorderPane();
	    HBox manualGammaInput = new HBox(5);
	    VBox topChild = new VBox(5);
	    
	    Label gammaInputLabel = new Label("If desired value is out of slider range, type it here:");
        TextField gammaInput = new TextField();
        
        Label validInputText = new Label("Invalid Gamma Input! Type in a floating point number!");
        validInputText.setVisible(false);
        validInputText.setTextFill(Color.FIREBRICK);
        
        gammaInput.setText("1");
        
        manualGammaInput.getChildren().addAll(gammaInputLabel, gammaInput);
        manualGammaInput.setAlignment(Pos.CENTER);
        topChild.getChildren().addAll(manualGammaInput, validInputText);
        topChild.setAlignment(Pos.CENTER);
        
        Slider gammaSlider = new Slider(0, 10, 1);
        gammaSlider.setShowTickMarks(true);
        gammaSlider.setShowTickLabels(true);
        gammaSlider.setMajorTickUnit(0.5);
        gammaSlider.setPrefHeight(2000);
        gammaSlider.setPadding(new Insets(50, 50, 50, 50));
        
        gammaSlider.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            gammaValue = newValue.doubleValue();
            computeGammaLookUpTable();
            imageView.setImage(gammaCorrecter(originalImage));
        });
        
        gammaInput.textProperty().addListener((observable, oldValue, newValue) ->
        {
            try
            {
                gammaValue = Double.parseDouble(newValue);
                computeGammaLookUpTable();
                imageView.setImage(gammaCorrecter(originalImage));
                validInputText.setVisible(false);
            }
            catch(NumberFormatException e)
            {
                validInputText.setVisible(true);
            }
        });
        
        allGammaInputs.setCenter(gammaSlider);
        allGammaInputs.setTop(topChild);
        allGammaInputs.setPadding(new Insets(30, 30, 30, 30));
        
        Scene gammaInputScene = new Scene(allGammaInputs, 1000, 300);
        gammaWindow.setScene(gammaInputScene);
        gammaWindow.show();
	}
	
	private Image contrastStretcher(Image image)
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

	private int getContrastStretchedValue(double colourValue)
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
	
	private void makeContrastInputWindow(ImageView imageView)
	{
		Stage contrastInput = new Stage();
		contrastInput.setHeight(800);
		contrastInput.setWidth(1400);
		contrastInput.setTitle("Contrast Stretch Values Input");
		
		GridPane valuesPane = new GridPane();
		valuesPane.setHgap(5);
		valuesPane.setVgap(5);
		valuesPane.setAlignment(Pos.TOP_CENTER);
		valuesPane.setPadding(new Insets(20, 20, 20, 20));
		
		Label r1Label = new Label("R1 Value:");
		Label s1Label = new Label("S1 Value:");
		Label r2Label = new Label("R2 Value:");
		Label s2Label = new Label("S2 Value:");
		
		valuesPane.add(r1Label, 0, 0);
		valuesPane.add(s1Label, 0, 1);
		valuesPane.add(r2Label, 0, 2);
		valuesPane.add(s2Label, 0, 3);
		
		Text r1Text = new Text(r1 + "");
		Text s1Text = new Text(s1 + "");
		Text r2Text = new Text(r2 + "");
		Text s2Text = new Text(s2 + "");
		
		valuesPane.add(r1Text, 1, 0);
		valuesPane.add(s1Text, 1, 1);
		valuesPane.add(r2Text, 1, 2);
		valuesPane.add(s2Text, 1, 3);
		
		Button applyContrastBtn = new Button("Apply Contrast Stretching");
		valuesPane.add(applyContrastBtn, 0, 4, 2, 1);
		
		Text contrastSuccess = new Text("Contrast Strecthing applied succesfully!");
		contrastSuccess.setFill(Color.GREEN);
		contrastSuccess.setVisible(false);
		valuesPane.add(contrastSuccess, 0, 5, 2, 1);
		
		NumberAxis xAxis = new NumberAxis("Input", 0, BYTE_LIMIT, 25);
		NumberAxis yAxis = new NumberAxis("Output", 0, BYTE_LIMIT, 25);
		
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
		        contrastSuccess.setVisible(false);
                
		        Number newX = xAxis.getValueForDisplay(m.getSceneX() - 60);
                Number newY = yAxis.getValueForDisplay(m.getSceneY() - 8);
                
                point1.setXValue(newX.intValue());
                point1.setYValue(newY.intValue());
                
                r1 = newX.intValue();
                s1 = newY.intValue();
                
                r1Text.setText(r1 + "");
                s1Text.setText(s1 + "");
                computeContrastLookUpTable();
		    }
		});
		
		point2.getNode().setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent m)
            {
                contrastSuccess.setVisible(false);
                
                Number newX = xAxis.getValueForDisplay(m.getSceneX() - 60);
                Number newY = yAxis.getValueForDisplay(m.getSceneY() - 8);
                
                point2.setXValue(newX.intValue());
                point2.setYValue(newY.intValue());
                
                r2 = newX.intValue();
                s2 = newY.intValue();
                
                r2Text.setText(r2 + "");
                s2Text.setText(s2 + "");
                computeContrastLookUpTable();
            }
        });
		
		applyContrastBtn.setOnAction(e ->
		{
		    imageView.setImage(contrastStretcher(imageView.getImage()));
		    contrastSuccess.setVisible(true);
		});
		
		BorderPane contrastInputPane = new BorderPane();
		contrastInputPane.setCenter(contrastInputChart);
		contrastInputPane.setRight(valuesPane);
		
		Scene contrastInputScene = new Scene(contrastInputPane, 1400, 800);
		contrastInput.setScene(contrastInputScene);
		contrastInput.show();
	}
	
	private int[][] getHistogram(Image image)
	{
        // Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        
        // Get an interface to read from the original image passed as the
        // parameter to the function
        PixelReader image_reader = image.getPixelReader();
        
        int[][] histogram = new int[5][BYTE_LIMIT+1];
        
        for(int i=0; i < 5; i++)
        {
            for(int j=0; j <= BYTE_LIMIT; j++)
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
                histogram[0][red]++;
                
                int green = (int)(color.getGreen()*BYTE_LIMIT);
                histogram[1][green]++;
                
                int blue = (int)(color.getBlue()*BYTE_LIMIT);
                histogram[2][blue]++;
                
                int brightness = (red + blue + green)/3;
                histogram[3][brightness]++;
                
                /*Get the brightness of the color in the HSV model for accurate
                 * equalization on the coloured image.*/  
                HSVColor hsvColor = new HSVColor(color.getRed(), color.getGreen(), color.getBlue());
                int value = (int)(hsvColor.getValue() * BYTE_LIMIT);
                histogram[4][value]++;
            }
        }
        
        return histogram;
	}
	
	private int getMaxFromHistogram(int[] histogram)
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
	
	private XYChart.Series<Number, Number> getHistogramSeries(int[][] histogram, Color color)
	{
	    XYChart.Series<Number, Number> intensityLevelCount = new XYChart.Series<>();
	    
	    if(color.equals(Color.RED))
	    {
	        for(int i=0; i<BYTE_LIMIT+1; i++)
	        {
	            XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[0][i]);
	            intensityLevelCount.getData().add(intensityCount);
	        }
	    }
	    else if(color.equals(Color.GREEN))
	    {
	        for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[1][i]);
                intensityLevelCount.getData().add(intensityCount);
            }
	    }
	    else if(color.equals(Color.BLUE))
        {
            for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[2][i]);
                intensityLevelCount.getData().add(intensityCount);
            }
        }
	    else if(color.equals(Color.GREY))
        {
            for(int i=0; i<BYTE_LIMIT+1; i++)
            {
                XYChart.Data<Number, Number> intensityCount = new XYChart.Data<>(i, histogram[3][i]);
                intensityLevelCount.getData().add(intensityCount);
            }
        }
	    else
	    {
	        throw new IllegalArgumentException();
	    }
	    
	    return intensityLevelCount;
	}
	
	private AreaChart<Number, Number> getHistogramChart(int[][] histogram)
	{
	    
        int maxValue = getMaxFromHistogram(histogram[0]);
        
	    ValueAxis<Number> xAxis = new NumberAxis("Intensity Levels", 0, BYTE_LIMIT,25);
        ValueAxis<Number> yAxis = new NumberAxis("Number of pixels", 0, maxValue, 25);
        
        AreaChart<Number, Number> histogramChart = new AreaChart<>(xAxis, yAxis);
        
        XYChart.Series<Number, Number> redLevelCount = getHistogramSeries(histogram, Color.RED);
        redLevelCount.setName("Red Channel");
        XYChart.Series<Number, Number> greenLevelCount = getHistogramSeries(histogram, Color.GREEN);
        greenLevelCount.setName("Green Channel");
        XYChart.Series<Number, Number> blueLevelCount = getHistogramSeries(histogram, Color.BLUE);
        blueLevelCount.setName("Blue Channel");
        XYChart.Series<Number, Number> brightnessLevelCount = getHistogramSeries(histogram, Color.GREY);
        brightnessLevelCount.setName("Brightness");
        
        histogramChart.getData().addAll(redLevelCount, brightnessLevelCount, greenLevelCount, blueLevelCount);
        histogramChart.setCreateSymbols(false);
        histogramChart.setAnimated(false);
        
        return histogramChart;
	}
	
	private void showHistograms(ImageView imageView)
	{
	    Image image = imageView.getImage();
	    Stage histogramWindow = new Stage();
	    histogramWindow.setWidth(1500);
	    histogramWindow.setHeight(1000);
	    histogramWindow.setTitle("Histogram View");
	    
	    int[][] histogram = getHistogram(image);
	    AreaChart<Number, Number> histogramChart = getHistogramChart(histogram);
	    
        /*XYChart.Series<Number, Number> redLevelCount = histogramChart.getData().get(0);
        XYChart.Series<Number, Number> greenLevelCount = histogramChart.getData().get(2);
        XYChart.Series<Number, Number> blueLevelCount = histogramChart.getData().get(3);
        XYChart.Series<Number, Number> brightnessLevelCount = histogramChart.getData().get(1);*/
        
        BorderPane histogramPane = new BorderPane();
        histogramPane.setCenter(histogramChart);
        
        VBox colourButtons = new VBox(5);
        Button redButton = new Button("Red Histogram");
        Button greenButton = new Button("Green Histogram");
        Button blueButton = new Button("Blue Histogram");
        Button brightnessButton = new Button("Brightness Histogram");
        Button rgbButton = new Button("RGB histogram");
        Button equalizationButton = new Button("Equalize histogram");
        
        colourButtons.getChildren().addAll(redButton, greenButton, blueButton, 
            brightnessButton, rgbButton, equalizationButton);
        histogramPane.setRight(colourButtons);
        
        redButton.setOnAction(e -> 
        {
            int[][] newHistogram = getHistogram(imageView.getImage());
            AreaChart<Number, Number> newHistogramChart = (AreaChart<Number, Number>) histogramPane.getCenter();
            XYChart.Series<Number, Number> redLevelCount = getHistogramSeries(newHistogram, Color.RED);
            redLevelCount.setName("Red Channel");
            
            newHistogramChart.getData().clear();
            newHistogramChart.getData().add(redLevelCount);
            newHistogramChart.setLegendVisible(false);
        });
        
        greenButton.setOnAction(e -> 
        {
            int[][] newHistogram = getHistogram(imageView.getImage());
            AreaChart<Number, Number> newHistogramChart = (AreaChart<Number, Number>) histogramPane.getCenter();
            XYChart.Series<Number, Number> greenLevelCount = getHistogramSeries(newHistogram, Color.GREEN);
            greenLevelCount.setName("Green Channel");
            
            newHistogramChart.getData().clear();
            newHistogramChart.getData().addAll(new XYChart.Series<>(), 
                new XYChart.Series<>(), greenLevelCount);
            newHistogramChart.setLegendVisible(false);
        });
        
        blueButton.setOnAction(e -> 
        {
            int[][] newHistogram = getHistogram(imageView.getImage());
            AreaChart<Number, Number> newHistogramChart = (AreaChart<Number, Number>) histogramPane.getCenter();
            XYChart.Series<Number, Number> blueLevelCount = getHistogramSeries(newHistogram, Color.BLUE);
            blueLevelCount.setName("Blue Channel");
            
            //histogram = getHistogram(imageView.getImage());
            newHistogramChart.getData().clear();
            newHistogramChart.getData().addAll(new XYChart.Series<>(), 
                new XYChart.Series<>(), new XYChart.Series<>(), blueLevelCount);
            newHistogramChart.setLegendVisible(false);
        });
        
        brightnessButton.setOnAction(e -> 
        {
            int[][] newHistogram = getHistogram(imageView.getImage());
            AreaChart<Number, Number> newHistogramChart = (AreaChart<Number, Number>) histogramPane.getCenter();
            XYChart.Series<Number, Number> brightnessLevelCount = getHistogramSeries(newHistogram, Color.GREY);
            brightnessLevelCount.setName("Brightness");
            
            newHistogramChart.getData().clear();
            newHistogramChart.getData().addAll(new XYChart.Series<>(), brightnessLevelCount);
            newHistogramChart.setLegendVisible(false);
        });
        
        rgbButton.setOnAction(e -> 
        {
            AreaChart<Number, Number> newHistogramChart = (AreaChart<Number, Number>) histogramPane.getCenter();
            
            int[][] newHistogram = getHistogram(imageView.getImage());
            
            //AtomicReference<Integer[][]> atomic = new AtomicReference<>();
            //atomic.set(histogram);
            
            XYChart.Series<Number, Number> redLevelCount = getHistogramSeries(newHistogram, Color.RED);
            redLevelCount.setName("Red Channel");
            XYChart.Series<Number, Number> greenLevelCount = getHistogramSeries(newHistogram, Color.GREEN);
            greenLevelCount.setName("Green Channel");
            XYChart.Series<Number, Number> blueLevelCount = getHistogramSeries(newHistogram, Color.BLUE);
            blueLevelCount.setName("Blue Channel");
            XYChart.Series<Number, Number> brightnessLevelCount = getHistogramSeries(newHistogram, Color.GREY);
            brightnessLevelCount.setName("Brightness");
            
            newHistogramChart.getData().clear();
            newHistogramChart.getData().addAll(redLevelCount, brightnessLevelCount, 
                greenLevelCount, blueLevelCount);
            newHistogramChart.setLegendVisible(true);
        });
        
        equalizationButton.setOnAction(e ->
        {
            int imageSize = (int) (image.getWidth() * image.getHeight());
            int[] brightnessMapping = computeCumulativeDistribution(histogram[4], imageSize);
            imageView.setImage(getEqualizedImage(image, brightnessMapping));
            
            int[][] newHistogram = getHistogram(imageView.getImage());
            histogramPane.setCenter(getHistogramChart(newHistogram));
        });
        
        Scene histogramView = new Scene(histogramPane, 1920, 1080);
        histogramWindow.setScene(histogramView);
        histogramWindow.show();
	}
	
	private int[] computeCumulativeDistribution(int[] histogram, int imageSize)
	{
	    int[] brightnessMapping = new int[BYTE_LIMIT+1];
	    
	    double cumulativeSum = 0;
	    
	    for(int i=0; i <= BYTE_LIMIT; i++)
	    {
	        cumulativeSum += histogram[i];
	        brightnessMapping[i] = (int)(BYTE_LIMIT * (cumulativeSum/imageSize));
	    }
	    
	    return brightnessMapping;
	}
	
	private Image getEqualizedImage(Image image, int[] brightnessMapping)
	{
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        
        // Create a new image of that width and height
        WritableImage equalizedImage = new WritableImage(width, height);
        
        // Get an interface to write to that image memory
        PixelWriter equalizedImageWriter = equalizedImage.getPixelWriter();
        // Get an interface to read from the original image passed as the
        // parameter to the function
        PixelReader imageReader = image.getPixelReader();

        // Iterate over all pixels
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
               Color currentColor = imageReader.getColor(x, y);
               
               double hue = currentColor.getHue();
               double saturation = currentColor.getSaturation();
               int oldValue = (int)(currentColor.getBrightness()*BYTE_LIMIT);
               double newValue = brightnessMapping[oldValue]/255.0;
               
               Color newColor = Color.hsb(hue, saturation, newValue);
               equalizedImageWriter.setColor(x, y, newColor);
            }
        }
        
        return equalizedImage;
	}
	
	private Image getCrossCorelatedImage(Image image)
	{
	    int[][] pixelSums = new int[(int)image.getHeight()][(int)image.getWidth()];
	    
	    int max = Integer.MIN_VALUE;
	    int min = Integer.MAX_VALUE;
	    
	    /*I am doing cross correlation on the grey scale version of the image, 
	     * so I average the RGB channels of a pixel before applying the
	     * algorithm.*/
	    for(int y=0; y<image.getHeight(); y++)
	    {
	        for(int x=0; x<image.getWidth(); x++)
	        {
	            if(canFilterBeCentredOn(image, x, y))
	            {
	                pixelSums[y][x] = getPixelProductSum(image, x, y);
	                
	                if(pixelSums[y][x] > max)
	                {
	                    max = pixelSums[y][x];
	                }
	                
	                if(pixelSums[y][x] < min)
	                {
	                    min = pixelSums[y][x];
	                }
	            }
	            else
	            {
	                pixelSums[y][x] = 0;
	            }
	        }
	    }
	    
	    System.out.println("max "+max+"min "+min);
	    int halfOfFilterSize = laplacianFilter.length/2;
	    for(int y = halfOfFilterSize; y < image.getHeight() - halfOfFilterSize; y++)
	    {
	        for(int x = halfOfFilterSize; x < image.getWidth() - halfOfFilterSize; x++)
	        {
	            int normalisedValue = (pixelSums[y][x] - min)*BYTE_LIMIT/(max - min);
	            pixelSums[y][x] = normalisedValue;
	        }
	    }
	    
	    
	    return getGreyScaleImage(pixelSums);
	}
	
	private Image getGreyScaleImage(int[][] pixelValues)
	{
        WritableImage greyImage = new WritableImage(pixelValues[0].length, 
            pixelValues.length);
        
        PixelWriter imageWriter = greyImage.getPixelWriter();
        
        for(int y = 0; y < pixelValues.length; y++)
        {
            for(int x = 0; x < pixelValues[0].length; x++)
            {
                Color newColor = Color.rgb(pixelValues[y][x], pixelValues[y][x],
                    pixelValues[y][x]);
                imageWriter.setColor(x, y, newColor);
            }
        }
        
        return greyImage;
	}
	
	private int getPixelProductSum(Image image, int x, int y)
	{
	    int topLeftX = x - laplacianFilter.length/2;
	    int topLeftY = y - laplacianFilter.length/2;
	    PixelReader imageReader = image.getPixelReader();
	    int sum = 0;
	    
	    for(int i = 0; i < laplacianFilter.length; i++)
	    {
	        for(int j = 0; j < laplacianFilter.length; j++)
	        {
	            Color pixelColor = imageReader.getColor(topLeftX + i, topLeftY + j);
	            int brightness = (int)(BYTE_LIMIT*(pixelColor.getBlue() + 
	                    pixelColor.getRed() + pixelColor.getGreen())/3);
	            sum += brightness * laplacianFilter[i][j];
	        }
	    }
	    
	    return sum;
	}
	
	private boolean canFilterBeCentredOn(Image image, int x, int y)
	{
	    int halfLengthOfFilter = laplacianFilter.length/2;
	    if(x < halfLengthOfFilter || y < halfLengthOfFilter)
	    {
	        return false;
	    }
	    else if(x >= image.getWidth() - halfLengthOfFilter || 
	            y >= image.getHeight() - halfLengthOfFilter)
	    {
	        return false;
	    }
	    else
	    {
	        return true;
	    }
	}
}
