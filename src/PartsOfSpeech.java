import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PartsOfSpeech {
    private Map<String, ArrayList<ProbabilityPair>> transitionModel;
    private Map<String, ArrayList<ProbabilityPair>> sensorModel;
    private Random rand;

    PartsOfSpeech() {
        this.transitionModel = new HashMap<>();
        this.sensorModel = new HashMap<>();
        rand = new Random();
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

    private ProbabilityPair find(ArrayList<ProbabilityPair> list, String key) {
        for (ProbabilityPair pair : list) {
            if(pair.getKey().equals(key))
                return pair;
        }
        return null;
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
            else {
                list = new ArrayList<>();
                this.transitionModel.put(ngram[0], list);
            }
            if (find(list, ngram[ngram.length-1]) == null) {
                list.add(new ProbabilityPair(ngram[ngram.length-1], 1.0));
            }
            else {
                ProbabilityPair pair = list.get(list.indexOf(find(list, ngram[ngram.length-1])));
                pair.setValue(pair.getValue() + 1.0);
            }
        }
    }

    private void tagLanguage(ArrayList<String> words) {
        //TODO: add functionality
    }

    private void modelSensor() {
        //TODO: add functionality
    }

    private void updateProbabilities(Map<String, ArrayList<ProbabilityPair>> map) {
        for (Map.Entry<String, ArrayList<ProbabilityPair>> entry : map.entrySet())
        {
            Double total = 0.0;
            for (ProbabilityPair pair : entry.getValue())
                total += pair.getValue();
            for (ProbabilityPair pair : entry.getValue())
                pair.setValue(pair.getValue()/total);
        }
    }

    private String generateWord(ArrayList<ProbabilityPair> list, Double value) {
        String result = "";
        HashMap<ProbabilityPair, Double> probably = new HashMap<>();
        Double max = 0.0;
        for (ProbabilityPair pair : list) {
            max += pair.getValue();
            probably.put(pair, max);
        }
        for (Map.Entry<ProbabilityPair, Double> entry : probably.entrySet()) {
            if (value < entry.getValue() && value >= entry.getValue() - entry.getKey().getValue())
                result = entry.getKey().getKey();
        }
        return result;
    }

    private String generateText(String seed, int count) {
        String result = seed;
        for (int i = 0; i < count; i++) {
            result += " ";
            ArrayList<ProbabilityPair> list = this.transitionModel.get(seed);
            Double value = rand.nextDouble();
            seed = generateWord(list, value);
            result += seed;
        }
        return result;
    }

    private String generateTags(ArrayList<String> words) {
        //TODO: add functionality
        return "";
    }

    /**
     * Part 1
     * filename - to read probabilities from
     * seedword - to start generating text from
     * count - length of generated text
     *
     * Part 2
     * filename - training data
     * filename - testing data
     * 
     * @param args
     */
    public static void main(String[] args) {
        PartsOfSpeech POS = new PartsOfSpeech();

        // Part 1 //
        try {
            ArrayList<String> words = new ArrayList<>();
            Files.lines(new File(args[0]).toPath()).forEach(line -> {
               String[] lineWords = line.split(" ");
                for (String word: lineWords) {
                    words.add(word);
                }
            });

            POS.modelLanguage(words, 2);
            POS.updateProbabilities(POS.transitionModel);

            String genesis = POS.generateText(args[1], Integer.parseInt(args[2]));
            try{
                PrintWriter writer = new PrintWriter("tests/generated.txt", "UTF-8");
                writer.println(genesis);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Part 2 //
        try {
            ArrayList<String> words = new ArrayList<>();
            Files.lines(new File(args[3]).toPath()).forEach(line -> {
                String[] lineWords = line.split(" ");
                for (String word: lineWords) {
                    words.add(word);
                }
            });

            POS.reset();
            POS.tagLanguage(words);
            POS.updateProbabilities(POS.transitionModel);
            POS.modelSensor();
            POS.updateProbabilities(POS.sensorModel);

            words.clear();
            Files.lines(new File(args[4]).toPath()).forEach(line -> {
                String[] lineWords = line.split(" ");
                for (String word: lineWords) {
                    words.add(word);
                }
            });
            String tags = POS.generateTags(words);
            try{
                PrintWriter writer = new PrintWriter("tests/tagged.txt", "UTF-8");
                writer.println(tags);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        this.transitionModel = new HashMap<>();
        this.sensorModel = new HashMap<>();
    }

}
