import com.pannous.jini.openai.OpenAiAPI;
import com.pannous.jini.openai.Prompt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class OpenAiAPITest {

    OpenAiAPI openAiAPI = new OpenAiAPI();

    @Test
    void testQuery() {
        String content = "This is a test";
        try {
            String result = openAiAPI.query(content);
            assert result != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    void testPrompt() throws IOException, InterruptedException {
        String code = "// hi";
        String json = "[";

        Prompt prompt = Prompt.EXECUTE;
        if (prompt == Prompt.EXECUTE) {
            json += "{\"role\":\"system\",\"content\":\"OS: " + System.getProperty("os.name") + "\"},";
            json += "{\"role\":\"system\",\"content\":\"ARCH: " + System.getProperty("os.arch") + "\"},";
        } else {
            String language = prompt.language;
            json += "{\"role\":\"system\",\"content\":\"Language: " + language + "\"},";
        }
        json += "{\"role\":\"system\",\"content\":\"" + prompt.getText().replaceAll("\"", "'") + "\"},";
        if (prompt == Prompt.CONVERT)
            json += "{\"role\":\"system\",\"content\":\"Target language: " + prompt.language + "\"},";
        json += "{\"role\":\"user\",\"content\":\"" + code.replaceAll("\"", "'") + "\"}";
//        if (options.has(replace) || options.has(Options.noExplanations)) {
//            json+=",{\"role\":\"system\",\"content\":\"DO NOT OUTPUT ANY COMMENTS OR EXPLANATIONS"+"\"},";
//        }
        json += "]";
        System.out.println(json);
        String result = openAiAPI.query(json);
        System.out.println(result);
        assert result != null;

    }
}
