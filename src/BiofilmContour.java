import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import javax.imageio.ImageIO;

public class BiofilmContour {
  
  public static class pair {
    int x,y;
    pair(int a,int b) {
      x = a;
      y = b;
    }
  }
  
  public static void main(String[] args) throws IOException {
    Scanner q = new Scanner(new File("info.txt"));
    File output = new File("output"+q.nextLine()+".txt");
    output.createNewFile();
    PrintStream w = new PrintStream(output);
    double xLength = q.nextDouble(), yLength = q.nextDouble(), zLength = q.nextDouble();
    int imgHeight, imgWidth, PixelValue, total = 0, tubeWidth = q.nextInt(), tubeHeight = q.nextInt(), xTopLeft = q.nextInt(), yTopLeft = q.nextInt();
    int sttImg = q.nextInt(), endImg = q.nextInt(), rndrPrd = q.nextInt(), temp1[][] = new int[2662][2662];
    int adjPixThresh = q.nextInt(), pixBrtThresh = q.nextInt(); q.nextLine();
    String fileName = q.nextLine();
    w.println(fileName + " pictures " + sttImg + " - " + endImg);
    //begin loop
    for (int pic = sttImg;pic <= endImg;pic++) {
      
    String s = String.valueOf(pic);
    //for sets
    while (s.length() < 4) s = '0' + s;
    s = "IM" + s;
    File f = new File(fileName + "\\" + s + ".jpg");
    BufferedImage img = ImageIO.read(f);
    //draw clarification circles
    Graphics graphics = img.getGraphics();
    graphics.setColor(Color.black);
    graphics.drawOval(xTopLeft, yTopLeft, tubeWidth, tubeHeight);
    graphics.drawOval(xTopLeft - 1, yTopLeft, tubeWidth, tubeHeight);
    //ImageIO.write(img, "png", new File(s+" aserrender.png"));
    imgHeight = img.getHeight();
    imgWidth = img.getWidth();
    int BiofilmArray[][] = new int[imgWidth][imgHeight], temp2[] = new int[1000000];
    pair coords;
    Queue<pair> queue = new ArrayDeque<pair>(3000000);
    queue.add(new pair(xTopLeft + tubeWidth / 2, yTopLeft + tubeHeight / 2));
    queue.add(new pair(xTopLeft + tubeWidth / 2 + 200, yTopLeft + tubeHeight / 2)); //Redundancy
    queue.add(new pair(xTopLeft + tubeWidth / 2 - 200, yTopLeft + tubeHeight / 2));
    queue.add(new pair(xTopLeft + tubeWidth / 2, yTopLeft + tubeHeight / 2 + 200));
    queue.add(new pair(xTopLeft + tubeWidth / 2, yTopLeft + tubeHeight / 2 - 200));
    while (!queue.isEmpty()) {
      coords = queue.remove();
      if (coords.x < 0 || coords.y < 0 || coords.x >= imgWidth || coords.y >= imgHeight) continue;
      if (BiofilmArray[coords.x][coords.y] == 1) continue;
      PixelValue = img.getRGB(coords.x, coords.y) & 0xFF;
      if (PixelValue <= pixBrtThresh) {
        BiofilmArray[coords.x][coords.y] = 1;
        queue.add(new pair(coords.x + 1,coords.y));
        queue.add(new pair(coords.x - 1,coords.y));
        queue.add(new pair(coords.x,coords.y + 1));
        queue.add(new pair(coords.x,coords.y - 1));
      }
    }
    queue.add(new pair(0,0));
    while (!queue.isEmpty()) {
      coords = queue.remove();
      if (coords.x < 0 || coords.y < 0 || coords.x >= imgWidth || coords.y >= imgHeight) continue;
      if (BiofilmArray[coords.x][coords.y] >= 1) continue;
      PixelValue = img.getRGB(coords.x, coords.y) & 0xFF;
      if (PixelValue <= pixBrtThresh) {
        BiofilmArray[coords.x][coords.y] = 2;
        queue.add(new pair(coords.x + 1,coords.y));
        queue.add(new pair(coords.x - 1,coords.y));
        queue.add(new pair(coords.x,coords.y + 1));
        queue.add(new pair(coords.x,coords.y - 1));
      }
    }
    int numSections = 2, secCnt, isValid;
    for (int i = 0;i < img.getWidth();i++) {
      for (int j = 0;j < img.getHeight();j++) {
        if (BiofilmArray[i][j] == 0) {
          numSections++;
          secCnt = 0;
          isValid = 1;
          queue.add(new pair(i,j));
          while (!queue.isEmpty()) {
            coords = queue.remove();
            if (coords.x < 0 || coords.y < 0 || coords.x >= imgWidth || coords.y >= imgHeight) {
              isValid = 0;
              continue;
            }
            if (BiofilmArray[coords.x][coords.y] != 0) continue;
            secCnt++;
            BiofilmArray[coords.x][coords.y] = numSections;
            queue.add(new pair(coords.x + 1,coords.y));
            queue.add(new pair(coords.x - 1,coords.y));
            queue.add(new pair(coords.x,coords.y + 1));
            queue.add(new pair(coords.x,coords.y - 1));
          }
          if (isValid == 1 && secCnt > adjPixThresh) {
            queue.add(new pair(i,j));
            while (!queue.isEmpty()) {
              coords = queue.remove();
              if (coords.x < 0 || coords.y < 0 || coords.x >= imgWidth || coords.y >= imgHeight) {
                continue;
              }
              if (BiofilmArray[coords.x][coords.y] != numSections) continue;
              temp1[coords.x][coords.y] = sttImg - pic + 255;
              BiofilmArray[coords.x][coords.y] = 1; 
              queue.add(new pair(coords.x + 1,coords.y));
              queue.add(new pair(coords.x - 1,coords.y));
              queue.add(new pair(coords.x,coords.y + 1));
              queue.add(new pair(coords.x,coords.y - 1));
            }
          }
        }
      }
    }
    System.out.println(s + " finished analysis");
    
    }
    BufferedImage outimg = new BufferedImage(2662, 2662, BufferedImage.TYPE_INT_RGB);
    Graphics g = outimg.getGraphics();
    g.setColor(Color.black);
    g.fillRect(0, 0, 2662, 2662);
    for (int i = 0;i < 2662;i++) {
      for (int j = 0;j < 2662;j++) {
        if (temp1[i][j] != 0) outimg.setRGB(i, j, 0xFFFF0000 + temp1[i][j] * 0x100);
      }
    }
    ImageIO.write(outimg, "png", new File("contourMap.png"));
  }
}