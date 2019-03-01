
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
import java.util.concurrent.atomic.AtomicReference;

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

public class Photoshop extends Application
{
	private static final int BYTE_LIMIT = 255;

	private int[] gammaLookupTable;

	private int[] contrastLookupTable;
	        
	private static final int[][] laplacianFilter = {{-4,-1,0,-1,-4}, 
	                                                {-1,2,3,2,-1}, 
	                                                {0,3,4,3,0}, 
	                                                {-1,2,3,2,-1}, 
	                                                {-4,-1,0,-1,-4}};
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
		//imageView.setFitWidth(1000);
		//imageView.setFitHeight(800);
		
		// Create the simple GUI
		Button invert_button = new Button("Invert");
		Button gamma_button = new Button("Gamma Correct");
		Button contrast_button = new Button("Contrast Stretching");
		Button histogramButton = new Button("Histograms");
		Button cc_button = new Button("Cross Correlation");
		Button resetButton = new Button("Reset Image");
		Button setContrastValueBtn = new Button("Set contrast values");

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
			    showGammaInput(imageView, image);
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
				showHistograms(imageView);
			}
		});

		cc_button.setOnAction(new EventHandler<ActionEvent>()
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

		// Using a flow pane
		BorderPane root = new BorderPane();

		HBox topElements = new HBox(5);
		topElements.setAlignment(Pos.CENTER);
		// Gaps between buttons
		
		// Add all the buttons and the image for the GUI
		topElements.getChildren().addAll(invert_button, gamma_button, contrast_button,
				histogramButton, cc_button, resetButton);

		VBox inputs = new VBox(5);
		inputs.getChildren().addAll(setContrastValueBtn);
		
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
	
	public void showGammaInput(ImageView imageView, Image originalImage)
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
        
        Text validInputText = new Text("Invalid Gamma Input! Type in a floating point number!");
        validInputText.setVisible(false);
        validInputText.setFill(Color.FIREBRICK);
        
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
        
        int[][] histogram = new int[5][BYTE_LIMIT+1];
        
        for(int i=0; i < 5; i++)
        {
            for(int j=0; j < BYTE_LIMIT+1; j++)
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
                
                HSVColor hsvColor = new HSVColor(color.getRed(), color.getGreen(), color.getBlue());
                int value = (int)(hsvColor.getValue() * BYTE_LIMIT);
                histogram[4][value]++;
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
	
	public AreaChart<Number, Number> getHistogramChart(int[][] histogram)
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
	
	public void showHistograms(ImageView imageView)
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
	
	public Image getEqualizedImage(Image image, int[] brightnessMapping)
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
	
	public Image getCrossCorelatedImage(Image image)
	{
	    int[][] pixelSums = new int[(int)image.getHeight()][(int)image.getWidth()];
	    
	    int max = 0;
	    int min = Integer.MAX_VALUE;
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
	                pixelSums[y][x] = BYTE_LIMIT;
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
	
	public Image getGreyScaleImage(int[][] pixelValues)
	{
	    // Create a new image of that width and height
        WritableImage greyImage = new WritableImage(pixelValues[0].length, 
            pixelValues.length);
        
        // Get an interface to write to that image memory
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
	
	public int getPixelProductSum(Image image, int x, int y)
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
	
	public boolean canFilterBeCentredOn(Image image, int x, int y)
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
	
	public static void main(String[] args)
	{
		launch();
	}
}
