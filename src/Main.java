
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File("word_list.txt");

//        System.setProperty("http.proxyHost", "proxy");
//        System.setProperty("http.proxyPort", "8080");
//
//        System.setProperty("https.proxyHost", "proxy");
//        System.setProperty("https.proxyPort", "8080");
//
//        System.setProperty("ftp.proxyHost", "proxy");
//        System.setProperty("ftp.proxyPort", "8080");
//        for (int i = 0; i < 10; i++) {
//            final int idx = i;
//            new Thread() {
//                @Override
//                public void run() {
//                    super.run(); //To change body of generated methods, choose Tools | Templates.
//                    try {
//                        scanFile(file, idx * 100, (idx + 1) * 100);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }.start();
//        }
        scanFile(file, 0, 2159);
    }

    public static void scanFile(File file, final int fromIdx, final int toIdx) throws Exception {

        Scanner sc = new Scanner(file);
        String url_sample = "https://en.oxforddictionaries.com/definition/";

        Map<String, HashSet<String>> wordList = new HashMap<>();

        int idx = 0;
        while (idx < fromIdx && sc.hasNext()) {
            idx++;
            sc.nextLine();
        }

        ArrayList<String> wordArrayList = new ArrayList<>();

        while (sc.hasNext() && (idx < toIdx)) {
            idx++;
            String str = sc.nextLine();
            if (str.split("\\w+").length != 0) {
            } else {
                wordArrayList.add(str);
            }
        }

        System.out.println(wordArrayList.size());
        for (String str : wordArrayList) {

//            System.out.println(str);
            String wordUrl = url_sample + str;
            Document doc;
            try {

                doc = Jsoup.connect(wordUrl).get();
            } catch (Exception e) {
                System.out.println("[ERROR] " + str);
                e.printStackTrace();
                continue;
            }
            Elements els = doc.select(".speaker");
            HashSet<String> audioList = new HashSet<>();

            for (int i = 0; i < els.size(); i++) {
                Element e = els.get(i);
                String audio_url = e.select("audio").attr("src");
                String FILE_NAME = audio_url.substring(audio_url.lastIndexOf('/') + 1, audio_url.length());
                audioList.add(FILE_NAME);
            }
            wordList.put(str, audioList);
        }
        String fileName = String.format("%s_%d_%d.txt", file.getName(), fromIdx, toIdx);
        File logFile = new File(fileName);

        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));

        for (Map.Entry<String, HashSet<String>> entry
                : wordList.entrySet()) {
            String wordKey = entry.getKey();
            HashSet<String> audioSet = entry.getValue();
            StringBuilder audioList = new StringBuilder();
            for (String audioFile : audioSet) {
                String formatAudio = String.format("[sound:%s]", audioFile);
                audioList.append(formatAudio);
            }
            String entryStr = String.format("%s|%s", wordKey, audioList.toString());
            System.out.println(entryStr);
            writer.write(entryStr);
            writer.write("\n");

        }
        writer.close();
    }

}
