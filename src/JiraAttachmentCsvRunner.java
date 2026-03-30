import java.io.BufferedReader;
import java.io.FileReader;

public class JiraAttachmentCsvRunner {

    private static final String CSV_PATH =
            "C:/Users/aruna.ramesh.kumar/OneDrive - Accenture/Desktop/Cmigration/Jira Java Attachment Load.csv";

    public static void main(String[] args) {

        System.out.println("🚀 CSV Attachment Migration – START");

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(CSV_PATH))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    System.out.println("ℹ️ Skipping CSV header: " + line);
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] cols = line.split(",");

                if (cols.length < 2) {
                    System.out.println("⚠️ Invalid CSV row: " + line);
                    continue;
                }

                String sourceKey =
                        cols[0].replace("\uFEFF", "").trim();
                String targetKey =
                        cols[1].trim();

                System.out.println(
                        "➡ Processing CSV row: [" + sourceKey + "] → [" + targetKey + "]");

                // ✅ THIS IS THE ONLY CALL THAT MATTERS
                JiraIssueBulkAttachmentMigratorCsv.migrate(
                        sourceKey,
                        targetKey
                );

                System.out.println("✅ Completed row: " + sourceKey);
            }

        } catch (Exception e) {
            System.out.println("❌ CSV Runner failed");
            e.printStackTrace();
        }

        System.out.println("🎉 CSV Attachment Migration – END");
    }
}
