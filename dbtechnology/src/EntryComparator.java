import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// Class used for comparing Entries based on different criteria
class EntryComparator{
    static class EntryBoundComparator implements Comparator<Entry>{
        private HashMap<Entry,Double> entryComparisonMap;
        EntryBoundComparator(List<Entry> entriesToCompare, int dimension, boolean compareByUpper){
            this.entryComparisonMap = new HashMap<>();
            if (compareByUpper){
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getUpper());
            }else{
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getLower());
            }
        }
        public int compare(Entry entryA, Entry entryB){
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }
}