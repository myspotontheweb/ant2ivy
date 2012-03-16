
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

    }

    void tearDown() {
        ant.delete(dir:"tmp")
        ant.delete(dir:"tomcat")
    }

    void testTomcatDistro() {
        //
        // Download the tomcat distro
        //
        ant.get(src:"http://www.apache.org/dist/tomcat/tomcat-7/v7.0.26/bin/apache-tomcat-7.0.26.zip", dest:"downloads/tomcat.zip", usetimestamp:"true")
        ant.unzip(src:"downloads/tomcat.zip", dest:"tmp")

        //
        // Run the groovy script against tomcat installation
        //
        "groovy ant2ivy -g com.example -a demo -s tmp/apache-tomcat-7.0.26/lib -t tomcat".execute().waitFor()

        //
        // Check build.xml
        //
        def buildFile = """
        <project name='Sample ivy build' default='resolve' xmlns:ivy='antlib:org.apache.ivy.ant'>
            <target name='resolve'>
                <ivy:resolve />
            </target>

            <target name='clean'>
                <ivy:cleancache />
            </target>
        </project>
        """

        assert new Diff(buildFile, new File("tomcat/build.xml").text).similar()

        //
        // Check ivy.xml
        //
        def ivyFile = """
        <ivy-module version='2.0'>
            <info organisation='com.example' module='demo' />
            <configurations defaultconfmapping='default' />
            <dependencies>
                <dependency org='org.apache.tomcat' name='tomcat-annotations-api' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-catalina-ant' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-catalina-ha' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-tribes' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-catalina' rev='7.0.26' conf='default->master' />
                <dependency org='org.eclipse.jetty.orbit' name='org.eclipse.jdt.core' rev='3.7.1' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-el-api' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-jasper-el' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-jasper' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-jsp-api' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-servlet-api' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-api' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-coyote' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-dbcp' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-i18n-es' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-i18n-fr' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-i18n-ja' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-jdbc' rev='7.0.26' conf='default->master' />
                <dependency org='org.apache.tomcat' name='tomcat-util' rev='7.0.26' conf='default->master' />
            </dependencies>
        </ivy-module>
        """

        assert new Diff(ivyFile, new File("tomcat/ivy.xml").text).similar()

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
            </resolvers>
        </ivysettings>
        """

        assert new Diff(ivysettingsFile, new File("tomcat/ivysettings.xml").text).similar()
    }
}

