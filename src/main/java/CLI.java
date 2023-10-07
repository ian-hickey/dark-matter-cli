import org.apache.commons.cli.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

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
        var templateMap = new HashMap<String, String>() {{
            put("rest", "ExampleResource.cfc");
            put("entity", "ExampleEntity.cfc");
            put("todo", "Todo.cfc");
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
                    + "src"+ File.separator + "main" + File.separator + "cfscript" + File.separator
                    + packageName.replace(".", File.separator));
            Files.createDirectories(directoryPath);

            // Copy the template file
            String tempPath = projectName + File.separator + "src"+ File.separator + "main" +
                    File.separator + "resources" + File.separator + "templates" + File.separator +
                    templateMap.get(template);
            InputStream resourceStream = CLI.class.getResourceAsStream(
                    "templates/" + templateMap.get(template));

            if (resourceStream != null) {
                // Copy the example to the new project
                Path templatePath = Paths.get(tempPath);
                Path destinationPath = directoryPath.resolve(templatePath.getFileName());
                Files.copy(resourceStream, destinationPath);

                // Update the groupid and artifact id in the POM file
                var pomPath = projectName + File.separator + "pom.xml"+ File.separator;
                var pomTemplatePath = Paths.get(pomPath);
                String content = new String(Files.readAllBytes(pomTemplatePath));
                content = content.replace("<groupId>org.ionatomics.darkmatter</groupId>", "<groupId>" + packageName + "</groupId>");
                content = content.replace("<artifactId>starter</artifactId>", "<artifactId>" + projectName + "</artifactId>");
                Files.write(pomTemplatePath, content.getBytes());

                // Update the app name in the readme.md file
                var rmPath = projectName + File.separator + "README.md"+ File.separator;
                var rmTemplatePath = Paths.get(rmPath);
                content = new String(Files.readAllBytes(rmTemplatePath));
                content = content.replace("# dark matter starter", "# "
                        + projectName.substring(0, 1).toUpperCase() + projectName.substring(1)
                        + " - Dark Matter + Quarkus");
                Files.write(rmTemplatePath, content.getBytes());

                var osCommand = "";
                if (System.getProperty("os.name").contains("Win")){
                    osCommand = "`.\\start-dev.bat`";
                }else{
                    osCommand = "`sudo ./start-dev.sh`";
                }
                System.out.println(projectName + " is ready! use `cd " + projectName + "`");
                System.out.println("To start Dark Matter + Quarkus: " + osCommand);
                System.out.println("See the project README.md for more detailed instructions. Enjoy!");
            }else{
                System.err.println("Unable to find template! Please recheck the -t option and try again.");
            }

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

        Option dbOption = new Option("d", "db", true,
                "Database Type (mysql, mariadb, postgresql, oracle, h2, derby, mssql, db2). ");
        dbOption.setRequired(false);
        options.addOption(dbOption);
        return options;
    }
}
