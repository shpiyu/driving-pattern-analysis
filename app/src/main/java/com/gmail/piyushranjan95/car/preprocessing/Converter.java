package com.gmail.piyushranjan95.car.preprocessing;

import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by piyush on 9/6/17.
 */
public class Converter {
    private final double maxSpeed = 163; //220
    private final double maxRPM = 6000; //8k
    private double maxjq;
    private double maxzs;

    private ArrayList<String[]> inputList;
    private ArrayList<String[]> changeRateList;
    private ArrayList<String[]> outputList;
    private ArrayList<Double[]> outputListDouble;

    public void initializeConverter()
    {
        inputList = new ArrayList<>();
        changeRateList = new ArrayList<>();
        outputList = new ArrayList<>();
        outputListDouble = new ArrayList<>();
    }


    public void readInput(String filename){
        String path = "/sdcard/"+filename;  // /sdcard/myinput.csv
        File testFile = new File(path);
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(testFile));
            String[] row = null;
            while((row = csvReader.readNext()) != null) {
                Log.d("op","enter the dragon");
                Log.d("op",row[0]
                        + " # " + row[1]
                        + " #  " + row[2]);
                inputList.add(row);
            }
            csvReader.close();

        } catch (FileNotFoundException e) {
            Log.d("op","file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("op","io exception");
            e.printStackTrace();
        }
    }

    public void makeChangeRateList(){
        for(int i=0;i<inputList.size()-1;i++)
        {
            // added Math.abs in the 11th hour :p
            double speedCR = Math.abs(trimEnd(inputList.get(i+1)[1],4) - trimEnd(inputList.get(i)[1],4));
            double rpmCR = Math.abs(trimEnd(inputList.get(i+1)[2],3) - trimEnd(inputList.get(i)[2],3));
            double tpCR = Math.abs(trimEnd(inputList.get(i+1)[3],1) - trimEnd(inputList.get(i)[3],1));

            String res = inputList.get(i)[0]+" "+speedCR+" "+rpmCR+" "+tpCR+" "+trimEnd(inputList.get(i)[4],1);
            String resa[] = res.split(" ");
            changeRateList.add(resa);
            Log.d("op",inputList.get(i)[0]+" "+speedCR+" "+rpmCR+" "+tpCR+" "+inputList.get(i)[4]);
        }
    }

    public void processing(){
        maxjq = -99;
        maxzs = -99;
        for(int i=0;i<changeRateList.size();i++){
            if(Double.parseDouble(changeRateList.get(i)[3])>maxjq){
                maxjq = Double.parseDouble(changeRateList.get(i)[3]);
            }

            if(Double.parseDouble(changeRateList.get(i)[2])>maxzs){
                maxzs = Double.parseDouble(changeRateList.get(i)[2]);
            }
        }


        //prepare output list
        for(int i=0;i<changeRateList.size();i++){
            Double value = ratioVE(trimEnd(inputList.get(i)[1],4),trimEnd(inputList.get(i)[2],3));
            Double value2 = ratioTE(Double.parseDouble(changeRateList.get(i)[3]),Double.parseDouble(changeRateList.get(i)[2]));
            String res = Math.floor(value * 100) / 100
                    +" "+Math.floor(value2*100)/100
                    +" "+trimEnd(inputList.get(i)[4],1);

            String resa[] = res.split(" ");


            outputList.add(resa);
            //outputListDouble.add(cast(resa));
        }

        writeResults(outputList);

    }


    private double ratioVE(double cs, double zs){
        return (cs/maxSpeed)/(zs/maxRPM);
    }

    private double ratioTE(double jq, double zs){
        return (jq/maxjq)/(zs/maxzs);
    }


    private double trimEnd(String str, int k){
        return Double.parseDouble( str.substring(0, str.length()-k));
    }

    private void writeResults(ArrayList<String[]> resa){
        try {
            //File testFile = new File("/sdcard/firstOutput.csv");
            //String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName = "firstOutput.csv";
            String filePath = "/sdcard" + File.separator + fileName;
            Log.d("op",filePath);
            //File f = new File(filePath );
            CSVWriter writer;
// File exist
//            if(f.exists() && !f.isDirectory()){
//                mFileWriter = new FileWriter(filePath , true);
//                writer = new CSVWriter(mFileWriter);
//            }
//            else {
            writer = new CSVWriter(new FileWriter(filePath));
//            }
            writer.writeAll(resa);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
