package es.ui1;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class Client extends Thread {

  private static final Scanner sc = new Scanner(System.in);
  private static final int BLOCK_SIZE = 16;
  private static final byte STX = 0x02;
  private static final byte ETX = 0x03;
  private final DataOutputStream out;
  private final MessageDigest md;

  Client(String host, int portNumber) throws IOException, NoSuchAlgorithmException {
    Socket socket = new Socket(host, portNumber);
    this.out = new DataOutputStream(socket.getOutputStream());
    this.md = MessageDigest.getInstance("SHA-256");
  }

  @Override
  public void run() {
    System.out.print("Introduce ruta del fichero: ");
    try {
      String filePath = sc.next();
      File file = new File(filePath);
      FileInputStream fis = new FileInputStream(file);
      byte[] dataBytes = new byte[(int) file.length()];
      fis.read(dataBytes);
      fis.close();
      byte[] hashBytes = md.digest(dataBytes);
      out.writeByte(STX);
      out.write(hashBytes);
      out.writeByte(ETX);
      for (int i = 0; i < dataBytes.length; i += BLOCK_SIZE) {
        int end = Math.min(i + BLOCK_SIZE, dataBytes.length);
        byte[] block = Arrays.copyOfRange(dataBytes, i, end);
        byte lrc = calculateLRC(block);
        out.writeByte(STX);
        out.write(block);
        out.writeByte(ETX);
        out.writeByte(lrc);
        System.out.println(Arrays.toString(block));
        System.out.println(lrc);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private static byte calculateLRC(byte[] data) {
    int sum = 0;
    for (byte b : data) {
      sum += b;
    }
    return (byte) ((sum ^ 0xff) + 1);
  }

}
