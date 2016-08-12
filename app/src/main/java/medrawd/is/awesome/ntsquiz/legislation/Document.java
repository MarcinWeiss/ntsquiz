package medrawd.is.awesome.ntsquiz.legislation;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import medrawd.is.awesome.ntsquiz.R;

public class Document {
    public static Map<String, Document> documents = new HashMap<>();

    private static final String TAG = Document.class.getSimpleName();
    private static Pattern article = Pattern.compile("^Art\\. (\\d+\\w*)\\..*");
    private static Pattern chapter = Pattern.compile("Rozdział (\\d+) .+");
    private static Pattern point = Pattern.compile("(\\d+)\\. .*");
    private static Pattern paragraph = Pattern.compile("^§ (\\d+)\\..*");
    private static Pattern subpoint = Pattern.compile("(\\d+)\\) .*");
    private static Pattern subsubpoint = Pattern.compile("([a-z])\\) .*");
    private static Pattern noneEmptyLine = Pattern.compile(".+");

    Map<String, Document> content = new DocumentTreeMap();
    String textA;
    String textB;

    public String getParagraph(String... keys) {
        Log.w(TAG, Arrays.toString(content.keySet().toArray()));
        Document previous = null;
        Document target = this;
        String lastKey = null;
        for (String key : keys) {
            Log.w(TAG, key);
            previous = target;
            lastKey = key;
            target = target.get(key);
        }

        if (previous != null && keys.length>2) {
            return paragraphToStringWithBold(previous, lastKey);
        } else {
            return paragraphToString(target);
        }
    }

    public Document get(String key) {
        return content.get(key);
    }


