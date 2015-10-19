import java.awt.Color;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import javax.imageio.ImageIO;

public class BiofilmEval {

  public static class pair {
    int x,y;
    pair(int a,int b) {
      x = a;
      y = b;
    }
  }

  public static void main(String[] args) throws IOException {
    Scanner q = new Scanner(new File("info.txt"));
    File output = new File(q.nextLine()+" output.txt");
    if (output.exists()) {
      System.out.println("Don't overwrite files");
      return;
    }
    output.createNewFile();
    PrintStream w = new PrintStream(output);
    double xLength = q.nextDouble(), yLength = q.nextDouble(), zLength = q.nextDouble(), error = 0;
    int imgHeight, imgWidth, PixelValue, total = 0, tubeWidth = q.nextInt(), tubeHeight = q.nextInt(), xTopLeft = q.nextInt(), yTopLeft = q.nextInt();
    int sttImg = q.nextInt(), endImg = q.nextInt(), rndrPrd = q.nextInt();
    int adjPixThresh = q.nextInt(), pixBrtThresh = q.nextInt(), prevSlice = 0; q.nextLine();
    String fileName = q.nextLine();
    boolean filter = q.nextInt() == 1;
    System.out.println(filter);
    w.println(fileName + " pictures " + sttImg + " - " + endImg);
    //begin loop
    for (int pic = sttImg;pic <= endImg;pic++) {

    String s = String.valueOf(pic);
    while (s.length() < 4) s = '0' + s;
    s = "IM" + s;
    File f = new File(fileName + "\\" + s + ".jpg");
    BufferedImage img = ImageIO.read(f);
    //draw filter
    if (filter) {
      Graphics graphics = img.getGraphics();
      graphics.drawImage(ImageIO.read(new File("Filter.png")), 0, 0, null);
      //if (pic < 55) ImageIO.write(img, "png", new File(s+" renderasdf.png"));
    }
    imgHeight = img.getHeight();
    imgWidth = img.getWidth();
    int BiofilmArray[][] = new int[imgWidth][imgHeight], temp1[] = new int[1000000], temp2[] = new int[1000000];
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
    int numSections = 1, secCnt, isValid, inRadCnt, outRadCnt, inRadInvalid = 0, outRadValid = 0, imgTotal = 0;
    for (int i = 0;i < img.getWidth();i++) {
      for (int j = 0;j < img.getHeight();j++) {
        if (BiofilmArray[i][j] == 0) {
          numSections++;
          secCnt = inRadCnt = outRadCnt = 0;
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
            if (Math.pow(coords.x - tubeWidth / 2.0 - xTopLeft, 2) / Math.pow(tubeWidth / 2,2) +
                Math.pow(coords.y - tubeHeight / 2.0 - yTopLeft, 2) / Math.pow(tubeHeight / 2,2)
                <= 1) inRadCnt++;
            else outRadCnt++;
            BiofilmArray[coords.x][coords.y] = numSections;
            queue.add(new pair(coords.x + 1,coords.y));
            queue.add(new pair(coords.x - 1,coords.y));
            queue.add(new pair(coords.x,coords.y + 1));
            queue.add(new pair(coords.x,coords.y - 1));
          }
          if (isValid == 1 && secCnt > adjPixThresh) {
            imgTotal += secCnt;
            temp2[numSections] = 1;
            outRadValid += outRadCnt;
          }
          if (isValid == 0 && inRadCnt > 0) inRadInvalid += inRadCnt;
        }
      }
    }
    //output
    /*if (pic > sttImg) {
      error = 0;
      if (imgTotal > 50000 && prevSlice > 50000) 
        error = Math.abs((double)(imgTotal - prevSlice) / prevSlice * 100);
    }*/
    boolean doRndr = (rndrPrd != 0 && (pic - sttImg) % rndrPrd == 0);
    if (inRadInvalid * 100 / (Math.PI*tubeHeight*tubeWidth / 4) > 4 || outRadValid * 100 / (Math.PI*tubeHeight*tubeWidth / 4) > 1 || doRndr/* || error > 40*/) {
      if (!doRndr) {
        /*if (error > 40) w.println("There may be a stitching error in " + f.getName());
        else */w.println("There may be an error in " + f.getName());
      }
      BufferedImage outimg = new BufferedImage(imgWidth,imgHeight, BufferedImage.TYPE_INT_RGB);
      Graphics g = outimg.getGraphics();
      g.setColor(Color.black);
      g.fillRect(0, 0, img.getWidth(), img.getHeight());
      for (int i = 0;i < img.getWidth();i++) {
        for (int j = 0;j < img.getHeight();j++) {
          if (temp2[BiofilmArray[i][j]] == 1) outimg.setRGB(i, j, 0xFFFFFFFF);
        }
      }
      g.setColor(Color.red);
      //g.drawOval(xTopLeft, yTopLeft, tubeWidth, tubeHeight);
      ImageIO.write(outimg, "png", new File(s+" render.png"));
    }
    if (error <= 40) prevSlice = imgTotal;
    w.println(s + "\t" + imgTotal);
    total+=imgTotal;
    System.out.println(s + " finished analysis");

    }
    w.println("Total pixels captured : " + total);
  }
}
