package com.github.kolleroot.gradle.kubernetes.internal

import org.gradle.api.Project

/**
 * Created by stefan on 17.11.16.
 */
abstract class ProjectToDocker {

    def project;

    public ProjectToDocker(Project project) {
        this.project = project
    }

    abstract String getBaseImage();

    abstract List<Tuple2<String, String>> getFiles();
}

class JarProjectToDocker extends ProjectToDocker {

    JarProjectToDocker(Project project) {
        super(project);
    }

    @Override
    String getBaseImage() {
        return ""
    }

    @Override
    List<Tuple2<String, String>> getFiles() {
        return null;
    }
}
