
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlWithThread {

    public static int MAX_NUM_THREAD = 8;

    public static void main(String[] args) throws Exception {
        File file = new File("word_list.txt");
//        System.out.println(file.exists());
        Scanner sc = new Scanner(file);
        String url_sample = "https://en.oxforddictionaries.com/definition/";

        Map<String, HashSet<String>> wordList = new HashMap<>();

        ArrayList<Thread> threadList = new ArrayList<>();
        int count = 0;
        while (sc.hasNext()) {
            count++;
//            if (count > 100) {
//                break;
//            }
            String str = sc.nextLine();
            if (str.split("\\w+").length != 0) {
                System.out.println(str);
            } else {
                Thread wordThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            super.run(); //To change body of generated methods, choose Tools | Templates.

                            String wordUrl = url_sample + str;
                            Document doc;
                            while (true) {
                                try {

                                    doc = Jsoup.connect(wordUrl).get();
                                    break;
                                } catch (SocketTimeoutException e) {
                                    System.out.println("[TIMEOUT] " + wordUrl);
//                                    e.printStackTrace();
                                    return;
                                }
//                                try {
//                                    Thread.sleep(5000);
//                                } catch (InterruptedException ex) {
//                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                                }
                            }
                            Elements els = doc.select(".speaker");

                            HashSet<String> audioList = new HashSet<>();

                            for (int i = 0; i < els.size(); i++) {
                                Element e = els.get(i);
//                                System.out.println(e.html());
                                String audio_url = e.select("audio").attr("src");
                                System.out.println(audio_url);

                                Thread downloadThread = new Thread() {
                                    @Override
                                    public void run() {
                                        super.run(); //To change body of generated methods, choose Tools | Templates.
                                        try {
                                            downloadFile(audio_url, "downloads");
                                        } catch (IOException ex) {
                                            Logger.getLogger(CrawlWithThread.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }

                                };

//                                threadList.add(downloadThread);
                                downloadThread.start();

                                String FILE_NAME = audio_url.substring(audio_url.lastIndexOf('/') + 1, audio_url.length());
                                audioList.add(FILE_NAME);
                            }
                            wordList.put(str, audioList);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                threadList.add(wordThread);
                while (!addThread(threadList, wordThread)) {
                    Thread.sleep(5000);
                }
                wordThread.start();

            }
        }

        while (true) {
            if (isAllDone(threadList)) {
                break;
            }
            Thread.sleep(5000);
        }
        for (Map.Entry<String, HashSet<String>> entry : wordList.entrySet()) {
            String wordKey = entry.getKey();
            HashSet<String> audioSet = entry.getValue();
            StringBuilder audioList = new StringBuilder();
            for (String audioFile : audioSet) {
                String formatAudio = String.format("[sound:%s]", audioFile);
                audioList.append(formatAudio);
            }
            System.out.println(wordKey + " : " + audioList.toString());
        }

        System.out.println(wordList.size());
        String fileName = String.format("%s_anki.txt", file.getName());
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

    public static boolean isAllDone(ArrayList<Thread> threadList) {
        for (int i = 0; i < threadList.size(); i++) {
            Thread subThread = threadList.get(i);
            if (subThread.isAlive()) {
                return false;
            }
        }
        return true;
    }

    synchronized public static boolean addThread(ArrayList<Thread> threadList, Thread newThread) {
        if (threadList.size() < MAX_NUM_THREAD) {
            threadList.add(newThread);
            return true;
        }

        for (int i = 0; i < threadList.size(); i++) {
            Thread subThread = threadList.get(i);
            if (!subThread.isAlive()) {
                threadList.remove(i);
                threadList.add(newThread);
                return true;
            }
        }

        return false;
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
