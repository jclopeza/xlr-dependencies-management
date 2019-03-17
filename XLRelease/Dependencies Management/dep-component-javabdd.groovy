// Exported from:        http://kubuntu:5516/#/templates/Folder1a787388922e47c08684a591a930e33e-Release70d87726338d4127a9a510af79a2eb06/releasefile
// XL Release version:   8.5.3
// Date created:         Sun Mar 17 18:00:11 CET 2019

xlr {
  template('dep-component-javabdd') {
    folder('Dependencies Management')
    variables {
      stringVariable('sha') {
        required false
        showOnReleaseStart false
      }
      stringVariable('email') {
        required false
        showOnReleaseStart false
      }
      stringVariable('message') {
        required false
        showOnReleaseStart false
      }
      stringVariable('build-number') {
        required false
        showOnReleaseStart false
      }
      stringVariable('build-status') {
        required false
        showOnReleaseStart false
      }
      stringVariable('component-version') {
        
      }
      stringVariable('id.release.webapp') {
        required false
        showOnReleaseStart false
      }
      stringVariable('id.task.webapp') {
        required false
        showOnReleaseStart false
      }
      stringVariable('id.phase.webapp') {
        required false
        showOnReleaseStart false
      }
    }
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2018-09-17T09:00:00+0200')
    scriptUsername 'admin'
    scriptUserPassword '{aes:v0}1QvVqAxaF3r4uu+DwAJAH4ls1aoCIGXzRzrRpxYH9AA='
    phases {
      phase('COMMIT STAGE') {
        color '#08B153'
        tasks {
          manual('Start pipeline') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Getting info from Github') {
            script {
              type 'webhook.JsonWebhook'
              'URL' 'https://api.github.com/repos/jclopeza/javabdd/branches/master'
              result variable('sha')
              result2 variable('email')
              result3 variable('message')
              jsonPathExpression 'commit.sha'
              jsonPathExpression2 'commit.commit.author.email'
              jsonPathExpression3 'commit.commit.message'
            }
          }
          custom('Build component javabdd') {
            script {
              type 'jenkins.Build'
              jenkinsServer 'Jenkins'
              jobName 'javabdd'
              jobParameters 'CHANGELOG=${message}\n' +
                            'EMAIL=${email}'
              buildNumber variable('build-number')
              buildStatus variable('build-status')
            }
          }
          custom('Check analysis and quality gate in Sonar') {
            taskRecoverOp com.xebialabs.xlrelease.domain.recover.TaskRecoverOp.RUN_SCRIPT
            script {
              type 'sonar.checkCompliance'
              sonarServer 'Sonar'
              resource 'com.xebialabs.congruencias:javabdd'
            }
          }
        }
      }
      phase('RELEASE') {
        color '#991C71'
        tasks {
          notification('Notify consumers new javabdd version available') {
            addresses 'consumers-javabdd@xlrelease.com'
            subject 'There is a new javabdd version available'
            body 'There is a new javabdd version available whit build number ${build-number}'
          }
          gate('Allow to use the new library in webapp') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Modify pom.xml in webapp') {
            script {
              type 'remoteScript.Unix'
              script './manage-dependencies.sh -v ${component-version}-B${build-number}'
              remotePath '/opt/xebialabs/devops-utilities'
              address 'localhost'
              username 'jcla'
              password '{aes:v0}tVuWQSyCGQot4v/lahsiFn1zDIbUHDUkPpjK1Hy0Aew='
            }
          }
        }
      }
      phase('RESTART BUILD WEBAPP') {
        color '#FD8D10'
        tasks {
          sequentialGroup('Get release info') {
            tasks {
              custom('Get release Id') {
                script {
                  type 'webhook.JsonWebhook'
                  'URL' 'http://localhost:5516/api/v1/releases/byTitle?releaseTitle=webapp%20${component-version}'
                  username 'admin'
                  password '{aes:v0}MF0eeQSLku+i3m6+nvRR3y57kH1Gq5JEALeUHc6oaeU='
                  result variable('id.release.webapp')
                  jsonPathExpression '[-1].id'
                }
              }
              custom('Get phase Id') {
                script {
                  type 'webhook.JsonWebhook'
                  'URL' 'http://localhost:5516/api/v1/phases/byTitle?phaseTitle=COMMIT%20STAGE&releaseId=${id.release.webapp}'
                  username 'admin'
                  password '{aes:v0}1bL8tMH/djV4OKGMDcbHoxVuxm01lq0EXC7hNQD9U1g='
                  result variable('id.phase.webapp')
                  jsonPathExpression '[-1].id'
                }
              }
              custom('Get task Id') {
                script {
                  type 'webhook.JsonWebhook'
                  'URL' 'http://localhost:5516/api/v1/tasks/byTitle?taskTitle=Getting%20info%20from%20Github&phaseTitle=COMMIT%20STAGE&releaseId=${id.release.webapp}'
                  username 'admin'
                  password '{aes:v0}ggUMcQgJRNJuoxdJ+VUHFEiWY/RR5y6G+r0BN41Dluw='
                  result variable('id.task.webapp')
                  jsonPathExpression '[-1].id'
                }
              }
            }
          }
          custom('Restarting build phase in webapp') {
            script {
              type 'webhook.JsonWebhook'
              'URL' 'http://localhost:5516/api/v1/releases/${id.release.webapp}/restart?fromPhaseId=${id.phase.webapp}&fromTaskId=${id.task.webapp}&phaseVersion=LATEST&resume=false'
              method 'POST'
              username 'admin'
              password '{aes:v0}xFajjxrVnForpE5fOd3Lwb9SLN2SDs7Lx0pj1e7J9Ec='
            }
          }
        }
      }
      phase('PRO') {
        color '#D94C3D'
        tasks {
          gate('Allow this library to run into Production') {
            owner 'admin'
            plannedDuration 90000
          }
        }
      }
    }
    releaseTriggers {
      gitPoll('javabdd') {
        releaseTitle 'javabdd - new version ${commitId}'
        enabled false
        variables {
          stringVariable('component-version') {
            value '1.6.0'
          }
        }
      }
    }
    extensions {
      dashboard('Dashboard') {
        parentId 'Applications/Folder1a787388922e47c08684a591a930e33e/Release70d87726338d4127a9a510af79a2eb06'
        owner 'admin'
        tiles {
          releaseProgressTile('Release progress') {
            col 2
          }
          timelineTile('Release timeline') {
            
          }
          jenkinsBuildsTile('Jenkins builds') {
            row 0
            col 1
          }
          sonarSummaryTile('SonarQube analysis summary') {
            row 0
            col 0
            width 1
            height 1
            sonarServer 'Sonar'
            resource 'com.xebialabs.congruencias:javabdd'
            metrics 'minor_violations':'Minor Issues','code_smells':'Code Smells','major_violations':'Major Issues','ncloc':'Lines of Code'
          }
        }
      }
    }
    
  }
}