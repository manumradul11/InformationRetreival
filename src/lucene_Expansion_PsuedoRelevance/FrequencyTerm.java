package lucene_Expansion_PsuedoRelevance;

public class FrequencyTerm implements Comparable<FrequencyTerm> {
    String term;
    long frequency;

    public FrequencyTerm(String term, long termFreq) {
        this.term = term;
        this.frequency = termFreq;
    }
    
    public void increment() {
        this.frequency++;
    }

    public String getDocId() {
        return term;
    }

    public void setDocId(String term) {
        this.term = term;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public boolean containsDoc(String term) {
        if(term.equalsIgnoreCase(this.term)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FrequencyTerm other = (FrequencyTerm) obj;
        if (!this.term.equalsIgnoreCase(other.term)) {
            return false;
        }
        return true;
    }
    
    public int compareTo(FrequencyTerm df) {
        if(this.getFrequency()> df.getFrequency()){
                return -1;
        } else if (this.getFrequency() < df.getFrequency()){
                return 1;
        } else {
                return 0;
        }
    }
    
    public void print() {
        System.out.println(term + " : " + frequency);
    }
    
}
