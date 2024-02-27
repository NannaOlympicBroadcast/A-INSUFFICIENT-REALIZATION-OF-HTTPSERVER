package Util;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class datadumper {
    public datadumper(String path, List<String[]> data){
        try (Writer reader = Files.newBufferedWriter(Paths.get("resources/"+path));
             CSVWriter csvReader = new CSVWriter(reader)) {
            csvReader.writeAll(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
