/*

   ant2ivy 
   -------
   A script to "kick-start" java projects using the ivy plug-in 

   License
   -------

   Copyright 2011 Mark O'Connor

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
//
// Dependencies
// ============

import groovy.xml.MarkupBuilder

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Grapes([
    @Grab(group='org.slf4j', module='slf4j-simple', version='1.6.2') 
])

//
// Classes
// =======

class Ant2Ivy {

    Logger log = LoggerFactory.getLogger(this.class.name);
    String groupId
    String artifactId
    String repoUrl

    Ant2Ivy(groupId, artifactId, repoUrl) {
        this.groupId = groupId
        this.artifactId = artifactId
        this.repoUrl = repoUrl

        log.debug "groupId: {}, artifactId: {}", groupId, artifactId
    }

    //
    // Given a directory, find all jar and search Nexus
    // based on the file's checksum
    //
    // Return a data structure containing the GAV coordinates of each jar
    //
    def search(File inputDir) {
        def results = [:]
        results["found"] = []
        results["missing"] = []

        log.info "Searching: {} ...", repoUrl

        def ant = new AntBuilder()
        ant.fileset(id:"jars", dir:inputDir.absolutePath, includes:"**/*.jar")

        ant.project.references.jars.each {
            def jar = new File(inputDir, it.name)

            // Checksum URL
            ant.checksum(file:jar.absolutePath, algorithm:"SHA1", property:jar.name)

            def searchUrl = "${repoUrl}/service/local/data_index?sha1=${ant.project.properties[jar.name]}"
            log.debug "SearchUrl: {}, File: {}", searchUrl, jar.name

            // Search for the first result
            def searchResults = new XmlParser().parseText(searchUrl.toURL().text)
            def artifact = searchResults.data.artifact[0]

            if (artifact) {
                log.debug "Found: {}", jar.name
                results["found"].add([file:jar.name, groupId:artifact.groupId.text(), artifactId:artifact.artifactId.text(), version:artifact.version.text()])
            }
            else {
                log.warn "Not Found: {}", jar.name
                results["missing"].add([file:jar.name, fileObj:jar])
            }
        }

        return results
    }

    //
    // Given an input direcory, search for the GAV coordinates 
    // and use this information to write two XML files:
    //
    // ivy.xml          Contains the ivy dependency declarations
    // ivysettings.xml  Resolver configuration
    //
    def generate(File inputDir, File outputDir) {
        outputDir.mkdir()

        def antFile = new File(outputDir, "build.xml")
        def ivyFile = new File(outputDir, "ivy.xml")
        def ivySettingsFile = new File(outputDir, "ivysettings.xml")
        def localRepo = new File(outputDir, "lib")
        def results = search(inputDir)

        //
        // Generate the ant build file
        //
        log.info "Generating ant file: {} ...", antFile.absolutePath
        def antContent = new MarkupBuilder(antFile.newPrintWriter())

        antContent.project(name: "Sample ivy build", default:"resolve", "xmlns:ivy":"antlib:org.apache.ivy.ant" ) {
            target(name:"resolve") {
                "ivy:resolve"()
            }
            target(name:"clean") {
                "ivy:cleancache"()
            }
        }

        // 
        // Generate the ivy file
        //
        log.info "Generating ivy file: {} ...", ivyFile.absolutePath
        def ivyConfig = new MarkupBuilder(ivyFile.newPrintWriter())

        ivyConfig."ivy-module"(version:"2.0") {
            info(organisation:this.groupId, module:this.artifactId) 
            configurations(defaultconfmapping:"default")
            dependencies() {
                results.found.each {
                    dependency(org:it.groupId, name:it.artifactId, rev:it.version, conf:"default->master")
                }
                results.missing.each {
                    dependency(org:"NA", name:it.file, rev:"NA")
                }
            }
        }

        // 
        // Generate the ivy settings file
        //
        log.info "Generating ivy settings file: {} ...", ivySettingsFile.absolutePath
        def ivySettings = new MarkupBuilder(ivySettingsFile.newPrintWriter())
        def ant = new AntBuilder()

        ivySettings.ivysettings() {
            settings(defaultResolver:"maven-repos") 
            resolvers() {
                chain(name:"maven-repos") {
                    // TODO: Make this list of Maven repos configurable
                    ibiblio(name:"central", m2compatible:"true")
                }
                if (results.missing.size() > 0) {
                    filesystem(name:"local") {
                        artifact(pattern:"\${ivy.settings.dir}/${localRepo.name}/[artifact]")
                    }
                }
            }
            if (results.missing.size() > 0) {
                modules() {
                    results.missing.each {
                        module(organisation:"NA", name:it.file, resolver:"local")
                        ant.copy(file:it.fileObj.absolutePath, tofile:"${localRepo.absolutePath}/${it.file}")
                    }
                }
            }
        }
    }
}

// 
// Main program
// ============
def cli = new CliBuilder(usage: 'ant2ivy')
cli.with {
    h longOpt: 'help', 'Show usage information'
    g longOpt: 'groupid',    args: 1, 'Module groupid', required: true
    a longOpt: 'artifactid', args: 1, 'Module artifactid', required: true
    s longOpt: 'sourcedir',  args: 1, 'Source directory containing jars', required: true
    t longOpt: 'targetdir',  args: 1, 'Target directory where write ivy build files', required: true
    r longOpt: 'nexusUrl',   args: 1, 'Alternative Nexus repository URL'
}
                                                                
def options = cli.parse(args)
if (!options) {
    return
}

if (options.help) {
    cli.usage()
}

def nexusUrl = (options.nexusUrl) ? options.nexusUrl : "http://repository.sonatype.org"

// 
// Generate ivy configuration
//
def ant2ivy = new Ant2Ivy(options.groupid, options.artifactid, nexusUrl)
ant2ivy.generate(new File(options.sourcedir), new File(options.targetdir))

