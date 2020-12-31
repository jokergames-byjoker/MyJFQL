package de.jokergames.jfql.util;

import de.jokergames.jfql.exception.FileException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Janick
 */

public class FileFactory {

    public JSONObject load(File file) {
        try {
            var reader = new FileReader(file);
            var builder = new StringBuilder();

            int read;

            while ((read = reader.read()) != -1) {
                builder.append((char) read);
            }

            reader.close();

            return new JSONObject(builder.toString());
        } catch (Exception ex) {
            throw new FileException("Can't load file '" + file.getName() + "'.");
        }
    }

    public JSONObject loadJoin(File... files){
        JSONObject jsonObject = new JSONObject();

        Arrays.stream(files).map(file -> load(file).toMap()).forEach(map -> map.keySet().forEach(key -> jsonObject.put(key, map.get(key))));
        return jsonObject;
    }

    public void save(File file, JSONObject jsonObject) {
        try {
            var writer = new FileWriter(file);
            writer.write(jsonObject.toString());
            writer.close();
        } catch (Exception ex) {
            throw new FileException("Can't save file '" + file.getName() + "'.");
        }
    }

}
