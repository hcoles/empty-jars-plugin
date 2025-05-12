package org.pitest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Component; // Added import
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Generates empty sources and javadoc jars to meet Maven Central requirements.
 * Previously maven central checks could be disabled with a flag, but that is no longer supported.
 */
@Mojo(name = "empty-jars", defaultPhase = LifecyclePhase.PACKAGE)
public class EmptyJarsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String finalName;


    @Component
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException {

        if (!"pom".equals(project.getPackaging())) {
            createJars();
        }

        createJars();
    }

    private void createJars() throws MojoExecutionException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        try {
            File sourcesJar = createEmptyJar("sources");
            File javadocJar = createEmptyJar("javadoc");

            this.projectHelper.attachArtifact(project, "jar", "sources", sourcesJar); // Used injected helper
            this.projectHelper.attachArtifact(project, "jar", "javadoc", javadocJar); // Used injected helper

            getLog().info("Empty sources JAR created: " + sourcesJar.getAbsolutePath());
            getLog().info("Empty javadoc JAR created: " + javadocJar.getAbsolutePath());

        } catch (IOException e) {
            throw new MojoExecutionException("Error creating empty JARs", e);
        }
    }

    private File createEmptyJar(String classifier) throws IOException {
        File jarFile = new File(outputDirectory, finalName + "-" + classifier + ".jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), new Manifest())) {
            // An empty JAR just needs a manifest, which is created by default by JarOutputStream
            // No entries need to be added for an empty JAR.
            JarEntry entry = new JarEntry("README.txt");
            jos.putNextEntry(entry);
            jos.write("No content is provided".getBytes());
            jos.closeEntry();
        }

        return jarFile;
    }

}

