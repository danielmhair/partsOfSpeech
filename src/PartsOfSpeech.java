import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
            if (i <= words.size()- n) {
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
        ArrayList<String[]> ngrams = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String[] splitTag = words.get(i).split("_");
            String[] ngram = new String[2];
            if (i <= words.size()- 2) {
                if (this.transitionModel.get(splitTag[1]) != null) {
                    this.transitionModel.put(splitTag[1], new ArrayList<>());
                }
                for (int j = 0; j < 2; j++) {
                    ngram[j] = words.get(i+j).split("_")[1];
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

    private void modelSensor(ArrayList<String> words) {
        for (String word : words) {
            String[] splitTag = word.split("_");
            ArrayList<ProbabilityPair> list;
            if (this.sensorModel.get(splitTag[1]) != null)
                list = this.sensorModel.get(splitTag[1]);
            else {
                list = new ArrayList<>();
                this.sensorModel.put(splitTag[1], list);
            }
            if (find(list, splitTag[0]) == null) {
                list.add(new ProbabilityPair(splitTag[0], 1.0));
            }
            else {
                ProbabilityPair pair = list.get(list.indexOf(find(list, splitTag[0])));
                pair.setValue(pair.getValue() + 1.0);
            }
        }
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

            POS.modelSensor(words);
            POS.updateProbabilities(POS.sensorModel);

            words.clear();
            Files.lines(new File(args[4]).toPath()).forEach(line -> {
                String[] lineWords = line.split(" ");
                for (String word: lineWords) {
                    words.add(word);
                }
            });
            ArrayList<String> tags = POS.viterbi(words);
            try{
                PrintWriter writer = new PrintWriter("tests/tagged.txt", "UTF-8");
                for (String tagged : tags) {
                    writer.print(tagged);
                    writer.print(" ");
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ProbNode {
        private double prob;
        private ProbNode previous;
        private String tag;

        public ProbNode(double prob, ProbNode previous, String tag) {
            this.prob = prob;
            this.previous = previous;
            this.tag = tag;
        }

        public ProbNode() {
            this.prob = 0.0;
            this.previous = null;
            this.tag = "";
        }

        public double getProb() {
            return prob;
        }

        public void setProb(double prob) {
            this.prob = prob;
        }

        public ProbNode getPrevious() {
            return previous;
        }

        public void setPrevious(ProbNode previous) {
            this.previous = previous;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    private double transitionProb(ArrayList<Map<String, ProbNode>> SequenceProb, int index,
                                  Map.Entry<String, ArrayList<ProbabilityPair>> state, Map.Entry<String, ArrayList<ProbabilityPair>> prev_state) {
        ProbabilityPair pair = find(this.transitionModel.get(prev_state.getKey()), state.getKey());
        double pair_prob = .001;
        if (pair != null)
            pair_prob = pair.getValue();
        return SequenceProb.get(index).get(prev_state.getKey()).getProb() * pair_prob;
    }

    private double sensorProb(Map.Entry<String, ArrayList<ProbabilityPair>> state, String observation) {
        ProbabilityPair pair = find(this.sensorModel.get(state.getKey()), observation);
        double pair_prob = .001;
        if (pair != null)
            pair_prob = pair.getValue();
        return pair_prob;
    }

    private double max(ArrayList<Map<String, ProbNode>> SequenceProb, int index, Map.Entry<String, ArrayList<ProbabilityPair>> state) {
        BigDecimal max_tr_prob = new BigDecimal(0.0);
        for (Map.Entry<String, ArrayList<ProbabilityPair>> prev_state : this.transitionModel.entrySet()) {
            BigDecimal tr_prob = new BigDecimal(transitionProb(SequenceProb, index, state, prev_state));
            if (tr_prob.compareTo(max_tr_prob) > 0)
                max_tr_prob = tr_prob;
        }
        return max_tr_prob.doubleValue();
    }

    private ArrayList<String> viterbi(ArrayList<String> obs) {
        ArrayList<Map<String, ProbNode>> SequenceProb = new ArrayList<>();

        Map<String, ProbNode> initialStates = new HashMap<>();
        for (Map.Entry<String, ArrayList<ProbabilityPair>> entry : this.sensorModel.entrySet()) {
            initialStates.put(entry.getKey(), new ProbNode(.0001, null, entry.getKey()));
            if (entry.getKey().equals("IN")) {
                initialStates.get("IN").setProb(0.999);
            }
        }
        SequenceProb.add(initialStates);

        //Forward
        for (int t = 1; t < obs.size(); t++) {
            Map<String, ProbNode> curStates = new HashMap<>();
            for (Map.Entry<String, ArrayList<ProbabilityPair>> state : this.transitionModel.entrySet()) {
                double max_tr_prob = max(SequenceProb, t - 1, state);
                for (Map.Entry<String, ArrayList<ProbabilityPair>> prev_state : this.transitionModel.entrySet()) {
                    if (transitionProb(SequenceProb, t - 1, state, prev_state) == max_tr_prob) {
                        double max_prob = max_tr_prob * sensorProb(state, obs.get(t));
                        curStates.put(state.getKey(), new ProbNode(max_prob, SequenceProb.get(t - 1).get(prev_state.getKey()), state.getKey()));
                        break;
                    }
                }
            }
            SequenceProb.add(curStates);
        }

        for (int i = 0; i < obs.size(); i++) {
            double max = -1.0;
            String tag = "";
            for (Map.Entry<String, ProbNode> entry : SequenceProb.get(i).entrySet()) {
                if(entry.getValue().getProb() > max) {
                    max = entry.getValue().getProb();
                    tag = entry.getValue().getTag();
                }
            }
            obs.set(i, obs.get(i) + "_" + tag);
        }

        return obs;
    }

    private void reset() {
        this.transitionModel = new HashMap<>();
        this.sensorModel = new HashMap<>();
    }

}
