import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartsOfSpeech {
    private Map<String, ArrayList<ProbabilityPair>> transitionModel;

    PartsOfSpeech() {
        this.transitionModel = new HashMap<>();
    }

    public class ProbabilityPair {

        public ProbabilityPair(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        public ProbabilityPair() {
            this.key = "";
            this.value = 0.0;
        }

        private String key;
        private Double value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
}
    }

    private void modelLanguage(ArrayList<String> words, int n) {
        ArrayList<String[]> ngrams = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String[] ngram = new String[n];
            if (i < words.size()-n) {
                if (this.transitionModel.get(words.get(i)) != null) {
                    this.transitionModel.put(words.get(i), new ArrayList<>());
                }
                for (int j = 0; j < n; j++) {
                    ngram[j] = words.get(i+j);
                }
                ngrams.add(ngram);
            }
        }

        for (String[] ngram: ngrams) {
            ArrayList<ProbabilityPair> list;
            if (this.transitionModel.get(ngram[0]) != null)
                list = this.transitionModel.get(ngram[0]);
            else
                list = new ArrayList<>();
            if (!list.contains(ngram[ngram.length-1])) {
                list.add(new ProbabilityPair(ngram[ngram.length-1], 1.0));
            }
            else {
                ProbabilityPair pair = list.get(list.indexOf(ngram[ngram.length-1]));
                pair.setValue(pair.getValue() + 1.0);
            }
        }

        System.out.println(this.transitionModel.toString());
    }

    private void updateProbabilities() {

    }

    public static void main(String[] args) {
        try {
            ArrayList<String> words = new ArrayList<>();
            Files.lines(new File(args[0]).toPath()).forEach(line -> {
               String[] lineWords = line.split(" ");
                for (String word: lineWords) {
                    words.add(word);
                }
            });

            PartsOfSpeech POS = new PartsOfSpeech();
            POS.modelLanguage(words, 2);
            POS.updateProbabilities();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
