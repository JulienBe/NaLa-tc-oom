import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(OomLabJobs)
    subProject(ClampTc)
}


object ClampTc : Project({
    name = "Clamp-tc"

    vcsRoot(ClampTc_GitGithubComJulienBeNaLaTcGitRefsHeadsMaster)

    buildType(ClampTc_Build)
})

object ClampTc_Build : BuildType({
    name = "Build"

    vcs {
        root(ClampTc_GitGithubComJulienBeNaLaTcGitRefsHeadsMaster)
    }

    triggers {
        vcs {
        }
    }
})

object ClampTc_GitGithubComJulienBeNaLaTcGitRefsHeadsMaster : GitVcsRoot({
    name = "git@github.com:JulienBe/NaLa-tc.git#refs/heads/master"
    url = "git@github.com:JulienBe/NaLa-tc.git"
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "sshkey"
    }
})


object OomLabJobs : Project({
    name = "Oom-Lab-Jobs"

    vcsRoot(OomLabJobs_GerritOom)
    vcsRoot(OomLabJobs_HttpsGithubComChrisCAttTcOomLabJobsGitRefsHeadsMaster)

    buildType(OomLabJobs_OomDeployClamp)
    buildType(OomLabJobs_update_oom)
})

object OomLabJobs_OomDeployClamp : BuildType({
    name = "oom deploy clamp"
    description = "update helm repository with latest oom charts from master"

    vcs {
        root(OomLabJobs_HttpsGithubComChrisCAttTcOomLabJobsGitRefsHeadsMaster)
    }

    steps {
        script {
            name = "make onap"
            scriptContent = "cd kubernetes"
        }
    }

    triggers {
        vcs {
        }
    }

    requirements {
        contains("system.agent.name", "ip_10.0.0.151")
    }
})

object OomLabJobs_update_oom : BuildType({
    name = "oom update"
    description = "update helm repository with latest oom charts from master"

    vcs {
        root(OomLabJobs_HttpsGithubComChrisCAttTcOomLabJobsGitRefsHeadsMaster)
        root(OomLabJobs_GerritOom)
    }

    steps {
        script {
            name = "make onap"
            scriptContent = """
                cd kubernetes
                helm serve &
                make all
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    requirements {
        contains("system.agent.name", "ip_10.0.0.151")
    }
})

object OomLabJobs_GerritOom : GitVcsRoot({
    name = "gerrit_oom"
    pollInterval = 86400
    url = "https://gerrit.onap.org/r/oom"
})

object OomLabJobs_HttpsGithubComChrisCAttTcOomLabJobsGitRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/ChrisC-att/tc-oom-lab-jobs.git#refs/heads/master"
    url = "https://github.com/ChrisC-att/tc-oom-lab-jobs.git"
    pushUrl = "https://github.com/ChrisC-att/tc-oom-lab-jobs.git"
})
