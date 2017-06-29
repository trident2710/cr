/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author adychka
 */
public class FileUtils {
  public static void writeObjectToFile(String directory,String fileName,Object object) throws FileNotFoundException, IOException{
    File file = new File(directory);
    file.mkdir();
    FileOutputStream outputStream;
    outputStream = new FileOutputStream(fileName);
    outputStream.write(object.toString().getBytes());
    outputStream.close();
  }
}
