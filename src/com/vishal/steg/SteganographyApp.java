package com.vishal.steg;

import java.io.IOException;
import java.util.Scanner;

public class SteganographyApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== Java Steganography Project =====");
        System.out.println("1. Encode Message into image");
        System.out.println("2. Decode Message from image");
        System.out.print("Choose option (1 or 2): ");
        int choice = sc.nextInt();
        sc.nextLine();
        try {
            if (choice == 1) {
                System.out.print("Enter input image path (e.g. input.png): ");
                String input = sc.nextLine();
                System.out.print("Enter output image path (e.g. output.png): ");
                String output = sc.nextLine();
                System.out.print("Enter message to hide: ");
                String msg = sc.nextLine();
                ImageSteganography.encode(input, output, msg);
            } else if (choice == 2) {
                System.out.print("Enter image path to decode (e.g. output.png): ");
                String path = sc.nextLine();
                String message = ImageSteganography.decode(path);
                System.out.println("Hidden message: " + message);
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }
}
