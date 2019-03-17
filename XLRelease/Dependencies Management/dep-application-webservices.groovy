// Exported from:        http://kubuntu:5516/#/templates/Folder1a787388922e47c08684a591a930e33e-Release0145b28d8a3249858a8ddaa6f7d0c57c/releasefile
// XL Release version:   8.5.3
// Date created:         Sun Mar 17 14:08:20 CET 2019

xlr {
  template('dep-application-webservices') {
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
      stringVariable('last-version') {
        required false
        showOnReleaseStart false
      }
      stringVariable('next-version') {
        required false
        showOnReleaseStart false
      }
      stringVariable('component-version') {
        
      }
    }
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2018-09-17T09:00:00+0200')
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
              'URL' 'https://api.github.com/repos/jclopeza/webservices/branches/master'
              result variable('sha')
              result2 variable('email')
              result3 variable('message')
              jsonPathExpression 'commit.sha'
              jsonPathExpression2 'commit.commit.author.email'
              jsonPathExpression3 'commit.commit.message'
            }
          }
          custom('Build component webservices') {
            script {
              type 'jenkins.Build'
              jenkinsServer 'Jenkins'
              jobName 'webservices'
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
              resource 'com.xebialabs.congruencias:webservices'
            }
          }
        }
      }
      phase('NEW VERSION IN XL-DEPLOY') {
        color '#FD8D10'
        tasks {
          custom('Get latest version') {
            script {
              type 'xld.GetLatestVersion'
              server 'XL Deploy'
              applicationId 'Applications/Calculator/webservices'
              stripApplications false
              packageId variable('last-version')
            }
          }
          externalScript('Calculate next version') {
            url 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/settingNewDarVersion.py'
          }
          custom('Create DAR and import to XL-Deploy') {
            script {
              type 'remoteScript.Unix'
              script '/opt/xebialabs/devops-utilities/dar-maker.sh -a webservices -v ${next-version}'
              address 'localhost'
              username 'jcla'
              password '{aes:v0}Q8BYn7qbXyJbVTGvKj+XJb0zjfhqxc2bVTgOXnPCs0E='
            }
          }
          gate('Release to PRE') {
            owner 'admin'
            plannedDuration 90000
          }
        }
      }
      phase('PRE') {
        color '#0099CC'
        tasks {
          gate('Ok Test Manager') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Update Ok Test Manager') {
            script {
              type 'xld.UpdateCIProperty'
              server 'XL Deploy'
              ciID 'Applications/Calculator/webservices/${next-version}'
              ciProperty 'satisfiesOkTestManager'
              propertyValue 'true'
            }
          }
          gate('Release to PRO') {
            owner 'admin'
            plannedDuration 90000
          }
        }
      }
      phase('PRO') {
        color '#0099CC'
        tasks {
          gate('Ok Release Manager') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Update Ok Release Manager') {
            script {
              type 'xld.UpdateCIProperty'
              server 'XL Deploy'
              ciID 'Applications/Calculator/webservices/${next-version}'
              ciProperty 'satisfiesOkReleaseManager'
              propertyValue 'true'
            }
          }
        }
      }
    }
    releaseTriggers {
      gitPoll('webservices') {
        releaseTitle 'webservices - new version ${commitId}'
        enabled false
        variables {
          stringVariable('component-version') {
            
          }
        }
      }
    }
    extensions {
      dashboard('Dashboard') {
        parentId 'Applications/Folder1a787388922e47c08684a591a930e33e/Release0145b28d8a3249858a8ddaa6f7d0c57c'
        owner 'admin'
        tiles {
          releaseProgressTile('Release progress') {
            col 2
          }
          timelineTile('Release timeline') {
            
          }
          sonarSummaryTile('SonarQube analysis summary') {
            row 0
            col 0
            width 1
            height 1
            sonarServer 'Sonar'
            resource 'com.xebialabs.congruencias:webservices'
            metrics 'minor_violations':'Minor Issues','code_smells':'Code Smells','major_violations':'Major Issues','ncloc':'Lines of Code'
          }
          jenkinsBuildsTile('Jenkins builds') {
            row 0
            col 1
          }
        }
      }
    }
    
  }
}