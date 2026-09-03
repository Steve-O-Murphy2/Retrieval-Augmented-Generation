import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        Path docsPath = Paths.get("src/main/resources/docs");

        try (Stream<Path> paths = Files.list(docsPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);

                            System.out.println("===== " + path.getFileName() + " =====");
                            System.out.println(content);
                            System.out.println();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}