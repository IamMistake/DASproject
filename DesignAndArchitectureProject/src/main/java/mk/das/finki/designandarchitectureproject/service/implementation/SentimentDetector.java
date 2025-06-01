package mk.das.finki.designandarchitectureproject.service.implementation;

import mk.das.finki.designandarchitectureproject.service.SentimentDetectorService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class SentimentDetector implements SentimentDetectorService {

    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "determination", "remuneration", "publish", "announce", "proposal", "approval", "selection", "decision", "success", "financial", "report", "minutes", "amendments", "proposal-decision", "appointment", "confirmation", "consolidated", "audited", "approved", "growth", "successfully", "professional", "agenda"
    ));

    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "sensitive", "dismiss", "failure", "problem", "concern", "disapproval", "reject", "resignation", "dismissed", "undecided", "error"
    ));


    public static String analyzeSentiment(String text) {
        String[] words = text.toLowerCase().split("\\W+");

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            } else if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }

        if (positiveCount > negativeCount) {
            return "Good news";
        } else if (negativeCount > positiveCount) {
            return "Bad news";
        } else {
            return "Neutral news";
        }
    }
}

