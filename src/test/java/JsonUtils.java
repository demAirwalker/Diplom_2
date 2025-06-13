import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {
    public static JSONObject readJsonFromFile(String path) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/" + path)));
        return new JSONObject(content);
    }
}