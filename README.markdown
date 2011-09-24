
Introduction
============
The purpose of this groovy script is to "kick-start" a Java project's use of
the ivy plugin for ANT.

Parameters
==========

    $ groovy ant2ivy --help
    usage: ant2ivy
     -a,--artifactid <arg>   Module artifactid
     -g,--groupid <arg>      Module groupid
     -h,--help               Show usage information
     -s,--sourcedir <arg>    Source directory containing jars
     -t,--targetdir <arg>    Target directory where write ivy build files

Example
-------
The following example scans tomcat's libraries and generates the ivy files to download them from Maven Central

    $ groovy ant2ivy -g com.example -a demo -s /opt/apache-tomcat-7.0.21/lib -t build
    13 [main] INFO Ant2Ivy - Searching: http://repository.sonatype.org ...
    3910 [main] WARN Ant2Ivy - Not Found: ecj-3.7.jar
    8025 [main] INFO Ant2Ivy - Generating ant file: /home/mark/build/build.xml ...
    8036 [main] INFO Ant2Ivy - Generating ivy file: /home/mark/build/ivy.xml ...
    8051 [main] INFO Ant2Ivy - Generating ivy settings file: /home/mark/build/ivysettings.xml ...
         [copy] Copying 1 file to /home/mark/build/jars

