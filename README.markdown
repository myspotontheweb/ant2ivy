

Introduction
============
The purpose of this groovy script is to "kick-start" a Java project's use of
the [ivy plugin](https://ant.apache.org/ivy/) for ANT.

Note:

* This tool can also generate a Maven POM and a Gradle build file, but these tools are limited to dependencies tht can be retrieved from a Maven repository.

Usage
=====

    $ groovy ant2ivy --help
    usage: ant2ivy
     -a,--artifactid <arg>   Module artifactid
     -g,--groupid <arg>      Module groupid
     -h,--help               Show usage information
     -r,--mavenUrl <arg>     Alternative Maven repository URL
     -s,--sourcedir <arg>    Source directory containing jars
     -t,--targetdir <arg>    Target directory where write ivy build files

Example
-------
The script can be run as follows, specifying source and target directories

```
$ groovy /path/to/ant2ivy.groovy -a example -g com.example -s lib_full_jars -t dependencies
```

Command generates the following files in the target directory:
    
```
dependencies
├── build.xml
├── ivy.xml
├── ivysettings.xml
├── lib
│   ├── cannotmatch1.jar
│   ├── cannotmatch2.jar
│   └── cannotmatch3.jar
├── build.gradle
└── pom.xml
```

The following files are associated with ANT and the [dependency management tool ivy](https://ant.apache.org/ivy/) 

* build.xml
* ivy.xml
* ivysettings.xml

The ivy settings file will configure a "local" repository pointing at the "lib" directory which will contain a copy of the jar files that could not be found in [Maven Central](https://search.maven.org/) 

The following files are a bonus, containing the dependencies that were matched.

* build.gradle
* pom.xml