    private static String paragraphToStringWithBold(Document paragraph, String keyToBold) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<br>");
        if (null != paragraph.textA) {
            stringBuilder.append(paragraph.textA);
            stringBuilder.append("<br>");
        }
        if (null != paragraph.content && paragraph.content.size() > 0) {
            stringBuilder.append(paragraphMapToString(paragraph.content, keyToBold));
            stringBuilder.append("<br>");
        }
        if (null != paragraph.textB) {
            stringBuilder.append(paragraph.textB);
            stringBuilder.append("<br>");
        }
        stringBuilder.delete(stringBuilder.length() - 4, stringBuilder.length());
        return stringBuilder.toString();
    }

    private static String paragraphToString(Document paragraph) {
        return paragraphToStringWithBold(paragraph, null);
    }

    private static String paragraphMapToString(Map<String, Document> map, String keyToBold) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Document> entry : map.entrySet()) {
            if(entry.getKey().equals(keyToBold)){
                stringBuilder.append("<b>");
            }
            stringBuilder.append(paragraphToString(entry.getValue()));
            if(entry.getKey().equals(keyToBold)){
                stringBuilder.append("</b>");
            }
            stringBuilder.append("<br>");
        }
        stringBuilder.delete(stringBuilder.length() - 4, stringBuilder.length());
        return stringBuilder.toString();
    }


    public Document(String text) {
        textA = text;
    }

    public String getTextB() {
        return textB;
    }

    public void setTextB(String textB) {
        this.textB = textB;
    }

    public Map<String, Document> getContent() {
        return content;
    }

    public void setContent(Map<String, Document> content) {
        this.content.clear();
        this.content.putAll(content);
    }

    public String getTextA() {
        return textA;
    }

    public void setTextA(String textA) {
        this.textA = textA;
    }

    public void addContent(String key, Document paragraph) {
        content.put(key, paragraph);
    }

    public static Document loadUoBiA(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.uobia));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document uoiba = new Document("UoBiA");

        String line = reader.readLine();
        while (line != null) {
            Matcher articleMatcher = article.matcher(line);
            if (articleMatcher.matches()) {
                Document article = getArticle(line, reader);
                uoiba.content.put(articleMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("UoBiA", uoiba);
        return uoiba;
    }

    public static Document loadKodeksKarny(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.kk));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document kodeksKarny = new Document("KK");

        String line = reader.readLine();
        while (line != null) {
            Matcher articleMatcher = article.matcher(line);
            if (articleMatcher.matches()) {
                Document article = getArticle(line, reader);
                kodeksKarny.content.put(articleMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("KK", kodeksKarny);
        return kodeksKarny;
    }

    public static Document loadRegulaminStrzelnic(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.regulamin_strzelnic));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document kodeksKarny = new Document("Wzorcowy regulamin strzelnic");

        String line = reader.readLine();
        while (line != null) {
            Matcher chapterMatcher = chapter.matcher(line);
            if (chapterMatcher.matches()) {
                Document article = getArticle(line, reader);
                kodeksKarny.content.put(chapterMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("Wzorcowy regulamin strzelnic", kodeksKarny);
        return kodeksKarny;
    }

    public static Document loadRozporzadzenieWsPrzewozenia(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.przewozenie));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document rozporzadzenie = new Document("Rozporządzenie w sprawie przewożenia broni i amunicji środkami transportu publicznego");

        String line = reader.readLine();
        while (line != null) {
            Matcher chapterMatcher = paragraph.matcher(line);
            if (chapterMatcher.matches()) {
                Document article = getArticle(line, reader);
                rozporzadzenie.content.put(chapterMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("Rozporządzenie w sprawie przewożenia broni i amunicji środkami transportu publicznego", rozporzadzenie);
        return rozporzadzenie;
    }

    public static Document loadRozporzadzenieWsDeponowania(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.deponowanie));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document rozporzadzenie = new Document("Rozporządzenie w sprawie deponowania broni");

        String line = reader.readLine();
        while (line != null) {
            Matcher chapterMatcher = paragraph.matcher(line);
            if (chapterMatcher.matches()) {
                Document article = getArticle(line, reader);
                rozporzadzenie.content.put(chapterMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("Rozporządzenie w sprawie deponowania broni", rozporzadzenie);
        return rozporzadzenie;
    }

    public static Document loadRozporzadzenieWsNoszeniaIprzechowywania(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.noszenie));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document rozporzadzenie = new Document("Rozporządzenie w sprawie noszenia i przechowywania broni");

        String line = reader.readLine();
        while (line != null) {
            Matcher chapterMatcher = paragraph.matcher(line);
            if (chapterMatcher.matches()) {
                Document article = getArticle(line, reader);
                rozporzadzenie.content.put(chapterMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("Rozporządzenie w sprawie noszenia i przechowywania broni", rozporzadzenie);
        return rozporzadzenie;
    }

    public static Document loadRozporzadzenieWsEgzaminu(Context context) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.egzamin));
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("utf8")));

        Document rozporzadzenie = new Document("Rozporządzenie w sprawie egzaminu");

        String line = reader.readLine();
        while (line != null) {
            Matcher chapterMatcher = paragraph.matcher(line);
            if (chapterMatcher.matches()) {
                Document article = getArticle(line, reader);
                rozporzadzenie.content.put(chapterMatcher.group(1), article);
                Log.w(TAG, "article " + line + "added");
                Log.w(TAG, "article " + line + ": " + paragraphToString(article));
            }
            line = reader.readLine();
        }
        documents.put("Rozporządzenie w sprawie egzaminu", rozporzadzenie);
        return rozporzadzenie;
    }

    private static Document getArticle(String artTitle, BufferedReader reader) throws IOException {
        Log.i(TAG, artTitle);
        Document article = new Document(artTitle);
        Document currParagraph = null;
        Document currSubPoint = null;
        Document currSubSubPoint = null;
        String line = reader.readLine();
        while (line != null && line.length() > 0) {
            Matcher matcherPoint = point.matcher(line);
            Matcher matcherSubPoint = subpoint.matcher(line);
            Matcher matcherParagraph = paragraph.matcher(line);
            Matcher matcherSubSubPoint = subsubpoint.matcher(line);
            Matcher matcherNonEmptyLine = noneEmptyLine.matcher(line);
            if (matcherPoint.matches()) {
                Log.i(TAG, "point found " + matcherPoint.group(0));
                currParagraph = new Document(matcherPoint.group(0));
                currSubPoint = null;
                currSubSubPoint = null;
                article.addContent(matcherPoint.group(1), currParagraph);
            } else if (matcherParagraph.matches()) {
                Log.i(TAG, "paragraph found " + matcherParagraph.group(0));
                currParagraph = new Document(matcherParagraph.group(0));
                currSubPoint = null;
                currSubSubPoint = null;
                article.addContent(matcherParagraph.group(1), currParagraph);
            } else if (matcherSubPoint.matches()) {
                Log.i(TAG, "subpoint found " + matcherSubPoint.group(0));
                currSubPoint = new Document(matcherSubPoint.group(0));
                currSubSubPoint = null;
                currParagraph.addContent(matcherSubPoint.group(1), currSubPoint);
            } else if (matcherSubSubPoint.matches()) {
                Log.i(TAG, "subsubpoint found " + matcherSubSubPoint.group(0));
                currSubSubPoint = new Document(matcherSubSubPoint.group(0));
                currSubPoint.addContent(matcherSubSubPoint.group(1), currSubSubPoint);
            } else if (matcherNonEmptyLine.matches()) {
                if (null != currSubSubPoint) {
                    currSubPoint.setTextB(line);
                } else if (null != currSubPoint || null != currParagraph) {
                    currParagraph.setTextB(line);
                }
            }
            line = reader.readLine();
        }
        return article;
    }
}
