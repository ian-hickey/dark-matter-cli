import org.apache.commons.cli.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

public class CLI {

    public static void main(String[] args) {
        var templateMap = new HashMap<String, String>() {{
            put("resource", "ExampleResource.cfc");
            put("entity", "ExampleEntity.cfc");
            put("todo", "Todo.cfc");
        }};
        Options options = new Options();
        Option projectNameOption = new Option("p", "project-name", true, "Project name. No spaces. Lowercase. " +
                "(todo, my-todo-app)");
        projectNameOption.setRequired(true);
        options.addOption(projectNameOption);

        Option packageNameOption = new Option("p", "package-name", true, "Package name. " +
                "This is normally a domain backwards. org.ionatomics, org.acme, com.ed.ian.");
        packageNameOption.setRequired(true);
        options.addOption(packageNameOption);

        Option templateOption = new Option("t", "template", true, "Name of the example to add (rest, entity, etc). " +
                "See a full list on Github.");
        templateOption.setRequired(true);
        options.addOption(templateOption);

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

        try {
            // Clone the git repository
            Git git = Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(new File(projectName))
                    .call();
            git.close();

            // Create the directory structure
            Path directoryPath = Paths.get(projectName + File.separator
                    + "src"+ File.separator + "main " + File.separator + "cfscript" + File.separator
                    + packageName.replace(".", File.separator));
            Files.createDirectories(directoryPath);

            // Copy the template file
            String tempPath = projectName + File.separator + "src"+ File.separator + "main " +
                    File.separator + "resources" + File.separator + "templates" + File.separator +
                    templateMap.get(template);

            Path templatePath = Paths.get(tempPath);
            Path destinationPath = directoryPath.resolve(templatePath.getFileName());
            Files.copy(templatePath, destinationPath);

            // Update the groupid and artifact id in the POM file
            tempPath = projectName + File.separator + "POM.xml"+ File.separator;
            templatePath = Paths.get(tempPath);
            destinationPath = directoryPath.resolve(templatePath.getFileName());
            String content = new String(Files.readAllBytes(destinationPath));
            content = content.replace("<groupId>org.ionatomics.darkmatter</groupId>", "<groupId>" + packageName + "</groupId>");
            content = content.replace("<artifactId>starter</artifactId>", "<artifactId>" + projectName + "</artifactId>");
            Files.write(destinationPath, content.getBytes());

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }
}
