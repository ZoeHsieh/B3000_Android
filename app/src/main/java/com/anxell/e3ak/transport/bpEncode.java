package com.anxell.e3ak.transport;






/*This class support data conversion function.
 * it can convert data to Hex.
 * 
 * */

import java.io.UnsupportedEncodingException;

public class bpEncode {
	//static boolean error_hex = false;




	public String decToHex(int dec) {
		 final int sizeOfIntInHalfBytes = 3;
		 final int numberOfBitsInAHalfByte = 4;
		 final int halfByte = 0x0F;
		 final char[] hexDigits = {
				'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
		};
		StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
		hexBuilder.setLength(sizeOfIntInHalfBytes);
		for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i)
		{   //Log.e("test","dec="+dec);
			int j = dec & halfByte;
			//Log.e("test","j="+j);
			hexBuilder.setCharAt(i, hexDigits[j]);
			dec >>= numberOfBitsInAHalfByte;
		}
		return hexBuilder.toString();
	}

	public int getUnsignedByte (short data){
		return data&0x0FFFF ;
	}
	public int getUnsignedByte (byte data){

		return data&0x0FF ;
	}

	public int getUnsignedTwoByte (byte data[]){
		if(data.length == 2) {

			int dataInt = (data[0]&0xFF)*256 + (data[1] &0xFF);

			return dataInt;
		}else
			return -1;
	}
	// This method use to the read characteristic.
	public String ASCIItoHexString(String Data)
	{  
		String t_result = null;
		if (Data != null) {
			char[]  Data_chars= Data.toCharArray();

			StringBuffer hex = new StringBuffer();
			for (int i = 0; i <Data_chars.length; i++) 
			hex.append(Integer.toHexString((int) Data_chars[i]));
			
			 t_result = hex.toString();
			 if(t_result.length()<2)
			 t_result = "0"+t_result;
			 t_result = t_result.toUpperCase();
		
			return  t_result;
		}
		return "error";
	}
	
	// This method use to the write characteristic.
	public byte[] HexStringToBytes( String hexData)
	{
		String tmp;
		tmp = hexData;
		 // every two letters in the string are one byte finally
		byte[] buffer = new byte[tmp.length() / 2];
		String tmp_p = "";
		try {
			for (int i = 0; i <buffer.length; ++i) {
				tmp_p = "0x" + tmp.substring(i * 2, i * 2 + 2);
				buffer[i] = Long.decode(tmp_p).byteValue();
			}
		} catch (NumberFormatException e) {
			//error_hex = true;
			tmp = ASCIItoHexString(tmp);
			byte[]  buffer2 = new byte[tmp.length() / 2];
			try {
				for (int i = 0; i <  buffer2.length; ++i) {
					tmp_p = "0x" + tmp.substring(i * 2, i * 2 + 2);
					 buffer2[i] = Long.decode(tmp_p).byteValue();
				}
			} catch (NumberFormatException i) {

			}
			return  buffer2;
		}

		return buffer;
	}


    //This method can transform hex to ASCII
	public String convert_ASCII(String Data)
	{
		String t_result = null;
		StringBuffer Buffer = new StringBuffer();
		for (int i = 0; i < Data.length(); i += 2) {
			t_result= Data.substring(i, i + 2);
			//Log.e("encodetool","data="+t_result);
			Buffer.append((char) Integer.parseInt(t_result, 16));
		}
		t_result= Buffer.toString();
		return t_result;
	}
	  
	public String BytetoHexString(byte []Rawdata)
	{   String Hexdata = "";
	  if (Rawdata != null &&Rawdata.length > 0) {
		final StringBuilder buffer = new StringBuilder(Rawdata.length);
		for (int i=0 ;i<Rawdata.length;i++) 
		buffer.append(String.format("%02x", Rawdata[i]));
		
		Hexdata = buffer.toString();
		Hexdata = Hexdata.toUpperCase();
		
	            }
	  return  Hexdata;
	}
	
	/*Serial Line Interface  protocol -SLIP */
    private final char  SLIP_END = 0xC0; //0xC0 0300indicates end of packet
	private final char  SLIP_ESC = 0xDB;  //0xDB 0333indicates byte stuffing
	private final char  SLIP_ESC_END = 0xDC;//0xDC 0334ESC ESC_END means END data byte
	private final char  SLIP_ESC_ESC = 0xDD;//0xDD 0335ESC ESC_ESC means ESC data byte

	
	
	public byte []SLIP_Encode(byte data[],int datalen)
	{

		byte packet[] = new byte [datalen+2];
             packet[0] = (byte)SLIP_END;
 			//Log.e("Sean","packet head="+String.format("%02x",(byte)SLIP_END));
		 for (int i = 1; i <=datalen; i ++) {

	          switch (data[i-1]) {
	          
		     /* case (byte)SLIP_END:
				  		packet[i] = (byte)SLIP_ESC;
				        if(i+1 < datalen+2)
				  			packet[i+1] = (byte)SLIP_ESC_END;

		      break;			
		      
		      case (byte)SLIP_ESC:

				  		packet[i] = (byte)SLIP_ESC;
				  		if(i+1 < datalen+2)
							packet[i+1] = (byte)SLIP_ESC_ESC;
		      break;*/
		      
		      default:
		        packet[i] = data[i-1];
			  }
	        
	       
		 }

		packet[datalen+1] = (byte)SLIP_END;
		return packet;
	} 
	
	public byte [] SLIP_Decode(byte packet[], int packetlen)
	{
		
		byte data[] =new byte [packetlen-2];
		int received = 0;
		/* sit in a loop reading bytes until we put together
		* a whole packet.
		* Make sure not to copy them into the packet if we
		* run out of room.
		*/
		for(int i = 0; i < packetlen; i ++)
		{   /* get a character to process
			*/ //Log.e("Sean","packet["+ i +"]="+String.format("%02x",packet[i]));
			   switch(packet[i]) {

			   /* if it's an END character then we're done with
				* the packet
				*/
			   case (byte)SLIP_END:
					   /* a minor optimization: if there is no
						* data in the packet, ignore it. This is
						* meant to avoid bothering IP with all
						* the empty packets generated by the
						* duplicate END characters which are in
						* turn sent to try to detect line noise.
						*/				
	                     if(received>0)
	                    	 return data;
						  else  
				             break;

			   /* if it's the same code as an ESC character, wait
				* and get another character and then figure out
				* what to store in the packet based on that.
				*/
			   case (byte)SLIP_ESC:
					   /* if "c" is not one of these two, then we
						* have a protocol violation.  The best bet
						* seems to be to leave the byte alone and
						* just stuff it into the packet
						*/
					   switch(packet[i]) {
						   case (byte)SLIP_ESC_END:
							       data [i]= (byte)SLIP_END;
								   break;
						   case (byte)SLIP_ESC_ESC:
							       data [i]= (byte)SLIP_ESC;
								   break;
						
					   }
					   break;

			   /* here we fall into the default handler and let
				* it store the character for us
				*/
			   default:
					if(received < packetlen)
				    {
						//Log.e("Sean","data["+ received +"]="+String.format("%02x",data[received]));
						data[received] = packet[i];
				      received ++;
				    }
			   }
	      }
     	
		
		
		return data;
	}
	public String toUtf8(String str) {
		String result = null;
		try {
			result = new String(str.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
