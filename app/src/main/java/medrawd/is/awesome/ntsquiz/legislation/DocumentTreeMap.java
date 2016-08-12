package medrawd.is.awesome.ntsquiz.legislation;

import java.util.Comparator;
import java.util.TreeMap;

public class DocumentTreeMap extends TreeMap<String, Document> {

    public DocumentTreeMap() {
        super(new DocumentComparator());
    }

    static class DocumentComparator implements Comparator<String> {
        @Override
        public int compare(String s, String t1) {
            if (s.matches("\\D*\\d+\\D*") && t1.matches("\\D*\\d+\\D*")) {
                Integer i1 = Integer.valueOf(s.replaceAll("[\\D]", ""));
                Integer i2 = Integer.valueOf(t1.replaceAll("[\\D]", ""));
                int comparison = i1.compareTo(i2);
                if (comparison == 0) {
                    return s.compareTo(t1);
                }
                return comparison;
            }
            return s.compareTo(t1);
        }
    }
}