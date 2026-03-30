import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JiraIssueBulkAttachmentMigratorCsv {

    // ========= CONFIG =========
    private static final String SOURCE_BASE_URL = "Your_Source_Instance_Link";
    private static final String SOURCE_EMAIL = "Your_Email";
    private static final String SOURCE_API_TOKEN = "Your_Jira_API_Token";

    private static final String TARGET_BASE_URL = "Your_Target_Instance_Link";
    private static final String TARGET_EMAIL = "Your_Email";
    private static final String TARGET_API_TOKEN = "Your_Jira_API_Token";

    // ✅ Thread pool (optimized)
    private static final ExecutorService executor = Executors.newFixedThreadPool(8);

    // ✅ File size limit (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public static void migrate(String sourceIssueKey, String targetIssueKey) throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        String sourceAuth = Base64.getEncoder()
                .encodeToString((SOURCE_EMAIL + ":" + SOURCE_API_TOKEN)
                        .getBytes(StandardCharsets.UTF_8));

        String targetAuth = Base64.getEncoder()
                .encodeToString((TARGET_EMAIL + ":" + TARGET_API_TOKEN)
                        .getBytes(StandardCharsets.UTF_8));

        System.out.println("\n🔎 Migrating: " + sourceIssueKey + " → " + targetIssueKey);

        // ---------- FETCH ----------
        HttpRequest issueRequest = HttpRequest.newBuilder()
                .uri(URI.create(SOURCE_BASE_URL + "/rest/api/3/issue/" + sourceIssueKey + "?fields=attachment"))
                .header("Authorization", "Basic " + sourceAuth)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> issueResponse = sendWithRetry(client, issueRequest);

        JsonNode attachments = mapper.readTree(issueResponse.body())
                .path("fields")
                .path("attachment");

        if (!attachments.isArray() || attachments.size() == 0) {
            System.out.println("ℹ️ No attachments found");
            return;
        }

        System.out.println("📎 Found " + attachments.size() + " attachments");

        // ---------- PARALLEL PROCESS ----------
        for (JsonNode attachment : attachments) {
            executor.submit(() -> processAttachment(
                    attachment,
                    client,
                    sourceAuth,
                    targetAuth,
                    targetIssueKey
            ));
        }
    }

    // ================= PROCESS EACH FILE =================
    private static void processAttachment(
            JsonNode attachment,
            HttpClient client,
            String sourceAuth,
            String targetAuth,
            String targetIssueKey
    ) {

        String attachmentId = attachment.get("id").asText();
        String fileName = attachment.get("filename").asText();
        long fileSize = attachment.get("size").asLong();

        try {
            System.out.println("➡️ Processing: " + fileName);

            // ✅ SIZE CHECK
            if (fileSize > MAX_FILE_SIZE) {
                System.out.println("⚠️ Skipped (Too Large): " + fileName);
                return;
            }

            // ---------- DOWNLOAD ----------
            System.out.println("⬇ Downloading: " + fileName);

            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SOURCE_BASE_URL + "/rest/api/3/attachment/content/" + attachmentId))
                    .header("Authorization", "Basic " + sourceAuth)
                    .GET()
                    .build();

            HttpResponse<InputStream> downloadResponse =
                    client.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());

            InputStream sourceStream = downloadResponse.body();

            // ---------- STREAM ----------
            String boundary = "----Boundary" + System.currentTimeMillis();

            PipedOutputStream pipeOut = new PipedOutputStream();
            PipedInputStream pipeIn = new PipedInputStream(pipeOut);

            // ✅ FIXED: Start thread properly
            Thread streamThread = new Thread(() -> {
                try (OutputStream out = pipeOut;
                     InputStream in = sourceStream) {

                    String header = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                            "Content-Type: application/octet-stream\r\n\r\n";

                    out.write(header.getBytes(StandardCharsets.UTF_8));

                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    String closing = "\r\n--" + boundary + "--\r\n";
                    out.write(closing.getBytes(StandardCharsets.UTF_8));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            streamThread.start(); // ✅ IMPORTANT FIX

            // ---------- SMALL DELAY (avoid rate limit) ----------
            Thread.sleep(200);

            // ---------- UPLOAD ----------
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_BASE_URL + "/rest/api/3/issue/" + targetIssueKey + "/attachments"))
                    .header("Authorization", "Basic " + targetAuth)
                    .header("X-Atlassian-Token", "no-check")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> pipeIn))
                    .build();

            HttpResponse<String> uploadResponse = sendWithRetry(client, uploadRequest);

            int status = uploadResponse.statusCode();

            if (status == 200 || status == 201) {
                System.out.println("⬆ Uploaded: " + fileName);
            } else {
                System.out.println("❌ Failed: " + fileName + " | HTTP " + status);
            }

        } catch (Exception e) {
            System.out.println("💥 Error: " + fileName);
            e.printStackTrace();
        }
    }

    // ================= RETRY =================
    private static HttpResponse<String> sendWithRetry(
            HttpClient client,
            HttpRequest request
    ) throws Exception {

        int maxRetries = 3;
        int attempt = 0;
        long waitTime = 1000;

        while (attempt < maxRetries) {

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 200 || status == 201) {
                return response;
            }

            if (status == 429 || status >= 500) {
                System.out.println("⚠️ Retry " + (attempt + 1) + " HTTP " + status);
                Thread.sleep(waitTime);
                waitTime *= 2;
                attempt++;
            } else {
                return response;
            }
        }

        throw new RuntimeException("❌ Failed after retries");
    }
}
