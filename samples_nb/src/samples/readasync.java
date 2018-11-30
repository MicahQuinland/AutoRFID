/**
 * Sample program that reads tags in the background and prints the
 * tags found.
 */

// Import the API
package samples;
import com.thingmagic.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class readasync
{
    static SerialPrinter serialPrinter;
  static StringPrinter stringPrinter;
  static TransportListener currentListener;

  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "  (URI: 'tmr:///COM1 --ant 1,2' or 'tmr://astra-2100d3/ --ant 1,2' "
                + "or 'tmr:///dev/ttyS0 --ant 1,2')\n\n");
    System.exit(1);
  }

   public static void setTrace(Reader r, String args[])
  {
    if (args[0].toLowerCase().equals("on"))
    {
        r.addTransportListener(Reader.simpleTransportListener);
        currentListener = Reader.simpleTransportListener;
    }
    else if (currentListener != null)
    {
        r.removeTransportListener(Reader.simpleTransportListener);
    }
  }

   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.print(tx ? "Sending: " : "Received:");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n         ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
  }

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }
  public static void main(String argv[])
  {
    // Program setup
    Reader r = null;
    int[] antennaList = new int[] {1};

    // Create Reader object, connecting to physical device
    try
    {
        boolean incorrectPort = true;
        String port = "tmr:///COM";
        System.out.println("Trying to find port....");
        int portIndex = 1;
        
            while (incorrectPort && portIndex < 20) {
                        
            String portName = port + Integer.toString(portIndex);
            try {
                r = Reader.create(portName);
                r.connect();
                incorrectPort = false;
                System.out.println(portName);
            } catch(ReaderException re) {
            	portIndex++;
            }
            catch (Exception e) {
                portIndex++;
            }
                
            }
            
        if (Reader.Region.UNSPEC == (Reader.Region)r.paramGet("/reader/region/id"))
        {
            Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
            if (supportedRegions.length < 1)
            {
                 throw new Exception("Reader doesn't support any regions");
            }
            else
            {
                 r.paramSet("/reader/region/id", supportedRegions[0]);
            }
        }
        
        String rPower = r.paramGet("/reader/radio/readPower").toString();
        System.out.println("Old read power: " +rPower);
        
        r.paramSet("/reader/radio/readPower", -500);
        
        
        rPower = r.paramGet("/reader/radio/readPower").toString();
        System.out.println("New read power: " +rPower);
        
        String rTemp = r.paramGet("/reader/radio/temperature").toString();
        System.out.println("Reader Temp: " +rTemp);

        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
        ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
        r.addReadExceptionListener(exceptionListener);
        // Create and add tag listener
        ReadListener rl = new PrintListener();
        r.addReadListener(rl);
        // search for tags in the background
        r.startReading();
        
        System.out.println("Tap Badge");
        Thread.sleep(5000);
        
        r.stopReading();

        r.removeReadListener(rl);
        r.removeReadExceptionListener(exceptionListener);

        // Shut down reader
        r.destroy();
    } 
    catch (ReaderException re)
    {
      System.out.println("ReaderException: " + re.getMessage());
    }
    catch (Exception re)
    {
        System.out.println("Exception: " + re.getMessage());
    }
}


  static class PrintListener implements ReadListener
  {
    public void tagRead(Reader r, TagReadData tr)
    {
    	String tag = tr.toString();
    	String[] split = tag.split(" ");  	
    	String temp = split[0];
    	
    	String[] split2 = temp.split(":");
    	String hexNum = split2[1];
    	System.out.println("ßßß%"+ hexToAScii(hexNum));
    	System.exit(0);
    }

  }
  
  public static String hexToAScii(String hexStr){ //Micah

      StringBuilder output = new StringBuilder("");
  
      for (int i = 0; i < hexStr.length(); i+=2) {
          String str = hexStr.substring(i,i + 2);
          output.append((char) Integer.parseInt(str, 16));
      }
  
      return output.toString();
  
    }

  static class TagReadExceptionReceiver implements ReadExceptionListener
  {
        String strDateFormat = "M/d/yyyy h:m:s a";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        public void tagReadException(com.thingmagic.Reader r, ReaderException re)
        {
            String format = sdf.format(Calendar.getInstance().getTime());
            System.out.println("Reader Exception: " + re.getMessage() + " Occured on :" + format);
            if(re.getMessage().equals("Connection Lost"))
            {
                System.exit(1);
            }
        }
    }
  
}
