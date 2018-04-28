package cn.ymcd.web.util;

import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class DesSecurity
{
  BASE64Encoder encoder;
  BASE64Decoder decoder;
  Cipher enCipher;
  Cipher deCipher;

  public DesSecurity(String key, String iv)
    throws Exception
  {
    if (key == null)
      throw new NullPointerException("Parameter is null!");
    InitCipher(key.getBytes(), iv.getBytes());
  }

  private void InitCipher(byte[] secKey, byte[] secIv) throws Exception
  {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(secKey);

    DESKeySpec dsk = new DESKeySpec(md.digest());

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    SecretKey key = keyFactory.generateSecret(dsk);

    IvParameterSpec iv = new IvParameterSpec(secIv);
    AlgorithmParameterSpec paramSpec = iv;

    this.enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    this.deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

    this.enCipher.init(1, key, paramSpec);
    this.deCipher.init(2, key, paramSpec);

    this.encoder = new BASE64Encoder();
    this.decoder = new BASE64Decoder();
  }

  public String encrypt64(byte[] data) throws Exception
  {
    return this.encoder.encode(this.enCipher.doFinal(data));
  }

  public byte[] decrypt64(String data) throws Exception
  {
    return this.deCipher.doFinal(this.decoder.decodeBuffer(data));
  }

}
