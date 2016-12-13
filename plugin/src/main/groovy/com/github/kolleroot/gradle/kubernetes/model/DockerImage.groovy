package com.github.kolleroot.gradle.kubernetes.model

import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.kolleroot.gradle.kubernetes.helper.DockerImageFileBundleCounter
import org.gradle.api.Named
import org.gradle.model.Managed
import org.gradle.model.ModelSet
import org.gradle.model.Unmanaged

/**
 * The base interface for docker images source
 *
 * Each image consists of a list of instructions and a list of {@link FileBundle FileBundles}.
 */
@Managed
interface DockerImage extends Named {
    List<String> getInstructions()

    ModelSet<FileBundle> getBundles()
}

/**
 * A bundle of files relative to the project root and a name by which it will be referenced in the Dockerfile.
 */
@Managed
interface FileBundle {
    String getBundleName()

    void setBundleName(String name)

    @Unmanaged
    Closure getSpec()

    void setSpec(Closure spec)
}

/**
 * Each function adds an instruction to the list of instructions.
 *
 * These instructinos are from {@link com.bmuschko.gradle.docker.tasks.image.Dockerfile}
 *
 * @see com.bmuschko.gradle.docker.tasks.image.Dockerfile
 */
@Managed
@SuppressWarnings('AbstractClassWithoutAbstractMethod')
abstract class DefaultDockerImage implements DockerImage {
    // @formatter:off

    /**
     * Adds a full instruction as String.
     *
     * Example:
     *
     * <pre>
     * task createDockerfile(type: Dockerfile) {
     *     instruction 'FROM ubuntu:14.04'
     *     instruction 'MAINTAINER Benjamin Muschko "benjamin.muschko@gmail.com"'
     * }
     * </pre>
     *
     * @param instruction Instruction as String
     */
    void instruction(String instruction) {
        instructions << new Dockerfile.GenericInstruction(instruction).build()
    }

