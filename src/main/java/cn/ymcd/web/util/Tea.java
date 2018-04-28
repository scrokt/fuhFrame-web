package cn.ymcd.web.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Tea {
    
    private final static int[] KEY = new int[]{
    	0xd47a6cb2, 0xa75c64ab,
    	0xf49e21b6, 0xc36ad84d
    };
    
    private final static int[] KEY1 = new int[]{
        0xdc8811cb, 0xea7711ca,
        0xac9911ba, 0xdf6611ab
    };
  
    public static byte[] encrypt(byte[] content, int offset, int[] key, int times){//timesΪ��������
        int[] tempInt = byteToInt(content, offset);
        int y = tempInt[0], z = tempInt[1], sum = 0, i;
        int delta=0x9e3779b9;
        int a = key[0], b = key[1], c = key[2], d = key[3]; 

        for (i = 0; i < times; i++) {   
            
            sum += delta;
            y += ((z<<4) + a) ^ (z + sum) ^ ((z>>>5) + b);
            z += ((y<<4) + c) ^ (y + sum) ^ ((y>>>5) + d);
        }
        tempInt[0]=y;
        tempInt[1]=z; 
        return intToByte(tempInt, 0);
    }
    
    public static byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times){
        int[] tempInt = byteToInt(encryptContent, offset);
        int v0=tempInt[0],v1=tempInt[1];
    	for(int i=0,sum=0xc6ef3720,k0=key[0],k1=key[1],k2=key[2],k3=key[3];i<32;++i)
    	{
    		v1-=((v0<<4)+k2)^(v0+sum)^((v0>>>5)+k3);
    		v0-=((v1<<4)+k0)^(v1+sum)^((v1>>>5)+k1);
    		sum-=0x9e3779b9;
    	}
    	tempInt[0]=v0;
    	tempInt[1]=v1;

        return intToByte(tempInt, 0);
    }

    private static int[] byteToInt(byte[] content, int offset){
        int[] result = new int[(content.length >> 2)];
        for(int i = 0, j = offset; j < content.length; i++, j += 4){
            result[i] = transform(content[j]) | transform(content[j + 1]) << 8 |
            transform(content[j + 2]) << 16 | transform(content[j + 3]) << 24;
        }
        return result;
        
    }
    
    private static byte[] intToByte(int[] content, int offset){
        byte[] result = new byte[(content.length << 2)];
        for(int i = 0, j = offset; j < result.length; i++, j += 4){
            result[j + 0] = (byte)(content[i] & 0xff);
            result[j + 1] = (byte)((content[i] >> 8) & 0xff);
            result[j + 2] = (byte)((content[i] >> 16) & 0xff);
            result[j + 3] = (byte)((content[i] >> 24) & 0xff);
        }
        return result;
    }

    private static int transform(byte temp){
        int tempInt = (int)temp;
        if(tempInt < 0){
            tempInt += 256;
        }
        return tempInt;
    }

    public static String decryptByTea(byte[] secretInfo, int[] key){
        String dataStr = "";
    	if(secretInfo.length % 8 != 1){
    		return null;
    	} 
    	byte n = secretInfo[0];
        byte[] decryptStr = null;
        byte[] tempDecrypt = new byte[secretInfo.length -1];
        for(int offset = 1; offset < secretInfo.length; offset += 8){
            decryptStr = decrypt(secretInfo, offset, key, 32);
            System.arraycopy(decryptStr, 0, tempDecrypt, offset - 1, 8);
        }
        
        try {
            dataStr = new String(tempDecrypt, 0, tempDecrypt.length - n, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return dataStr;
    }
    
    public static String decryptByTea1(String secretInfo) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            return decryptByTea(decoder.decodeBuffer(secretInfo), KEY1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptByTea(byte[] inputInfo, int[] key){
    	int i = 8 - inputInfo.length%8;
    	byte[] inputStream = new byte[inputInfo.length+i];
    	for(int j=0; j<inputStream.length; j++)
    		inputStream[j] = 0;
    	System.arraycopy(inputInfo, 0, inputStream, 0, inputInfo.length);

    	byte n = (byte)i;

    	byte[] tempEncrypt = new byte[inputStream.length + 1];
    	tempEncrypt[0]=n;
    	
    	byte[] ebcryptStr = null;
        for(int offset = 0; offset < inputStream.length; offset += 8){
        	ebcryptStr = encrypt(inputStream, offset, key, 32);
            System.arraycopy(ebcryptStr, 0, tempEncrypt, offset + 1, 8);
        }
    	
        return tempEncrypt;
    }

    public static String decryptByTea(String secretInfo) {
    	BASE64Decoder decoder = new BASE64Decoder();
		try {
			return decryptByTea(decoder.decodeBuffer(secretInfo), KEY);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static String encryptByTea(String inputInfo) {
    	BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encodeBuffer(encryptByTea(inputInfo.getBytes(), KEY));
    }
    
    public static String encryptByTea1(String inputInfo) {
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return encoder.encodeBuffer(encryptByTea(inputInfo.getBytes("utf-8"), KEY1));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // should never get here
        return encoder.encodeBuffer(encryptByTea(inputInfo.getBytes(), KEY1));
    }
    
    public static void main(String args[]) {
        String encrypt = Tea.encryptByTea("c32323BB@sina.com");
        System.out.println(Tea.decryptByTea(encrypt));
    } 
}