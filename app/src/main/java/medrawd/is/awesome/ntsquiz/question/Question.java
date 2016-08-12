package medrawd.is.awesome.ntsquiz.question;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import medrawd.is.awesome.ntsquiz.R;
import medrawd.is.awesome.ntsquiz.legislation.Document;

public class Question {
    public static final String TAG = Question.class.getSimpleName();
    public static final String QUESTIONS_FILENAME = "pytania.tsv";
    public static List<Question> questions = new ArrayList<>();
    private static Pattern documentNamePattern = Pattern.compile("^(.+) \\-.*$");
    private static Pattern articleNamePattern = Pattern.compile("^.*Art\\. (\\d+\\w*).*$");
    private static Pattern chapterNamePattern = Pattern.compile("^.*rozdz\\. (\\d+\\w*).*$");
    private static Pattern paragraphNamePattern = Pattern.compile("^.*§ (\\d+\\w*).*$");
    private static Pattern pointNamePattern = Pattern.compile("^.*ust. (\\d+).*$");
    private static Pattern subpointNamePattern = Pattern.compile("^.*pkt (\\d+).*$");

    String question;
    String[] answers;
    Integer correctAnswer;
    String[] justification;

    public Question(String question, String[] answers, int correctAnswer, String justification) {
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        Log.i(TAG, question);

        Matcher documentNameMatcher = documentNamePattern.matcher(justification);
        Matcher articleNameMatcher = articleNamePattern.matcher(justification);
        Matcher chapterNameMatcher = chapterNamePattern.matcher(justification);
        Matcher paragraphNameMatcher = paragraphNamePattern.matcher(justification);
        Matcher pointNameMatcher = pointNamePattern.matcher(justification);
        Matcher subpointNameMatcher = subpointNamePattern.matcher(justification);

        List<String> justificationAddress = new ArrayList<>();
        if (documentNameMatcher.matches()) {
            justificationAddress.add(documentNameMatcher.group(1));
        } else {
            throw new RuntimeException("justification error no document name "+justification);
        }
        if (articleNameMatcher.matches()) {
            justificationAddress.add(articleNameMatcher.group(1));
        } else {
            if(chapterNameMatcher.matches()){
                justificationAddress.add(chapterNameMatcher.group(1));
            } else {
                if(paragraphNameMatcher.matches()){
                    justificationAddress.add(paragraphNameMatcher.group(1));
                }
            }
        }
        if (pointNameMatcher.matches()) {
            justificationAddress.add(pointNameMatcher.group(1));
        }
        if (subpointNameMatcher.matches()) {
            justificationAddress.add(subpointNameMatcher.group(1));
        }

        this.justification = justificationAddress.toArray(new String[justificationAddress.size()]);
        questions.add(this);
        Log.d(TAG, Arrays.toString(this.justification));
        Log.d(TAG, justification);
        String[] address = Arrays.copyOfRange(this.justification, 1, this.justification.length);
        Log.d(TAG, Document.documents.get(this.justification[0]).getParagraph(address));
    }

    public static List<Question> loadQuestions(Context context) throws IOException {
        Log.d(TAG, "loadQuestions");
        questions.clear();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.openFileInput(QUESTIONS_FILENAME));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        String line = reader.readLine();
        while (line != null) {
            String[] split = line.split("\t", -1);
            String correctAnswerString = split[4];
            Integer correctAnswer = null;

            if (correctAnswerString.equals("A")) {
                correctAnswer = 0;
            } else if (correctAnswerString.equals("B")) {
                correctAnswer = 1;
            } else if (correctAnswerString.equals("C")) {
                correctAnswer = 2;
            }

            if (correctAnswer != null) {
                new Question(split[0], Arrays.copyOfRange(split, 1, 4), correctAnswer, split[5]);
            }
            line = reader.readLine();
        }
        return questions;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getAnswers() {
        return answers;
    }

    public void setAnswers(String[] answers) {
        this.answers = answers;
    }

    public Integer getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Integer correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String[] getJustification() {
        return justification;
    }

    public void setJustification(String[] justification) {
        this.justification = justification;
    }
}
