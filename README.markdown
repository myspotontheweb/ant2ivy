
Introduction
============
The purpose of this groovy script is to "kick-start" a Java project's use of
the ivy plugin for ANT.

Usage
=====

    $ groovy ant2ivy --help
    usage: ant2ivy
     -a,--artifactid <arg>   Module artifactid
     -g,--groupid <arg>      Module groupid
     -h,--help               Show usage information
     -r,--nexusUrl <arg>     Alternative Nexus repository URL
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

Jar files which cannot be identified are placed into a local directory which 
ivy is configured to pull from.

Generates the following files

**ivy.xml**

    <ivy-module version='2.0'>
      <info organisation='com.example' module='demo' />
      <configurations defaultconfmapping='default' />
      <dependencies>
       <dependency org='org.apache.tomcat' name='tomcat-annotations-api' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-catalina-ant' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-catalina-ha' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-tribes' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-catalina' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-el-api' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-jasper-el' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-jasper' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-jsp-api' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-servlet-api' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-api' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-coyote' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-dbcp' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-i18n-es' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-i18n-fr' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-i18n-ja' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-jdbc' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='org.apache.tomcat' name='tomcat-util' rev='7.0.21' conf='default-&gt;master' />
       <dependency org='NA' name='ecj-3.7.jar' rev='NA' />
     </dependencies>
   </ivy-module>

**ivysettings.xml**

    <ivysettings>
      <settings defaultResolver='maven-repos' />
      <resolvers>
        <chain name='maven-repos'>
          <ibiblio name='central' m2compatible='true' />
          <ibiblio name='spring-external' m2compatible='true' root='http://repository.springsource.com/maven/bundles/external' />
        </chain>
        <filesystem name='local'>
          <artifact pattern='/home/mark/build/jars/[artifact]' />
        </filesystem>
      </resolvers>
      <modules>
        <module organisation='NA' name='ecj-3.7.jar' resolver='local' />
      </modules>
    </ivysettings>


