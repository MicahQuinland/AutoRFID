/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thingmagic;



/**
 * Sample programs that pull the autonomous streaming and prints the tags
 * As there is no auto port detection in Java, this sample program expects autonomous
 * streaming port as argument.
 */
public class ReceiveAutonomousReading
{
   
    public static void main(String[] args)
    {
        
        try
        {            
            boolean isConnected = false;
            SerialTransport serialTransport = new SerialTransport();
            isConnected = serialTransport.connect();
            if (isConnected)
            {
                serialTransport.receiveStreaming();
            }           
        }
        catch (ReaderException re)
        {
            re.printStackTrace();
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
