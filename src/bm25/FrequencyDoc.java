package bm25;

public class FrequencyDoc implements Comparable<FrequencyDoc> {
    String docId;
    int frequency;

    public FrequencyDoc(String docId, int frequency) {
        this.docId = docId;
        this.frequency = frequency;
    }
    
    public void increment() {
        this.frequency++;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public boolean containsDoc(String docId) {
        if(docId.equalsIgnoreCase(this.docId)) {
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
        final FrequencyDoc other = (FrequencyDoc) obj;
        if (!this.docId.equalsIgnoreCase(other.docId)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(FrequencyDoc df) {
        if(this.getFrequency()> df.getFrequency()){
                return -1;
        } else if (this.getFrequency() < df.getFrequency()){
                return 1;
        } else {
                return 0;
        }
    }
    
    public void print() {
        System.out.println(docId + " : " + frequency);
    }
    
}
