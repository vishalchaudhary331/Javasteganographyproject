package com.vishal.steg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ImageSteganography {
    public static void encode(String inputImagePath, String outputImagePath, String message) throws IOException {
        BufferedImage image = ImageIO.read(new File(inputImagePath));
        if (image == null) throw new IOException("Cannot read input image");

        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int msgLen = msgBytes.length;
        int width = image.getWidth(), height = image.getHeight();
        int capacity = (width * height * 3) / 8;

        if (msgLen + 4 > capacity) throw new IllegalArgumentException("Message too long for this image");

        byte[] data = new byte[4 + msgLen];
        data[0] = (byte)((msgLen >> 24) & 0xFF);
        data[1] = (byte)((msgLen >> 16) & 0xFF);
        data[2] = (byte)((msgLen >> 8) & 0xFF);
        data[3] = (byte)(msgLen & 0xFF);
        System.arraycopy(msgBytes, 0, data, 4, msgLen);

        int dataIndex = 0, bitIndex = 7;
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int[] colors = {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
                for (int c = 0; c < 3; c++) {
                    if (dataIndex >= data.length) {
                        rgb = (colors[0] << 16) | (colors[1] << 8) | (colors[2]);
                        image.setRGB(x, y, rgb);
                        continue;
                    }
                    int bit = (data[dataIndex] >> bitIndex) & 1;
                    colors[c] = (colors[c] & 0xFE) | bit;
                    bitIndex--;
                    if (bitIndex < 0) {
                        bitIndex = 7;
                        dataIndex++;
                    }
                }
                rgb = (colors[0] << 16) | (colors[1] << 8) | (colors[2]);
                image.setRGB(x, y, rgb);
                if (dataIndex >= data.length) break outer;
            }
        }
        ImageIO.write(image, "png", new File(outputImagePath));
        System.out.println("Message encoded into: " + outputImagePath);
    }

    public static String decode(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        if (image == null) throw new IOException("Cannot read image");

        int width = image.getWidth(), height = image.getHeight();
        byte[] lenBytes = new byte[4];
        int dataIndex = 0, bitIndex = 7, byteAccumulator = 0, messageLength = -1;
        byte[] messageBytes = null;
        int collectedBytes = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int[] colors = {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
                for (int c = 0; c < 3; c++) {
                    int bit = colors[c] & 1;
                    byteAccumulator = (byteAccumulator << 1) | bit;
                    bitIndex--;
                    if (bitIndex < 0) {
                        if (dataIndex < 4) {
                            lenBytes[dataIndex++] = (byte) byteAccumulator;
                            if (dataIndex == 4) {
                                messageLength = ((lenBytes[0] & 0xFF) << 24)
                                        | ((lenBytes[1] & 0xFF) << 16)
                                        | ((lenBytes[2] & 0xFF) << 8)
                                        | (lenBytes[3] & 0xFF);
                                messageBytes = new byte[messageLength];
                            }
                        } else {
                            messageBytes[collectedBytes++] = (byte) byteAccumulator;
                            if (collectedBytes == messageLength) break outer;
                        }
                        byteAccumulator = 0;
                        bitIndex = 7;
                    }
                }
            }
        }
        if (messageBytes == null) return "";
        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
