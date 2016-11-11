import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartsOfSpeech {
    private Map<String, ArrayList<Pair<String, Double>>> transitionModel;

    PartsOfSpeech() {
        this.transitionModel = new HashMap<>();
    }

    private void modelLanguage(String[] words) {
        for (String word: words) {
            if (this.transitionModel.getOrDefault(word, ))
        }
    }

    public static void main(String[] args) {

    }
}
