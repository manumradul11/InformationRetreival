
package bm25;

public class RankDoc implements Comparable<RankDoc> {
    private String docId;
    private double score;

    public RankDoc(String docId, double score) {
        this.docId = docId;
        this.score = score;
    }
    

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RankDoc other = (RankDoc) obj;
        if (this.docId != other.docId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return docId + "\t" + score;
    }

    
    
    public int compareTo(RankDoc bm) {
        if(this.getScore() > bm.getScore()){
                return -1;
        } 
        else if (this.getScore() < bm.getScore())
        {
                return 1;
        } else {
                return 0;
        }
    }
    
    
}


