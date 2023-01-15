package ch.pricemeier.d2lang_plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Mojo(name = "build-diagrams", defaultPhase = LifecyclePhase.COMPILE)
public class D2LangPlugin extends AbstractMojo {

    @Parameter(property = "themeId")
    Integer themeId;

    @Parameter(property = "inputPath", required = true)
    String inputPath;

    @Parameter(property = "outputPath", required = true)
    String outputPath;

    @Parameter(property = "d2langBinaryPath", required = true)
    String d2langBinaryPath;

    @Parameter(property = "customArgs")
    String customArgs;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            try (Stream<Path> stream = Files.walk(Paths.get(inputPath))) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(f -> f.toString().endsWith(".d2"))
                        .forEach(this::process);
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void process(final Path file) {
        final Path baseInputPath = Paths.get(inputPath);
        final Path baseOutputPath = Paths.get(outputPath);
        final Path outputPath = convertToOutputPath(file, baseInputPath, baseOutputPath);
        Path svgOutputPath = replaceExtension(outputPath);
        try {
            String d2langCLI = constructD2LangCLICommand(file, svgOutputPath);
            Files.createDirectories(svgOutputPath.getParent());
            executeD2LangCommand(d2langCLI);
        } catch (IOException | InterruptedException e) {
            getLog().error(e);
        }
    }

    private void executeD2LangCommand(String d2langCLI) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(d2langCLI);
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
        String line;
        getLog().debug("Standard Output:");
        while ((line = stdoutReader.readLine()) != null) {
            getLog().debug(line);
        }
        getLog().debug("Standard Error:");
        while ((line = stderrReader.readLine()) != null) {
            getLog().debug(line);
        }
        int exitCode = process.waitFor();
        getLog().debug("Process exit code: " + exitCode);
    }

    private String constructD2LangCLICommand(Path file, Path svgOutputPath) {
        String d2langCLI = String.format("%s %s %s", d2langBinaryPath, file, svgOutputPath);
        if (themeId != null) {
            getLog().info("Using themeId: " + themeId);
            d2langCLI += String.format(" -t %d", themeId);
        }
        if (customArgs != null && customArgs.trim().length() > 0) {
            getLog().info("Using customArgs: " + customArgs);
            d2langCLI += String.format(" %s", customArgs);
        }
        getLog().info("Executing: " + d2langCLI);
        return d2langCLI;
    }

    private static Path convertToOutputPath(Path file, Path baseInputPath, Path baseOutputPath) {
        final Path relativeSubpath = baseInputPath.relativize(file);
        final Path outputPath = baseOutputPath.resolve(relativeSubpath);
        return outputPath;
    }

    private static Path replaceExtension(Path outputPath) {
        String d2OutputFilename = outputPath.getFileName().toString();
        String svgOutputFilename = d2OutputFilename.substring(0, d2OutputFilename.lastIndexOf(".")) + ".svg";
        Path svgOutputPath = outputPath.resolveSibling(svgOutputFilename);
        return svgOutputPath;
    }
}
