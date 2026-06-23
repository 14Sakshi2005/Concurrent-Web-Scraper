import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Scraper {

    private static final ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();
    private static final Semaphore rateLimiter = new Semaphore(3);

    public static void main(String[] args) {
        // --- TEST WEBSITES ---
        urlQueue.add("https://www.google.com");
        urlQueue.add("https://www.wikipedia.org");
        urlQueue.add("https://www.github.com");
        urlQueue.add("https://www.isro.gov.in");
        urlQueue.add("https://www.india.gov.in");
        urlQueue.add("https://www.bseindia.com");
        urlQueue.add("https://www.ndtv.com");
        urlQueue.add("https://www.moneycontrol.com");
        urlQueue.add("https://www.bbc.com");
        urlQueue.add("https://www.nasa.gov");
        urlQueue.add("https://www.nytimes.com");
        urlQueue.add("https://www.reddit.com");
        urlQueue.add("https://www.amazon.com");

        // Your requested new print statement:
        System.out.println("--- Starting the Scraper ---");

        ExecutorService executor = Executors.newFixedThreadPool(4);

        while (!urlQueue.isEmpty()) {
            String url = urlQueue.poll();
            if (url != null) {
                executor.execute(() -> downloadWebsite(url));
            }
        }

        executor.shutdown();
    }

    private static void downloadWebsite(String urlString) {
        try {
            rateLimiter.acquire();
            // FIXED: Using .threadId() instead of .getId()
            System.out.println("[START] Thread " + Thread.currentThread().threadId() + " is downloading: " + urlString);

            // FIXED: Using URI.create().toURL() instead of new URL()
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);

            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                in.readLine(); 
                in.close();
                System.out.println("[SUCCESS] Thread " + Thread.currentThread().threadId() + " finished " + urlString + " with Code: " + responseCode);
            } else {
                System.out.println("[WARNING] Thread " + Thread.currentThread().threadId() + " hit an issue on " + urlString + " with Code: " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Thread failed to download " + urlString + " -> " + e.getMessage());
        } finally {
            rateLimiter.release();
        }
    }
}