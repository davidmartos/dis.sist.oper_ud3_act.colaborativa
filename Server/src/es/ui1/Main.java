package es.ui1;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);

        try {
            new Server(portNumber).start();
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
