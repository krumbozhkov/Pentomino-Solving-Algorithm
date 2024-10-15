import java.io.Serial;
import java.io.Serializable;

public class HiScore implements Serializable, Comparable<HiScore> {

    @Serial
    private static final long serialVersionUID = 1L;
    public int score;
    public String initials;

    public HiScore(int scoreInput, String initialsInput) {
        score = scoreInput;
        initials = initialsInput;
    }


    public int getScore(){
        return score;
    }

    public String getInitials(){
        return initials;
    }

    public void setScore(int newScore){
        score = newScore;
    }

    public void setInitials(String newInitials){
        initials = newInitials;
    }

    public String convertToString(){
        return (initials + ".......... " + score);
    }


    @Override
    public int compareTo(HiScore o) { //suggested by chatGPT
        return Integer.compare(o.score, this.score);
    }
}


