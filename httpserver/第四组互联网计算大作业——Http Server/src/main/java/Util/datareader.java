package Util;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class datareader {
    public List<String[]> form;

    public datareader(String path){
        form=new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("resources/"+path));
             CSVReader csvReader = new CSVReader(reader)) {
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                form.add(record);
            }
        } catch (IOException | CsvValidationException ex) {
            ex.printStackTrace();
        }
    }

}
