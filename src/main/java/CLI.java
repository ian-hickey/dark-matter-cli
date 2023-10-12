import org.apache.commons.cli.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CLI {

    public static void main(String[] args) {
        String logo = """
                        ______   _______  ______    ___   _    __   __  _______  _______  _______  _______  ______  \s
                       |      | |   _   ||    _ |  |   | | |  |  |_|  ||   _   ||       ||       ||       ||    _ | \s
                       |  _    ||  |_|  ||   | ||  |   |_| |  |       ||  |_|  ||_     _||_     _||    ___||   | || \s
                       | | |   ||       ||   |_||_ |      _|  |       ||       |  |   |    |   |  |   |___ |   |_||_\s
                       | |_|   ||       ||    __  ||     |_   |       ||       |  |   |    |   |  |    ___||    __  |
                       |       ||   _   ||   |  | ||    _  |  | ||_|| ||   _   |  |   |    |   |  |   |___ |   |  | |
                       |______| |__| |__||___|  |_||___| |_|  |_|   |_||__| |__|  |___|    |___|  |_______||___|  |_|                                                                                                               \s
                """;
        //System.out.println(logo + "\uD83D\uDE80");
        var templateMap = new HashMap<String, ArrayList<String>>() {{
            put("rest", new ArrayList<>(){{ add("templates/ExampleResource.cfc"); }});
            put("entity", new ArrayList<>(){{
                add("templates/ProductResource.cfc");
                add("templates/model/Product.cfc");
                add("products.sql"); /* this goes into the resources base folder. */
            }});
            put("todo", new ArrayList<>(){{
                add("templates/TodoResource.cfc");
                add("templates/model/Todo.cfc");}});
        }};

        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("dark-matter-cli", options);
            System.exit(1);
            return;
        }

        String gitUrl = "https://github.com/ian-hickey/dark-matter-starter/";
        String projectName = cmd.getOptionValue("project-name");
        String packageName = cmd.getOptionValue("package-name");
        String template = cmd.getOptionValue("template");
        String dbType = cmd.getOptionValue("db");
        if (dbType == null || dbType.isEmpty()) {
            dbType = "mysql"; // default to mysql
        }

        if (!templateMap.containsKey(template)) {
            throw new RuntimeException("Unable to find the template. Please check the -t option and retry.");
        }

        try {
            // Clone the git repository
            ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", gitUrl, projectName);
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            // Create the directory structure
            Path directoryPath = Paths.get(projectName + File.separator
                    + "src" + File.separator + "main" + File.separator + "cfscript" + File.separator
                    + packageName.replace(".", File.separator));
            Files.createDirectories(directoryPath);

            // Copy the template file
            var exampleTemplates = templateMap.get(template);
            Path templatesBasePath = Paths.get(projectName, "src", "main", "resources");

            for (var exampleTemplate : exampleTemplates) {
                String tempPath = templatesBasePath.resolve(exampleTemplate.replace("templates/", "").replace("/", File.separator)).toString();
                InputStream resourceStream = CLI.class.getResourceAsStream(exampleTemplate);

                if (resourceStream != null) {
                    // Copy the example to the new project
                    Path baseDirectory = exampleTemplate.startsWith("templates/") ? directoryPath : templatesBasePath;
                    Path relativePathFromBase = templatesBasePath.relativize(Paths.get(tempPath));
                    Path destinationPath = baseDirectory.resolve(relativePathFromBase);

                    // Create any additional directories stored in the template config.
                    Files.createDirectories(destinationPath.getParent());  // We only want to create the parent directories
                    System.out.println("Creating: " + destinationPath);

                    // If this is entity example and the template we are processing is the ProductResource,
                    // we need to replace the package in the example
                    if (template.equals("entity") && exampleTemplate.equals("templates/ProductResource.cfc")) {
                        var content = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));

                        // Replace the desired content in that string
                        content = content.replace("${packageName}", packageName);

                        // Write the modified content to the correct file path
                        Files.writeString(destinationPath, content, StandardCharsets.UTF_8,
                                StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        Files.copy(resourceStream, destinationPath);
                    }

                } else {
                    System.err.println("Unable to find template! Please recheck the -t option and try again.");
                    System.exit(1);
                }
            }

            // Update the groupid and artifact id in the POM file
            var pomPath = projectName + File.separator + "pom.xml" + File.separator;
            var pomTemplatePath = Paths.get(pomPath);
            String content = new String(Files.readAllBytes(pomTemplatePath));
            content = content.replace("<groupId>org.ionatomics.darkmatter</groupId>", "<groupId>" + packageName + "</groupId>");
            content = content.replace("<artifactId>starter</artifactId>", "<artifactId>" + projectName + "</artifactId>");

            // Add the correct database dependency
            content = content.replace("mysql", dbType);//defaults to mysql
            Files.write(pomTemplatePath, content.getBytes());

            // Update the app name in the readme.md file
            var rmPath = projectName + File.separator + "README.md" + File.separator;
            var rmTemplatePath = Paths.get(rmPath);
            content = new String(Files.readAllBytes(rmTemplatePath));
            content = content.replace("# dark matter starter", "# "
                    + projectName.substring(0, 1).toUpperCase() + projectName.substring(1)
                    + " - Dark Matter + Quarkus");
            Files.write(rmTemplatePath, content.getBytes());

            // Update - Append lines to the application.properties depending on the example selected.
            if (template.equals("entity")) {
                // With the entity example, we need to append the sql file location.
                Path appPath = Paths.get(projectName,"src", "main", "resources", "application.properties");
                String propertiesPath = appPath.toAbsolutePath().toString();
                String lineToAdd = System.lineSeparator() + "quarkus.hibernate-orm.sql-load-script=products.sql";

                Path path = Paths.get(propertiesPath);
                try {
                    Files.write(path, lineToAdd.getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }

            // Print usage instructions
            var osCommand = "";
            if (System.getProperty("os.name").contains("Win")) {
                osCommand = "`.\\start-dev.bat`";
            } else {
                osCommand = "`sudo ./start-dev.sh`";
            }

            System.out.println(projectName + " is ready! use `cd " + projectName + "`");
            System.out.println("To start Dark Matter + Quarkus: " + osCommand);
            System.out.println("See the project README.md for more detailed instructions. Enjoy!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        Option projectNameOption = new Option("n", "project-name", true, "Project name. No spaces. Lowercase. " +
                "(todo, my-todo-app)");
        projectNameOption.setRequired(true);
        options.addOption(projectNameOption);

        Option packageNameOption = new Option("p", "package-name", true, "Package name. " +
                "This is normally your domain backwards. org.ionatomics, org.acme, com.ed.ian.");
        packageNameOption.setRequired(true);
        options.addOption(packageNameOption);

        Option templateOption = new Option("t", "template", true,
                "Name of the example to add (rest, entity, todo, etc). " +
                "See a full list on Github.");
        templateOption.setRequired(true);
        options.addOption(templateOption);

        Option dbOption = new Option("d", "db", true,
                "Database Type (mysql, mariadb, postgresql, oracle, h2, derby, mssql, db2). ");
        dbOption.setRequired(false);
        options.addOption(dbOption);
        return options;
    }

    public static String getDirectoryPath(String inputPath) {
        Path path = Paths.get(inputPath);
        Path parent = path.getParent();
        return parent == null ? "" : parent.toString();
    }
}
