package com.example.etisala;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // the virbal
    private static final String  TAG = "MainActivity";
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] ListFile;
    File file;
    ArrayList<String> PathHistory;
    String LastDirectory;
    int count = 0;
    ArrayList<numberClass> uploadData;
    ListView LsInterinalStorage , listnumber;
    Button btnUploadData , btnSdCard , btnExcell , btn_wrsms;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LsInterinalStorage = (ListView) findViewById(R.id.List_File);
        listnumber = (ListView) findViewById(R.id.listnumber);
        btnSdCard = (Button) findViewById(R.id.btnSDCard);
        btnUploadData = (Button) findViewById(R.id.btn_up);
        btnExcell = (Button) findViewById(R.id.btn_excell);
        btn_wrsms = (Button) findViewById(R.id.btn_wrsms);

        uploadData = new ArrayList<>();
        //check Permision read and wirate
        CheckPermisoin();
        //Goes up one directory level
        btnUploadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // LsInterinalStorage.setEnabled(true);
                if(listnumber.getVisibility() == View.VISIBLE ){
                        LsInterinalStorage.setVisibility(View.VISIBLE);
                        listnumber.setVisibility(View.INVISIBLE);
                }else {
                    if(count == 0){
                        Log.d(TAG, "btnUpDirectory: You have reached the highest level directory.");
                    }else{
                        PathHistory.remove(count);
                        count--;
                        CheckInternalStorage();
                        Log.d(TAG, "btnUpDirectory: " + PathHistory.get(count));
                    }

                }

            }
        });
        btnSdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listnumber.getVisibility() == View.VISIBLE ){
                    LsInterinalStorage.setVisibility(View.VISIBLE);
                    listnumber.setVisibility(View.INVISIBLE);
                }else {
                    LsInterinalStorage.setVisibility(View.INVISIBLE);
                    listnumber.setVisibility(View.VISIBLE);
                }
                count = 0;
                PathHistory = new ArrayList<String>();
                PathHistory.add(count,System.getenv("EXTERNAL_STORAGE"));
                Log.d(TAG, "btnSDCard: " + PathHistory.get(count));
                CheckInternalStorage();
            }
        });


        btnExcell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PrintDataToLog();

            }
        });

        LsInterinalStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LastDirectory = PathHistory.get(count);
                if(LastDirectory.equals(adapterView.getItemAtPosition(i))){
                    Log.d(TAG,"LvInternalStoage : Select a file For Uplaod : " + LastDirectory);
                    if(LastDirectory.endsWith(".xlsx")){
                        readExcelData(LastDirectory);
                    }else {
                        toastMessage("صيغة الملف غير مدعومة !!");
                    }

                }else{
                    count++;
                    PathHistory.add(count,(String) adapterView.getItemAtPosition(i));
                    CheckInternalStorage();
                    Log.d(TAG,"LvInternalStoage : " + PathHistory.get(count));
                }
            }
        });



    }

    private void CheckInternalStorage() {
        Log.d(TAG,"CheckInternalStorage : Started !!");
        try{
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                toastMessage("No SD Card Found");
            }
            else {
                file = new File(PathHistory.get(count));
                Log.d(TAG,"CheckInternalStorage : dirictory Path :" + PathHistory.get(count));

            }
            ListFile = file.listFiles();
            // create a String Array For File Path
            FilePathStrings = new String[ListFile.length];
            //  Create a String Array For Files Name
            FileNameStrings = new String[ListFile.length];
            for(int i = 0 ; i < ListFile.length ; i++){

                // get the path of the image File
                FilePathStrings[i] = ListFile[i].getAbsolutePath();
                // get the name of the image File
                FileNameStrings[i] = ListFile[i].getName();
                Log.d(TAG,"Files : File Name & Path = " + FileNameStrings[i] + "&" + FilePathStrings[i]);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 ,FilePathStrings);
            LsInterinalStorage.setAdapter(adapter);


        }
        catch (NullPointerException e){

            Log.e(TAG,"CheckInternalStorage : NullPointerException : " + e.getMessage());

        }

    }

    private void readExcelData(String lastDirectory) {
        Log.d(TAG , "readExcelData : Read Excel File ....");
        //toastMessage("تم قراءة ملف الاكسيل بنجاح ...");
        Toast.makeText(this,"تم قراءة ملف الاكسيل بنجاح ...",Toast.LENGTH_LONG).show();
        File inputFile = new File(lastDirectory);
        try{
            InputStream stream = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            StringBuilder builder = new StringBuilder();
            for(int r = 3 ; r < rowCount ; r++){
                Row row = sheet.getRow(r);
                int cellCount = row.getPhysicalNumberOfCells();
                for(int c = 0 ; c < cellCount ; c++){
                    //handles if there are to many columns on the excel sheet.
                    if(c>2){
                        Log.e(TAG, "readExcelData: ERROR. Excel File Format is incorrect! " );
                        toastMessage("خطأ في صيغة الملف !!! ");
                        break;
                    }else{
                        String value = getCellAsString(row, c, formulaEvaluator);
                        String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;
                        Log.d(TAG, "readExcelData: Data from row: " + cellInfo);
                        builder.append(value + ",");
                    }
                }
                builder.append(":");
            }
            parsaStringBuilder(builder);
        }
        catch (FileNotFoundException e){
            Log.e(TAG ,"readExcelData : FileNotFoundException  : " + e.getMessage() );
        }
        catch (IOException e){
            Log.e(TAG ,"readExcelData : IOException  : " + e.getMessage() );
        }


    }

    private void parsaStringBuilder(StringBuilder builder) {
        Log.d(TAG , " parsaStringBuilder is Started ...");
        // split the sb into Row
        String[] rows = builder.toString().split(":");
        // Add the Array List <Number Class>
        for (int i = 0 ; i < rows.length ; i++){
            String[]colmus = rows[i].split(",");
            try{
                String numberPhone = colmus[0];
                String test = colmus[1];
                uploadData.add(new numberClass(numberPhone , test));
            }
            catch (NumberFormatException e){
                Log.e(TAG,"parsaStringBuilder : NumberFormatException :  "+ e.getMessage());
            }
        }
      //  PrintDataToLog();

    }

    private void PrintDataToLog() {
        if(listnumber.getVisibility() == View.INVISIBLE ){
            LsInterinalStorage.setVisibility(View.INVISIBLE);
            listnumber.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "printDataToLog: Printing data to log...");
        String [] numbers = new String[uploadData.size()];
        for(int i = 0; i< uploadData.size(); i++){
            String x = uploadData.get(i).getNumberPhone();
            String y = uploadData.get(i).getTest();
            numbers[i] = uploadData.get(i).getTest();
            Log.d(TAG, "printDataToLog: (x,y): (" + x + "," + y + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 ,numbers);
        listnumber.setAdapter(adapter);
        //LsInterinalStorage.setEnabled(false);
        listnumber.setOnItemClickListener(null);
    }

    private String getCellAsString(Row r, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try{
            Cell cell = r.getCell(c);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
        //    value = "0"+cellValue.getStringValue();
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                  //  value = ""+cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:

                    double numericValue = cellValue.getNumberValue();
//                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
//                        double date = cellValue.getNumberValue();
//                        SimpleDateFormat formatter =
//                                new SimpleDateFormat("MM/dd/yy");
//                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
//                    } else {
//                        value = ""+numericValue;
//                    }
//                    value = "0"+String.valueOf(numericValue);
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "0"+cellValue.getStringValue();
                    break;
                default:
            }
        }
        catch (NullPointerException e){
            Log.e(TAG, "getCellAsString NullPointerException : "+e.getMessage());
        }

        return value;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckPermisoin(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int PermisionCheck = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            PermisionCheck += this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PermisionCheck += this.checkSelfPermission(Manifest.permission.SEND_SMS);
            PermisionCheck += this.checkSelfPermission(Manifest.permission.BROADCAST_SMS);
            PermisionCheck += this.checkSelfPermission(Manifest.permission.READ_SMS);
            PermisionCheck += this.checkSelfPermission(Manifest.permission.RECEIVE_SMS);
            if(PermisionCheck != 0){
                this.requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.SEND_SMS , Manifest.permission.BROADCAST_SMS , Manifest.permission.READ_SMS , Manifest.permission.RECEIVE_SMS },1011);

            }else {

                Log.d(TAG,"SDK : Check Permission Not req");
                }
            }


        }
        private void toastMessage(String msg){
            Toast.makeText(this , msg , Toast.LENGTH_LONG).show();
        }
        private class sendSms extends AsyncTask<String,String,String>{
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected String doInBackground(String... strings) {
                return null;
            }
        }

    }


