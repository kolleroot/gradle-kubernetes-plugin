Gradle Kubernetes Plugin
========================

The purpose of the `gradle-kubernetes-plugin` is to improve the development
process of creating and deploying kubernetes applications.

## Requirements
This plugin requires a valid docker installation, credentials for a kubernetes
cluster and the kubernetes client tools to work properly.

The docker installation is required to build, tag and push your application
to a docker registry, the credentials are used to communicate with the cluster
and the kubernetes client (`kubectl`) is required to `port-forward` to the
docker registry in the cluster.

The credentials are expected to be at `$HOME/.kube/config`, the default
location for the kubernetes credentials.

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

## Example

This _minimalistic_ example defines a docker image `simpleImage` which will be
pushed to the docker registry `localhost:5000` and a kubernetes pod named
`simpleApplication`. This is currently as minimalistic, as possible, but I hope
that in the future there will be more automated configuration and less typing.

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

### Caveats
This plugin uses the new rule based model configuration mechanism in gradle
([link](https://docs.gradle.org/current/userguide/software_model.html)). If
you aren't familiar with this technique, it shouldn't be any problem. But
there are a view caveats.

* When creating a kubernetes api object, you have to specify the type of the
  object. All these objects are in the package `com.github.kolleroot.gradle.kubernetes.model.api`
  and have a class name in the following format: `VersionKind` like `V1Pod` and
  `V1beta1Deployment`.

* The kubernetes api sometimes uses empty objects to represent, that an object
  was set but has no explicit properties. Gradle on the other hand can't
  distinguish between configured and unconfigured objects, as far as I know.
  To resolv this issue you have to set the boolean property `preserve` to
  `true`. This property won't be in the final JSON but tells the serializer to
  preserve this empty object.

* There are two types of arrays: the one with primitive elements like strings
  and numbers and the one with objects like `V1Container`.
  
    * Primitive arrays:
    
        `externalIPs = ["192.168.2.100", "192.168.2.101"]`
  
    * Object arrays:
    
        ```groovy
        containers {
            create {
                name = "main"
                ...
            }
            create {
                name = "backup"
                ...
            }
        }
        ```
* There are also two types of maps with similar differences. They are too
divided into primitive and object maps.
    * Primitive maps:
    
        `labels = [app: 'RestApi', 'env': 'production']`
    
    * Object maps:
    
        ```groovy
        kubernetesObjects {
            'rest-api-deployment'(V1beta1Deployment) {
                ...
            }
        }
        ```
        
        Quotation marks are only required, if the name isn't a valid method
        name in groovy.

## License

This plugin is made available under the
[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).
