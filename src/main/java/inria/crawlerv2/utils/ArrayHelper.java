/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.utils;

import java.util.Random;

/**
 *
 * @author adychka
 */
public class ArrayHelper {
  public static <T>T[] shuffle(T[] array){
    T temp;
    int index;
    Random random = new Random();
    for (int i = array.length - 1; i > 0; i--)
    {
        index = random.nextInt(i + 1);
        temp = array[index];
        array[index] = array[i];
        array[i] = temp;
    }
    return array;
  }
}
