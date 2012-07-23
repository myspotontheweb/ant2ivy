
// Dependencies
// ============
import groovy.util.GroovyTestCase
import groovy.xml.MarkupBuilder
import org.custommonkey.xmlunit.*

@Grapes(
    @Grab(group='xmlunit', module='xmlunit', version='1.3')
)

// Tests
// =====
class Test extends GroovyTestCase {

    def ant = new AntBuilder()

    void setUp() {
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)

        ant.mkdir(dir:"downloads")
    }

    void tearDown() {
        ant.delete(dir:"tmp")
        ant.delete(dir:"work")
    }

    void testTomcatDistro() {
        //
        // Download the tomcat distro
        //
        ant.get(src:"http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.21/bin/apache-tomcat-7.0.21.zip", dest:"downloads/apache-tomcat-7.0.21.zip", usetimestamp:"true")
        ant.unzip(src:"downloads/apache-tomcat-7.0.21.zip", dest:"tmp")

        //
        // Run the groovy script against tomcat installation
        //
        //def test = "cmd /c groovy ant2ivy -g com.example -a demo -s tmp/apache-tomcat-7.0.21/lib -t work".execute()
        def test = "groovy ant2ivy -g com.example -a demo -s tmp/apache-tomcat-7.0.21/lib -t work".execute()

        def output = new StringBuffer()
        test.waitForProcessOutput(output, output)

        assert !test.exitValue() : output

        //
        // Check unidentified files are setup as ivy repository
        //
        assert new File("work/lib/ecj-3.7.jar").exists()

        //
        // Check build.xml
        //
        def buildFile = """
        <project name='Sample ivy build' default='resolve' xmlns:ivy='antlib:org.apache.ivy.ant'>
            <target name='install' description='Install ivy'>
                <mkdir dir='\${user.home}/.ant/lib' />
                <get dest='\${user.home}/.ant/lib/ivy.jar' src='http://search.maven.org/remotecontent?filepath=org/apache/ivy/ivy/2.2.0/ivy-2.2.0.jar' />
            </target>
            <target name='resolve' description='Resolve 3rd party dependencies'>
                <ivy:resolve />
            </target>
            <target name='clean' description='Remove all build files'>
                <ivy:cleancache />
            </target>
        </project>
        """

        assert new Diff(buildFile, new File("work/build.xml").text).similar()

        //
        // Check ivy.xml
        //
        def ivyFile = """
        <ivy-module version='2.0'>
            <info organisation='com.example' module='demo' />
            <configurations defaultconfmapping='compile->master(default)'>
                <conf name='compile' description='Compile dependencies' />
                <conf name='runtime' description='Runtime dependencies' extends='compile' />
                <conf name='test' description='Test dependencies' extends='runtime' />
            </configurations>
            <dependencies>
                <dependency org='org.apache.tomcat' name='tomcat-annotations-api' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-catalina-ant' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-catalina-ha' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-tribes' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-catalina' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-el-api' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-jasper-el' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-jasper' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-jsp-api' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-servlet-api' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-api' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-coyote' rev='7.0.21' />
                <dependency org='org.apache.tomcat' name='tomcat-dbcp' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-i18n-es' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-i18n-fr' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-i18n-ja' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-jdbc' rev='7.0.21'/>
                <dependency org='org.apache.tomcat' name='tomcat-util' rev='7.0.21'/>
                <dependency org='NA' name='ecj-3.7.jar' rev='NA' />
            </dependencies>
        </ivy-module>
        """

        assert new Diff(ivyFile, new File("work/ivy.xml").text).similar()

        //
        // Check ivysettings.xml
        //
        def ivysettingsFile = """
        <ivysettings>
            <settings defaultResolver='maven-repos' />
            <resolvers>
                <chain name='maven-repos'>
                    <ibiblio name='central' m2compatible='true' />
                </chain>
                <filesystem name='local'>
                    <artifact pattern='\${ivy.settings.dir}/lib/[artifact]' />
                </filesystem>
            </resolvers>
            <modules>
                <module organisation='NA' resolver='local' />
            </modules>
        </ivysettings>
        """

        assert new Diff(ivysettingsFile, new File("work/ivysettings.xml").text).similar()

        //
        // Check pom.xml
        //
        def pomFile = """
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <groupId>com.example</groupId>
            <artifactId>demo</artifactId>
            <packaging>jar</packaging>
            <version>1.0-SNAPSHOT</version>
            <dependencies>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-annotations-api</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-catalina-ant</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-catalina-ha</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-tribes</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-catalina</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-el-api</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-jasper-el</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-jasper</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-jsp-api</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-servlet-api</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-api</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-coyote</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-dbcp</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-i18n-es</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-i18n-fr</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-i18n-ja</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-jdbc</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.tomcat</groupId>
                    <artifactId>tomcat-util</artifactId>
                    <version>7.0.21</version>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>NA</groupId>
                    <artifactId>ecj-3.7.jar</artifactId>
                    <version>NA</version>
                    <scope>compile</scope>
                </dependency>
            </dependencies>
        </project>
        """

        assert new Diff(pomFile, new File("work/pom.xml").text).similar()
    }
}

