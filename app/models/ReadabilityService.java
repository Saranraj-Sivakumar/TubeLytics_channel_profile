package models;

public class ReadabilityService {

    public double calculateFleschKincaidGrade(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double gradeLevel = 0.39 * ((double) wordCount / sentenceCount) + 11.8 * ((double) syllableCount / wordCount) - 15.59;
        return Math.round(gradeLevel * 100.0) / 100.0;
    }

    public double calculateFleschReadingEase(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double readingEase = 206.835 - 1.015 * ((double) wordCount / sentenceCount) - 84.6 * ((double) syllableCount / wordCount);
        return Math.round(readingEase * 100.0) / 100.0;
    }

    private int countWords(String text) {
        String[] words = text.split("\\s+");
        return words.length;
    }

    private int countSentences(String text) {
        String[] sentences = text.split("[.!?]");
        return sentences.length;
    }

    private int countSyllablesInText(String text) {
        int syllableCount = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            syllableCount += countSyllables(word);
        }
        return syllableCount;
    }

    private int countSyllables(String word) {
        int count = 0;
        boolean lastWasVowel = false;
        String vowels = "aeiouy";
        word = word.toLowerCase();

        for (char wc : word.toCharArray()) {
            if (vowels.indexOf(wc) != -1) {
                if (!lastWasVowel) {
                    count++;
                }
                lastWasVowel = true;
            } else {
                lastWasVowel = false;
            }
        }

        if (word.endsWith("e")) {
            count--;
        }
        return Math.max(count, 1);
    }
}
