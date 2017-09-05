package com.anxell.e3ak;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by nsdi-monkey on 2017/2/22.
 */

public class DimenTool {
    private static final float SCALE = 0.5f;

    public static void gen() {

        // 以此資料夾中的dimens.xml文件內容為初始值參照
        File file = new File("./app/src/main/res/values/dimens.xml");

        BufferedReader reader = null;
        StringBuilder sw240 = new StringBuilder();
        StringBuilder sw320 = new StringBuilder();
        StringBuilder sw480 = new StringBuilder();
        StringBuilder sw600 = new StringBuilder();

        StringBuilder sw720 = new StringBuilder();

        StringBuilder sw800 = new StringBuilder();

        StringBuilder w820 = new StringBuilder();

        try {

            System.out.println("生成不同分辨率：");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int line = 1;

            // 一次讀入一行，直到讀入null為文件結束
            while ((tempString = reader.readLine()) != null) {
                if (tempString.contains("</dimen>")) {

                    // tempString = tempString.replaceAll(" ", "");
                    String start = tempString.substring(0, tempString.indexOf(">") + 1);

                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    // 截取<dimen></dimen>標籤內的內容，從>右括號開始，到左括號減2，取得配置的數字
                    Double num = Double.parseDouble
                            (tempString.substring(tempString.indexOf(">") + 1,
                                    tempString.indexOf("</dimen>") - 2));

                    // 根據不同尺寸，計算新的值，拼接新的字符串，並且結尾處換行
                    // values / dimens 中的值是以 sw320dp為基礎，所以不用建立 sw320dp的資料夾
//                    sw240.append(start).append( num * 0.75).append(end).append("\r\n");  // 240 / 320
//
//                    sw320.append(start).append( num * 1).append(end).append("\r\n");  // 320 / 320
//
//                    sw480.append(start).append(num * 1.5).append(end).append("\r\n");    // 480 / 320
//
//                    sw600.append(start).append(num * 1.87).append(end).append("\r\n");   // 600 / 320
//
//                    sw720.append(start).append(num * 2.25).append(end).append("\r\n");   // 720 / 320
//
//                    sw800.append(start).append(num * 2.5).append(end).append("\r\n");    // 800 / 320
//
//                    w820.append(start).append(num * 2.56).append(end).append("\r\n");    // 820 / 320

                    sw240.append(start).append(num * 0.75).append(end).append("\r\n");  // 240 / 320

                    sw320.append(start).append(num * 1).append(end).append("\r\n");  // 320 / 320

                    sw480.append(start).append(num * 1.5 * SCALE).append(end).append("\r\n");    // 480 / 320

                    sw600.append(start).append(num * 1.87 * SCALE).append(end).append("\r\n");   // 600 / 320

                    sw720.append(start).append(num * 2.25 * SCALE).append(end).append("\r\n");   // 720 / 320

                    sw800.append(start).append(num * 2.5 * SCALE).append(end).append("\r\n");    // 800 / 320

                    w820.append(start).append(num * 2.56 * SCALE).append(end).append("\r\n");    // 820 / 320



                } else {
                    sw240.append(tempString).append("");

                    sw480.append(tempString).append("");

                    sw600.append(tempString).append("");

                    sw720.append(tempString).append("");

                    sw800.append(tempString).append("");

                    w820.append(tempString).append("");

                }

                line++;

            }

            reader.close();
            System.out.println("<!--  sw240 -->");

            System.out.println(sw240);

            System.out.println("<!--  sw320 -->");

            System.out.println(sw320);

            System.out.println("<!--  sw480 -->");

            System.out.println(sw480);

            System.out.println("<!--  sw600 -->");

            System.out.println(sw600);

            System.out.println("<!--  sw720 -->");

            System.out.println(sw720);

            System.out.println("<!--  sw800 -->");

            System.out.println(sw800);

            String sw240file = "./app/src/main/res/values-sw240dp/dimens.xml";

            String sw320file = "./app/src/main/res/values-sw320dp/dimens.xml";

            String sw480file = "./app/src/main/res/values-sw480dp/dimens.xml";

            String sw600file = "./app/src/main/res/values-sw600dp/dimens.xml";

            String sw720file = "./app/src/main/res/values-sw720dp/dimens.xml";

            String sw800file = "./app/src/main/res/values-sw800dp/dimens.xml";

            String w820file = "./app/src/main/res/values-w820dp/dimens.xml";
            // 將新的內容，寫入到指定的文件中
            writeFile(sw240file, sw240.toString());

            writeFile(sw320file, sw320.toString());

            writeFile(sw480file, sw480.toString());

            writeFile(sw600file, sw600.toString());

            writeFile(sw720file, sw720.toString());

            writeFile(sw800file, sw800.toString());

            writeFile(w820file, w820.toString());

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (reader != null) {

                try {

                    reader.close();

                } catch (IOException e1) {

                    e1.printStackTrace();
                }
            }
        }
    }


    /**
     * 寫入方法
     */

    public static void writeFile(String file, String text) {

        PrintWriter out = null;

        try {

            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            out.println(text);

        } catch (IOException e) {

            e.printStackTrace();

        }

        out.close();

    }
    public static void main(String[] args) {

        gen();

    }
}
