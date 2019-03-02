
/**
 * Name: Alexandru Dascalu
 * Student ID 965337
 * @author Alexandru Dascalu
 * 
 * I declare that this class is my own piece of work, and that I did not copy any
 * of the functions that change the graphics of the image from a colleague or 
 * from the Internet, nor have I looked at any piece of code of any of my coursemates.
 */
public class HSVColor 
{
    private double hue;
    private final double saturation;
    private final double value;
    
    public HSVColor(double red, double green, double blue)
    {
        if(red < 0 || red > 1 || green < 0 || green > 1 || 
                blue < 0 || blue > 1)
        {
            throw new IllegalArgumentException();
        }
        
        double min;
        double max;
        double delta;
        
        min = Math.min(Math.min(red, green), blue);
        max = Math.max(Math.max(red, green), blue);
        
        value = max;
        delta = max - min;
        
        if(delta == 0)
        {
            saturation = 0;
            hue = 0;
        }
        else
        {
            saturation = delta/max;
            
            if(max == red)
            {
                hue = ((green - blue)/delta) % 6;
            }
            else if(max == green)
            {
                hue = (blue - red)/delta + 2;
            }
            else
            {
                hue = (red - green)/delta + 4;
            }
            
            hue *= 60.0;
            
            if(hue < 0)
            {
                hue += 360;
            }
        }
    }
    
    public double getHue()
    {
        return hue;
    }
    
    public double getSaturation()
    {
        return saturation;
    }
    
    public double getValue()
    {
        return value;
    }
}
