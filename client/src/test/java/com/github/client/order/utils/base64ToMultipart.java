package com.github.client.order.utils;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

/**
 * @author: JianLei
 * @date: 2020/6/15 10:03 下午
 * @description:
 */
public class base64ToMultipart {

  public static MultipartFile base64ToMultipart(String base64) {
    try {
//      String[] baseStrs = base64.split(",");

      BASE64Decoder decoder = new BASE64Decoder();
      byte[] b = new byte[0];
      b = decoder.decodeBuffer(base64);

      for (int i = 0; i < b.length; ++i) {
        if (b[i] < 0) {
          b[i] += 256;
        }
      }
      return new BASE64DecodedMultipartFile(b, base64);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // 测试
  public static void main(String[] args) throws IOException {
    MultipartFile multipartFiles[] = new MultipartFile[2];
    InputStream inputStream = null;
      byte[] buffer = null;
    try {

      String path = "/Users/shijianlei/Desktop/fabian-wiktor-e-imPaYqV1s-unsplash.jpg";
      File file = new File(path);
      inputStream = new FileInputStream(file);
      int count = 0;
      while (count == 0) {

        count = inputStream.available();
      }


      buffer = new byte[count];
      inputStream.read(buffer);

    } catch (Exception e) {
      System.out.println("出现异常"+e);

    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }

    String encode = new BASE64Encoder().encode(buffer);
    System.out.println("----->>>"+encode);
    multipartFiles[0] = base64ToMultipart.base64ToMultipart(encode);
    System.out.println(multipartFiles[0].getOriginalFilename());
  }
}
