package Controller.BackEnd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Your own paths and keys, read from Resources/local.properties which is kept out of git.
// Copy Resources/local.properties.example to local.properties and fill it in. Use forward slashes in paths
public class LocalConfig {
    private static final String filePath = "Resources/local.properties";
    private static Properties properties;

    public static String Get(String key) throws IOException {
        if(properties == null){
            Properties loaded = new Properties();
            try (FileInputStream in = new FileInputStream(filePath)) {
                loaded.load(in);
            } catch (IOException e) {
                throw new IOException("Couldn't read " + filePath + " - copy local.properties.example to local.properties and fill it in", e);
            }
            properties = loaded;
        }

        String value = properties.getProperty(key);
        if(value == null || value.trim().isEmpty()){
            throw new IOException("'" + key + "' is not set in " + filePath);
        }
        return value.trim();
    }
}
