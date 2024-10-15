import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class manageHiScores{ //class suggested by ChatGPT, changes made to fit our functionality better. Comments are original

    public static List<HiScore> getHiScores(){
        List<HiScore> hiScores = new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("hiScores.txt"))) {
            hiScores = (List<HiScore>) ois.readObject();
        } catch (FileNotFoundException | EOFException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        Collections.sort(hiScores);

        return hiScores;
    }


    public static void updateHiScores(HiScore newHiScore) {
        List<HiScore> hiScores = getHiScores();

        hiScores.add(newHiScore);
        Collections.sort(hiScores);

        if (hiScores.size() > 5) {
            List<HiScore> hiScoresTemp = new ArrayList<>(hiScores.subList(0, 5)); //stores the top 5 hiScores in a temporary list, then stores it to the main list
            hiScores.clear();
            hiScores.addAll(hiScoresTemp);
        }


        writeHiScores(hiScores);
    }


    public static void writeHiScores(List<HiScore> hiScores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("hiScores.txt"))) {
            oos.writeObject(hiScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}