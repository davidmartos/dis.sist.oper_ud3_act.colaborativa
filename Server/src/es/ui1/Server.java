package es.ui1;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Server extends Thread {

  private static final int STX = 0x02;
  private static final int ETX = 0x03;
  private static final int BLOCK_SIZE = 16;

  private final ServerSocket serverSocket;
  private final MessageDigest md;

  Server(int portNumber) throws IOException, NoSuchAlgorithmException {
    this.serverSocket = new ServerSocket(portNumber);
    this.md = MessageDigest.getInstance("SHA-256");
  }

  @Override
  public void run() {
    try {
      System.out.println("- Esperando conexión al servidor...");
      Socket socket = serverSocket.accept();
      DataInputStream dis = new DataInputStream(socket.getInputStream());
      System.out.println("-- Cliente conectado al servidor");
      byte[] hash = readData(dis);
      ByteArrayOutputStream fileData = new ByteArrayOutputStream();
      while (true) {
        byte[] block = readData(dis);
        checkLRC(dis, block);
        fileData.write(block);
        if (block.length < BLOCK_SIZE) {
          break;
        }
      }
      byte[] calculatedHash = md.digest(fileData.toByteArray());
      if (!MessageDigest.isEqual(hash, calculatedHash)) {
        System.out.println("Error: hash incorrecto");
      }
      System.out.println("--- Recibido correctamente: \n" + fileData);
    } catch (IOException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private static byte[] readData(DataInputStream dataInput) throws IOException {
    if (dataInput.readByte() != STX) {
      throw new IOException("STX no encontrado");
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte b;
    while ((b = dataInput.readByte()) != ETX) {
      buffer.write(b);
    }

    return buffer.toByteArray();
  }

  private static void checkLRC(DataInputStream dis, byte[] bytes) throws IOException {
    byte lrc = dis.readByte();
    if (calculateLRC(bytes) != lrc) {
      throw new IOException("LRC no es correcto");
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