    /**
     * Adds a full instruction as Closure with return type String.
     *
     * Example:
     *
     * <pre>
     * task createDockerfile(type: Dockerfile) {
     *     instruction { 'FROM ubuntu:14.04' }
     *     instruction { 'MAINTAINER Benjamin Muschko "benjamin.muschko@gmail.com"' }
     * }
     * </pre>
     *
     * @param instruction Instruction as Closure
     */
    void instruction(Closure instruction) {
        instructions << new Dockerfile.GenericInstruction(instruction).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#from">FROM instruction</a> sets the Base Image for
     * subsequent instructions.
     *
     * @param image Base image name
     */
    void from(String image) {
        instructions << new Dockerfile.FromInstruction(image).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#from">FROM instruction</a> sets the Base Image for
     * subsequent instructions.
     *
     * @param image Base image name
     */
    void arg(String arg) {
        instructions << new Dockerfile.ArgInstruction(arg).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#from">FROM instruction</a> sets the Base Image for
     * subsequent instructions.
     *
     * @param image Base image name
     */
    void from(Closure image) {
        instructions << new Dockerfile.FromInstruction(image).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#maintainer">MAINTAINER instruction</a> allows you to set
     * the Author field of the generated images.
     *
     * @param maintainer Maintainer
     */
    void maintainer(String maintainer) {
        instructions << new Dockerfile.MaintainerInstruction(maintainer).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#maintainer">MAINTAINER instruction</a> allows you to set
     * the Author field of the generated images.
     *
     * @param maintainer Maintainer
     */
    void maintainer(Closure maintainer) {
        instructions << new Dockerfile.MaintainerInstruction(maintainer).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#run">RUN instruction</a> will execute any commands in a
     * new layer on top of the current image and commit the results.
     *
     * @param command Command
     */
    void runCommand(String command) {
        instructions << new Dockerfile.RunCommandInstruction(command).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#run">RUN instruction</a> will execute any commands in a
     * new layer on top of the current image and commit the results.
     *
     * @param command Command
     */
    void runCommand(Closure command) {
        instructions << new Dockerfile.RunCommandInstruction(command).build()
    }

    /**
     * The main purpose of a <a href="https://docs.docker.com/reference/builder/#cmd">CMD instruction</a> is to provide
     * defaults for an executing container.
     *
     * @param command Command
     */
    void defaultCommand(String... command) {
        instructions << new Dockerfile.DefaultCommandInstruction(command).build()
    }

    /**
     * The main purpose of a <a href="https://docs.docker.com/reference/builder/#cmd">CMD instruction</a> is to provide
     * defaults for an executing container.
     *
     * @param command Command
     */
    void defaultCommand(Closure command) {
        instructions << new Dockerfile.DefaultCommandInstruction(command).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#expose">EXPOSE instruction</a> informs Docker that the
     * container will listen on the specified network ports at runtime.
     *
     * @param ports Ports
     */
    void exposePort(Integer... ports) {
        instructions << new Dockerfile.ExposePortInstruction(ports).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#expose">EXPOSE instruction</a> informs Docker that the
     * container will listen on the specified network ports at runtime.
     *
     * @param ports Ports
     */
    void exposePort(Closure ports) {
        instructions << new Dockerfile.ExposePortInstruction(ports).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#env">ENV instruction</a> sets the environment variable
     * <key> to the value <value>. This value will be passed to all future RUN instructions.
     *
     * @param key Key
     * @param value Value
     */
    void environmentVariable(String key, String value) {
        instructions << new Dockerfile.EnvironmentVariableInstruction(key, value).build()
    }

    void addFiles(String dest, Closure copySpec) {
        String baseName
        if(dest.split('/').size() > 0) {
            baseName = dest.split('/').last()
        } else {
            baseName = 'root'
        }
        String name = "${baseName}-${DockerImageFileBundleCounter.nextUnique}.zip"

        bundles.create {
            bundleName = name
            spec = copySpec
        }

        instructions << new Dockerfile.AddFileInstruction(name, dest).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#add">ADD instruction</a> copies new files, directories
     * or remote file URLs from <src> and adds them to the filesystem of the container at the path <dest>.
     *
     * @param src Source file
     * @param dest Destination path
     */
    void addFile(String src, String dest) {
        instructions << new Dockerfile.AddFileInstruction(src, dest).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#add">ADD instruction</a> copies new files, directories
     * or remote file URLs from <src> and adds them to the filesystem of the container at the path <dest>.
     *
     * @param src Source file
     * @param dest Destination path
     */
    void addFile(Closure src, Closure dest) {
        instructions << new Dockerfile.AddFileInstruction(src, dest).build()
    }

    /**
     * An <a href="https://docs.docker.com/reference/builder/#copy">ENTRYPOINT</a> allows you to configure a container
     * that will run as an executable.
     *
     * @param entryPoint Entry point
     */
    void entryPoint(String... entryPoint) {
        instructions << new Dockerfile.EntryPointInstruction(entryPoint).build()
    }

    /**
     * An <a href="https://docs.docker.com/reference/builder/#entrypoint">ENTRYPOINT</a> allows you to configure a
     * container that will run as an executable.
     *
     * @param entryPoint Entry point
     */
    void entryPoint(Closure entryPoint) {
        instructions << new Dockerfile.EntryPointInstruction(entryPoint).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#volume">VOLUME instruction</a> will create a mount point
     * with the specified name and mark it as holding externally mounted volumes from native host or other containers.
     *
     * @param volume Volume
     */
    void volume(String... volume) {
        instructions << new Dockerfile.VolumeInstruction(volume).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#volume">VOLUME instruction</a> will create a mount point
     * with the specified name and mark it as holding externally mounted volumes from native host or other containers.
     *
     * @param volume Volume
     */
    void volume(Closure volume) {
        instructions << new Dockerfile.VolumeInstruction(volume).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#user">USER instruction</a> sets the user name or UID to
     * use when running the image and for any RUN, CMD and ENTRYPOINT instructions that follow it in the Dockerfile.
     *
     * @param user User
     */
    void user(String user) {
        instructions << new Dockerfile.UserInstruction(user).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#user">USER instruction</a> sets the user name or UID to
     * use when running the image and for any RUN, CMD and ENTRYPOINT instructions that follow it in the Dockerfile.
     *
     * @param user User
     */
    void user(Closure user) {
        instructions << new Dockerfile.UserInstruction(user).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#workdir">WORKDIR instruction</a> sets the working
     * directory for any RUN, CMD and ENTRYPOINT instructions that follow it in the Dockerfile.
     *
     * @param dir Directory
     */
    void workingDir(String dir) {
        instructions << new Dockerfile.WorkDirInstruction(dir).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#workdir">WORKDIR instruction</a> sets the working
     * directory for any RUN, CMD and ENTRYPOINT instructions that follow it in the Dockerfile.
     *
     * @param dir Directory
     */
    void workingDir(Closure dir) {
        instructions << new Dockerfile.WorkDirInstruction(dir).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#onbuild">ONBUILD instruction</a> adds to the image a
     * trigger instruction to be executed at a later time, when the image is used as the base for another build.
     *
     * @param instruction Instruction
     */
    void onBuild(String instruction) {
        instructions << new Dockerfile.OnBuildInstruction(instruction).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#onbuild">ONBUILD instruction</a> adds to the image a
     * trigger instruction to be executed at a later time, when the image is used as the base for another build.
     *
     * @param instruction Instruction
     */
    void onBuild(Closure instruction) {
        instructions << new Dockerfile.OnBuildInstruction(instruction).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#label">LABEL instruction</a> adds metadata to an image.
     *
     * @param labels Labels
     */
    void label(Map<String, String> labels) {
        instructions << new Dockerfile.LabelInstruction(labels).build()
    }

    /**
     * The <a href="https://docs.docker.com/reference/builder/#label">LABEL instruction</a> adds metadata to an image.
     *
     * @param labels Labels
     */
    void label(Closure labels) {
        instructions << new Dockerfile.LabelInstruction(labels).build()
    }

    // formatter:on
}
