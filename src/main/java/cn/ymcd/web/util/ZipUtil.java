/**   
 * @Title: ZipUtil.java
 * @Package cn.ymcd.zip
 * @Description: TODO(用一句话描述该文件做什么)
 * @author wangjf wangjf@nccd.com.cn   
 * @date 2015-9-16 下午01:32:16
 * @version V1.0   
 */
package cn.ymcd.web.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName: ZipUtil
 * @Description: TODO(ZIP压缩解压)
 * @author wangjf wangjf@nccd.com.cn
 * @date 2015-9-16 下午01:32:16
 * 
 */
public class ZipUtil {
    /**
     * 递归压缩文件夹
     * 
     * @param srcRootDir
     *            压缩文件夹根目录的子路径
     * @param file
     *            当前递归压缩的文件或目录对象
     * @param zos
     *            压缩文件存储对象
     * @throws Exception
     */
    private static void zip(String srcRootDir, File file, ZipOutputStream zos) throws Exception {
        if (file == null) {
            return;
        }

        // 如果是文件，则直接压缩该文件
        if (file.isFile()) {
            int count, bufferLen = 1024;
            byte data[] = new byte[bufferLen];

            // 获取文件相对于压缩文件夹根目录的子路径
            String subPath = file.getAbsolutePath();
            int index = subPath.indexOf(srcRootDir);
            if (index != -1) {
                subPath = subPath.substring(srcRootDir.length() + File.separator.length());
            }
            ZipEntry entry = new ZipEntry(subPath);
            zos.putNextEntry(entry);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while ((count = bis.read(data, 0, bufferLen)) != -1) {
                zos.write(data, 0, count);
            }
            bis.close();
            zos.closeEntry();
        }
        // 如果是目录，则压缩整个目录
        else {
            // 压缩目录中的文件或子目录
            File[] childFileList = file.listFiles();
            for (int n = 0; n < childFileList.length; n++) {
                /*
                 * childFileList[n].getAbsolutePath().indexOf( file.getAbsolutePath());
                 */
                zip(srcRootDir, childFileList[n], zos);
            }
        }
    }

    /**
     * 对文件或文件目录进行压缩
     * 
     * @param srcPath
     *            要压缩的源文件路径。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     * @param zipPath
     *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹
     * @param zipFileName
     *            压缩文件名
     * @throws Exception
     */
    public static void zip(String srcPath, String zipPath, String zipFileName) throws Exception {
        if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(zipPath) || StringUtils.isEmpty(zipFileName)) {
            /**
             * 压缩文件不存在，或者解压文件路径不存在，则不处理
             */
        }
        CheckedOutputStream cos = null;
        ZipOutputStream zos = null;
        try {
            File srcFile = new File(srcPath);

            // 判断压缩文件保存的路径是否为源文件路径的子文件夹，如果是，则抛出异常（防止无限递归压缩的发生）
            if (srcFile.isDirectory() && zipPath.indexOf(srcPath) != -1) {
                /**
                 * 压缩文件不存在，或者解压文件路径不存在，则不处理
                 */
                return;
            }

            // 判断压缩文件保存的路径是否存在，如果不存在，则创建目录
            File zipDir = new File(zipPath);
            if (!zipDir.exists() || !zipDir.isDirectory()) {
                zipDir.mkdirs();
            }

            // 创建压缩文件保存的文件对象
            String zipFilePath = zipPath + File.separator + zipFileName;
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                // 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
                SecurityManager securityManager = new SecurityManager();
                securityManager.checkDelete(zipFilePath);
                // 删除已存在的目标文件
                zipFile.delete();
            }

            cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());
            zos = new ZipOutputStream(cos);

            // 如果只是压缩一个文件，则需要截取该文件的父目录
            String srcRootDir = srcPath;
            if (srcFile.isFile()) {
                int index = srcPath.lastIndexOf(File.separator);
                if (index != -1) {
                    srcRootDir = srcPath.substring(0, index);
                }
            }
            // 调用递归压缩方法进行目录或文件压缩
            zip(srcRootDir, srcFile, zos);
            zos.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
                if (cos != null) {
                    cos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压缩zip包
     * 
     * @param zipFilePath
     *            zip文件的全路径
     * @param unzipFilePath
     *            解压后的文件保存的路径
     * @param includeZipFileName
     *            解压后的文件保存的路径是否包含压缩文件的文件名。true-包含；false-不包含
     */
    @SuppressWarnings("unchecked")
    public static void unzip(String zipFilePath, String unzipFilePath, boolean includeZipFileName) throws Exception {
        if (StringUtils.isEmpty(zipFilePath) || StringUtils.isEmpty(unzipFilePath)) {
            /**
             * 压缩文件不存在，或者解压文件路径不存在，则不处理
             */
            return;
        }
        File zipFile = new File(zipFilePath);
        // 如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
        if (includeZipFileName) {
            String fileName = zipFile.getName();
            if (StringUtils.isNotEmpty(fileName)) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            unzipFilePath = unzipFilePath + File.separator + fileName;
        }
        // 创建解压缩文件保存的路径
        File unzipFileDir = new File(unzipFilePath);
        if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
            unzipFileDir.mkdirs();
        }

        // 开始解压
        ZipEntry entry = null;
        String entryFilePath = null, entryDirPath = null;
        File entryFile = null, entryDir = null;
        int index = 0, count = 0, bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)zip.entries();
        // 循环对压缩包里的每一个文件进行解压
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            // 构建压缩包中一个文件解压后保存的文件全路径
            entryFilePath = unzipFilePath + File.separator + entry.getName();
            // 构建解压后保存的文件夹路径
            index = entryFilePath.lastIndexOf(File.separator);
            if (index != -1) {
                entryDirPath = entryFilePath.substring(0, index);
            } else {
                entryDirPath = "";
            }
            entryDir = new File(entryDirPath);
            // 如果文件夹路径不存在，则创建文件夹
            if (!entryDir.exists() || !entryDir.isDirectory()) {
                entryDir.mkdirs();
            }

            // 创建解压文件
            entryFile = new File(entryFilePath);
            if (entryFile.exists()) {
                // 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
                SecurityManager securityManager = new SecurityManager();
                securityManager.checkDelete(entryFilePath);
                // 删除已存在的目标文件
                entryFile.delete();
            }

            // 写入文件
            bos = new BufferedOutputStream(new FileOutputStream(entryFile));
            bis = new BufferedInputStream(zip.getInputStream(entry));
            while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
                bos.write(buffer, 0, count);
            }
            if (null != zip) {
                zip.close();
            }
            bos.flush();
            bos.close();
            bis.close();
        }
    }

    /**
     * 解压文件
     * 
     * @Title: unzip
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @param @param zipFilePath
     * @param @param targetPath
     * @param @throws IOException 设定文件
     * @return void 返回类型
     * @throws
     */
    @SuppressWarnings("unchecked")
    public static void unzip(String zipFilePath, String targetPath) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            String directoryPath = "";
            if (null == targetPath || "".equals(targetPath)) {
                directoryPath = zipFilePath.substring(0, zipFilePath.lastIndexOf("."));
            } else {
                directoryPath = targetPath;
            }
            Enumeration<ZipEntry> entryEnum = (Enumeration<ZipEntry>)zipFile.entries();
            if (null != entryEnum) {
                ZipEntry zipEntry = null;
                while (entryEnum.hasMoreElements()) {
                    zipEntry = (ZipEntry)entryEnum.nextElement();
                    if (zipEntry.isDirectory()) {
                        directoryPath = directoryPath + File.separator + zipEntry.getName();
                        continue;
                    } else {
                        // 文件
                        File targetFile = buildFile(directoryPath + File.separator + zipEntry.getName(), false);
                        os = new BufferedOutputStream(new FileOutputStream(targetFile));
                        is = zipFile.getInputStream(zipEntry);
                        byte[] buffer = new byte[4096];
                        int readLen = 0;
                        while ((readLen = is.read(buffer, 0, 4096)) >= 0) {
                            os.write(buffer, 0, readLen);
                        }

                        os.flush();
                        os.close();
                    }
                }
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (null != zipFile) {
                zipFile.close();
            }
            if (null != is) {
                is.close();
            }
            if (null != os) {
                os.close();
            }
        }
    }

    /**
     * 
     * @Title: buildFile
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @param @param fileName
     * @param @param isDirectory
     * @param @return 设定文件
     * @return File 返回类型
     * @throws
     */
    public static File buildFile(String fileName, boolean isDirectory) {
        File target = new File(fileName);
        if (isDirectory) {
            target.mkdirs();
        } else {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
                target = new File(target.getAbsolutePath());
            }
        }
        return target;
    }

    public static byte[] zip2(String data) {
        return zip2(data.getBytes());
    }

    public static byte[] zip2(byte[] data) {
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    public static byte[] zip(String data) {
        return zip(data.getBytes());
    }

    public static byte[] zip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(data.length);
            zip.putNextEntry(entry);
            zip.write(data);
            zip.closeEntry();
            zip.close();

            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    public static final byte[] zip3(String str) {
        if (str == null)
            return null;

        byte[] compressed;
        ByteArrayOutputStream out = null;
        ZipOutputStream zout = null;

        try {
            // out = new ByteArrayOutputStream();
            // zout = new ZipOutputStream(out);
            // ZipEntry zipEntry = new ZipEntry("zip");
            // zipEntry.setSize(str.getBytes().length);
            // zout.putNextEntry(zipEntry);
            // zout.write(str.getBytes());
            // zout.closeEntry();
            // zout.close();
            // compressed = out.toByteArray();

            out = new ByteArrayOutputStream();
            zout = new ZipOutputStream(out);
            zout.putNextEntry(new ZipEntry("zip"));
            zout.write(str.getBytes());
            zout.closeEntry();
            zout.finish();
            compressed = out.toByteArray();

        } catch (IOException e) {
            compressed = null;
        } finally {
            if (zout != null) {
                try {
                    zout.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        return compressed;
    }

    public static byte[] unZip2(byte[] data) throws Exception {
        byte[] output = new byte[0];
        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            int cnt = 1;
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
                cnt++;
                if (cnt > 10000)
                    break;
            }
            output = o.toByteArray();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
            }
        }
        decompresser.end();
        return output;
    }

    public static byte[] unZip3(byte[] data) {
        byte[] b = null;
        try {
            b = unZip2(data);
        } catch (Exception e) {
            b = unZip(data);
        }
        return b;
    }

    public static byte[] unZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ex) {
            throw new RuntimeException("->unZip失败",ex);
        }
        return b;
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase() + " ");
        }
        return sb.toString();
    }

    public static byte[] encode(byte[] data, byte[] keybyte, byte[] ivbyte) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(keybyte);// 设置密钥参数
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
        Key key = keyFactory.generateSecret(keySpec);// 得到密钥对象
        IvParameterSpec iv = new IvParameterSpec(ivbyte);// 设置向量
        Cipher enCipher = Cipher.getInstance("DES/CBC/NoPadding");// 得到加密对象Cipher
        enCipher.init(Cipher.ENCRYPT_MODE, key, iv);// 设置工作模式为加密模式，给出密钥和向量
        byte[] pasByte = enCipher.doFinal(data);
        return pasByte;
    }

    public static byte[] encode(byte[] data, String keystr, String ivstr) throws Exception {
        return encode(data, keystr.getBytes(), ivstr.getBytes());
    }

    public static byte[] decode(byte[] data, byte[] keybyte, byte[] ivbyte) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(keybyte);// 设置密钥参数
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
        Key key = keyFactory.generateSecret(keySpec);// 得到密钥对象
        IvParameterSpec iv = new IvParameterSpec(ivbyte);// 设置向量
        Cipher deCipher = Cipher.getInstance("DES/CBC/NoPadding");
        deCipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] pasByte = deCipher.doFinal(data);
        byte[] result = new byte[pasByte.length];
        for (int i = pasByte.length - 1; i >= 0; i--) {
            if (pasByte[i] == 68 && pasByte[i - 1] == 67 && pasByte[i - 2] == 66 && pasByte[i - 3] == 65) {
                result = Arrays.copyOfRange(pasByte, 0, i - 7);
                return result;
            }
        }
        return pasByte;
    }

    public static byte[] decode(byte[] data, String keystr, String ivstr) throws Exception {
        return decode(data, keystr.getBytes(), ivstr.getBytes());
    }

    public static void main(String[] args) throws Exception {

        /*
         * String filePath = args[0]; String desKey = args[1]; String desIv = args[2]; File file = new File(filePath);
         * FileInputStream fileInputStream = new FileInputStream(file); byte[] data = new byte[(int)file.length()];
         * fileInputStream.read(data); fileInputStream.close();
         */

        /*
         * byte[] zip2 = zip2(data); byte[] zipData =
         * zip2("迈普通信技术股份有限公司\t740348619\t四川省成都市高新区九兴大道16#迈普大厦\t邓霄博    \t4008868669\tdengxiaobo@mail.maipu.com\r\n");
         * System.out.println("zip:" + toHex(zipData)); byte[] beforeEncode = new byte[(zipData.length / 8 + 2) * 8];
         * byte[] intToBytes = intToBytes(zipData.length); int i = 0; for (; i < zipData.length; i++) { beforeEncode[i]
         * = zipData[i]; } for (int j = 0; j < intToBytes.length; j++, i++) { beforeEncode[i] = intToBytes[j]; }
         * beforeEncode[i++] = 'A'; beforeEncode[i++] = 'B'; beforeEncode[i++] = 'C'; beforeEncode[i++] = 'D'; for (; i
         * < beforeEncode.length; i++) { beforeEncode[i] = '\0'; }
         * 
         * System.out.println("beforeEncode:" + toHex(beforeEncode));
         * 
         * byte[] afterEncode = encode(beforeEncode, "w$@t@$wl", "wlw#t$@a"); System.out.println("afterEncode:" +
         * toHex(afterEncode));
         */

        FileInputStream fileInputStream = new FileInputStream("e:/1005.txt");
        byte[] buffer = new byte[1024 * 10];
        int length = fileInputStream.read(buffer);
        fileInputStream.close();
        // String data =
        // "2rWgDTlYxLU+r91lqvTqSg3DURH338Tj8Gd6ARy/wsEQzsBHULYEcxlXKqEFcLIN6SMHJ2hYxeV5vQRgvZPoCyY/vPZs346kJ22MVQiotAKRCrVbZTRmXQ==";
        // byte[] buffer = data.getBytes();int length=buffer.length;
        byte[] afterEncode = new byte[length - 8];
        byte[] code = new byte[4];
        byte[] leng = new byte[4];
        for (int i = 0; i < 4; i++) {
            code[i] = buffer[i];
        }
        for (int i = 0; i < 4; i++) {
            leng[i] = buffer[i + 4];
        }
        System.out.println(byteArrayToInt(code));
        System.out.println(byteArrayToInt(leng));
        for (int i = 0, j = 8; i < length - 8; i++) {
            afterEncode[i] = buffer[j++];
        }

        System.out.println(afterEncode.length);
        System.out.println(toHex(afterEncode));

        byte[] afterDecode = decode(afterEncode, "m$@w@$dw", "mwd#w$@l");
        System.out.println("afterDecode:" + toHex(afterDecode));
        byte[] unZip3 = unZip3(afterDecode);
        System.out.println("afterUnzip:" + new String(unZip3));
    }

    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte)((value >> 24) & 0xFF);
        src[1] = (byte)((value >> 16) & 0xFF);
        src[2] = (byte)((value >> 8) & 0xFF);
        src[3] = (byte)(value & 0xFF);
        return src;

    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    };

    public static String toHex(byte b) {
        String result = Integer.toHexString(b & 0xFF);
        if (result.length() == 1) {
            result = '0' + result;
        }
        return result;
    }

    public static String toHex(byte[] bs) {
        String all = "";
        for (byte b : bs) {
            String result = Integer.toHexString(b & 0xFF);
            if (result.length() == 1) {
                result = '0' + result;
            }
            all += result;
        }
        return all;
    }
}
