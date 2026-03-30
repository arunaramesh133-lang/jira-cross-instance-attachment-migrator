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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CSV-SPECIFIC MIGRATOR
 * ---------------------------------
 * This class is a SAFE DUPLICATE of the
 * working demo migrator.
 *
 * It supports dynamic source/target keys
 * and is ONLY used by CSV runner.
 *
 * Original demo migrator remains untouched.
 */
public class JiraIssueBulkAttachmentMigratorCsv {

    // ========= SOURCE CONFIG =========
    private static final String SOURCE_BASE_URL =
            "https://jiraplatformengineer.atlassian.net";
    private static final String SOURCE_EMAIL =
            "arunaramesh133@gmail.com";
    private static final String SOURCE_API_TOKEN =
            "ZZZ";
    // =================================

    // ========= TARGET CONFIG =========
    private static final String TARGET_BASE_URL =
            "https://jirademo112.atlassian.net";
    private static final String TARGET_EMAIL =
            "arunaramesh133@gmail.com";
    private static final String TARGET_API_TOKEN =
            "xxx";
    // =================================

    /**
     * Entry point used by CSV runner
     */
    public static void migrate(
            String sourceIssueKey,
            String targetIssueKey
    ) throws Exception {

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

        System.out.println(
                "🔎 CSV Migrating attachments: "
                        + sourceIssueKey + " → " + targetIssueKey);

        // ---------- FETCH ATTACHMENTS ----------
        HttpRequest issueRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        SOURCE_BASE_URL
                                + "/rest/api/3/issue/"
                                + sourceIssueKey
                                + "?fields=attachment"))
                .header("Authorization", "Basic " + sourceAuth)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> issueResponse =
                client.send(issueRequest,
                        HttpResponse.BodyHandlers.ofString());

        JsonNode attachments =
                mapper.readTree(issueResponse.body())
                        .path("fields")
                        .path("attachment");

        if (!attachments.isArray() || attachments.size() == 0) {
            System.out.println(
                    "ℹ️ No attachments found on " + sourceIssueKey);
            return;
        }

        System.out.println(
                "📎 Found " + attachments.size() + " attachments");

        // ---------- LOOP ATTACHMENTS ----------
        for (JsonNode attachment : attachments) {

            String attachmentId =
                    attachment.get("id").asText();
            String fileName =
                    attachment.get("filename").asText();

            System.out.println("➡️ Uploading: " + fileName);

            // ---------- DOWNLOAD STREAM ----------
            HttpRequest downloadRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    SOURCE_BASE_URL
                                            + "/rest/api/3/attachment/content/"
                                            + attachmentId))
                            .header("Authorization", "Basic " + sourceAuth)
                            .GET()
                            .build();

            HttpResponse<InputStream> downloadResponse =
                    client.send(downloadRequest,
                            HttpResponse.BodyHandlers.ofInputStream());

            InputStream sourceStream =
                    downloadResponse.body();

            // ---------- MULTIPART STREAM ----------
            String boundary =
                    "----Boundary" + System.currentTimeMillis();

            PipedOutputStream pipeOut =
                    new PipedOutputStream();
            PipedInputStream pipeIn =
                    new PipedInputStream(pipeOut);

            new Thread(() -> {
                try (OutputStream out = pipeOut;
                     InputStream in = sourceStream) {

                    String header =
                            "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=\"file\"; filename=\""
                                    + fileName + "\"\r\n" +
                                    "Content-Type: application/octet-stream\r\n\r\n";

                    out.write(
                            header.getBytes(StandardCharsets.UTF_8));

                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    String closing =
                            "\r\n--" + boundary + "--\r\n";
                    out.write(
                            closing.getBytes(StandardCharsets.UTF_8));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // ---------- UPLOAD ----------
            HttpRequest uploadRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    TARGET_BASE_URL
                                            + "/rest/api/3/issue/"
                                            + targetIssueKey
                                            + "/attachments"))
                            .header("Authorization", "Basic " + targetAuth)
                            .header("X-Atlassian-Token", "no-check")
                            .header("Content-Type",
                                    "multipart/form-data; boundary=" + boundary)
                            .POST(HttpRequest.BodyPublishers.ofInputStream(
                                    () -> pipeIn))
                            .build();

            HttpResponse<String> uploadResponse =
                    client.send(uploadRequest,
                            HttpResponse.BodyHandlers.ofString());

            if (uploadResponse.statusCode() == 200
                    || uploadResponse.statusCode() == 201) {
                System.out.println("✅ Uploaded: " + fileName);
            } else {
                System.out.println(
                        "❌ Failed: " + fileName
                                + " | HTTP "
                                + uploadResponse.statusCode());
            }
        }

        System.out.println(
                "🎉 CSV migration completed: "
                        + sourceIssueKey + " → " + targetIssueKey);
        System.out.println("DEBUG SOURCE URL = " + SOURCE_BASE_URL);
        System.out.println("DEBUG ISSUE KEY  = " + sourceIssueKey);

    }
}
