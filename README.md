Gradle-Kubernetes Plugin
========================

The purpose of the `gradle-kubernetes-plugin` is to improve the development
process of creating and deploying kubernetes applications.

## Usage
This plugin is available via the gradle plugin repository.

Build script snippet for use in **all** Gradle versions:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.kolleroot.gradle.kubernetes:gradle-kubernetes-plugin:0.1.0"
  }
}

apply plugin: "com.github.kolleroot.gradle.kubernetes"
```

Build script snippet for new, incubating, plugin mechanism introduced in **Gradle 2.1**:

```groovy
plugins {
  id "com.github.kolleroot.gradle.kubernetes" version "0.1.0"
}
```

## Usage

This plugin uses the new rule based model configuration mechanism in gradle
([link](https://docs.gradle.org/current/userguide/software_model.html)). If
you aren't familiar with this technique, it shouldn't be any problem. But
there are a view caveats.

```groovy
import com.github.kolleroot.gradle.kubernetes.model.DefaultDockerImage
import com.github.kolleroot.gradle.kubernetes.model.api.V1Pod

plugins {
  id application
  id "com.github.kolleroot.gradle.kubernetes" version "0.1.0"
}

repositories {
    jcenter()
}

model {
    kubernetes {
        dockerImages {
            simpleImage(DefaultDockerImage) {
                from 'openjdk:8-jre'
                addFiles '/usr/bin/local', {
                    from "$buildDir/distrubutions/example-application-project-1.0.zip"
                }
                entrypoint '/usr/bin/local/example-application-project/bin/example-application-project'
            }
        }

        dockerRegistries {
            'localhost:5000'(KubernetesLocalDockerRegistry) {
                namespace = 'docker'
                pod = 'registry'
                port = '5000:5000'
            }
        }

        kubernetesObjects {
            simpleApplication(V1Pod) {
                apiVersion = 'v1'
                kind = 'Pod'
                metadata {
                    name = 'test'
                }
                spec {
                    containers {
                        create {
                            image = 'localhost:5000/simpleImage'
                            volumeMounts.create {
                                name = 'data'
                                mountPath = '/data'
                            }
                        }
                    }
                    volumes {
                        create {
                            name = 'data'
                            emptyDir { preserve = true }
                        }
                    }
                }
            }
        }
    }
}
```

## License

This plugin is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).
