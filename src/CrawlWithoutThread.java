
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlWithoutThread {

    public static void scanFile(File file, int fromIdx, int toIdx) throws Exception {

        Scanner sc = new Scanner(file);
        String url_sample = "https://en.oxforddictionaries.com/definition/";

        Map<String, HashSet<String>> wordList = new HashMap<>();

        int count = 0;
        while (sc.hasNext()) {
            count++;
            if (count > 100) {
                break;
            }
            String str = sc.nextLine();
            if (str.split("\\w+").length != 0) {
//                System.out.println(str);
            } else {
                String wordUrl = url_sample + str;
                Document doc = Jsoup.connect(wordUrl).get();

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
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File("word_list.txt");
        Scanner sc = new Scanner(file);
        String url_sample = "https://en.oxforddictionaries.com/definition/";

        Map<String, HashSet<String>> wordList = new HashMap<>();

        double totalConnectTime = 0;

        long startTime = System.nanoTime();

        int count = 0;
        while (sc.hasNext()) {
            count++;
//            if (count > 100) {
//                break;
//            }
            String str = sc.nextLine();
            if (str.split("\\w+").length != 0) {
//                System.out.println(str);
            } else {
                String wordUrl = url_sample + str;
                double connectStartTime = System.nanoTime() / 1000000000.0;
                Document doc = Jsoup.connect(wordUrl).get();
                double connectEndTime = System.nanoTime() / 1000000000.0;

                double deltaConnectTime = connectEndTime - connectStartTime;

                totalConnectTime += deltaConnectTime;
                System.out.println(String.format("%s : %f AVG : %f", str, deltaConnectTime, totalConnectTime / count));

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
        }

        long endTime = System.nanoTime();

        System.out.println(endTime - startTime);

//        for (Map.Entry<String, HashSet<String>> entry
//                : wordList.entrySet()) {
//            String wordKey = entry.getKey();
//            HashSet<String> audioSet = entry.getValue();
//            StringBuilder audioList = new StringBuilder();
//            for (String audioFile : audioSet) {
//                String formatAudio = String.format("[sound:%s]", audioFile);
//                audioList.append(formatAudio);
//            }
//            String entryStr = String.format("%s|%s", wordKey, audioList.toString());
//            System.out.println(entryStr);
//        }

        System.out.println(wordList.size());

        String fileName = String.format("%s_anki_fullList.txt", file.getName());
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

    public static int charNum(String str) {
        char[] arrs = str.toCharArray();
        int count = 0;
        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i] == '#') {
                count++;
            }
        }

        return count;
    }

    public static void downloadFile(final String FILE_URL, final String DOWNLOAD_PATH) throws MalformedURLException, IOException {
        File downloadFolder = new File(DOWNLOAD_PATH);
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }
        final String FILE_NAME = DOWNLOAD_PATH + '/' + FILE_URL.substring(FILE_URL.lastIndexOf('/') + 1, FILE_URL.length());

        if (new File(FILE_NAME).exists()) {
            return;
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(FILE_URL).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
