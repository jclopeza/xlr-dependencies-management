// Exported from:        http://kubuntu:5516/#/templates/Folder1a787388922e47c08684a591a930e33e-Releasead9d37c72cce402a8593d9992428edab/releasefile
// XL Release version:   8.5.3
// Date created:         Sun Mar 17 14:07:11 CET 2019

xlr {
  template('dep-application-front') {
    folder('Dependencies Management')
    variables {
      stringVariable('next-version') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-dev-out') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-dev-err') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-pre-out') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-pre-err') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-pro-out') {
        required false
        showOnReleaseStart false
      }
      stringVariable('ansible-pro-err') {
        required false
        showOnReleaseStart false
      }
      mapVariable('issues') {
        required false
        showOnReleaseStart false
      }
      stringVariable('last-version') {
        required false
        showOnReleaseStart false
      }
      stringVariable('component-version') {
        
      }
    }
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2018-09-18T09:00:00+0200')
    scriptUsername 'admin'
    scriptUserPassword '{aes:v0}ByT/1fmWZiOPux+kqgnyhWAiIneDhWIVpiTeENFAV3s='
    phases {
      phase('BUILD DAR') {
        color '#0099CC'
        tasks {
          custom('Get latest version') {
            script {
              type 'xld.GetLatestVersion'
              server 'XL Deploy'
              applicationId 'Applications/Calculator/front'
              stripApplications false
              packageId variable('last-version')
            }
          }
          externalScript('Calculate next version') {
            url 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/settingNewDarVersion.py'
          }
          manual('Allow to create the DAR file') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Create DAR and import to XL-Deploy') {
            script {
              type 'remoteScript.Unix'
              script '/opt/xebialabs/devops-utilities/dar-maker.sh -a front -v ${next-version}'
              address 'localhost'
              username 'jcla'
              password '{aes:v0}v/QxUVidgq+X9MnoOa8KFngo5etmcpxu2wdQjlbKxdw='
            }
          }
        }
      }
      phase('DEV') {
        color '#08B153'
        tasks {
          manual('Complete this task to deploy to DEV') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Deploy to DEV') {
            facets {
              facet {
                type 'udm.DeploymentTaskFacet'
                applicationId 'Configuration/environmentManagement/Application3ed3511896fb41ef9b15472d332def6f'
                environmentId 'Configuration/environmentManagement/Environmentdd9d7454a6f044e4b8619ab5fab0a58a'
                version '${next-version}'
              }
            }
            script {
              type 'xldeploy.Deploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deploymentApplication 'Applications/Calculator/front'
              deploymentVersion '${next-version}'
              deploymentPackage 'Applications/Calculator/front/${next-version}'
              deploymentEnvironment 'Environments/Calculator/calculator-dev'
            }
          }
          gate('Release to PRE') {
            owner 'admin'
          }
        }
      }
      phase('PRE') {
        color '#991C71'
        tasks {
          gate('Ok Test Manager') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Update Ok Test Manager') {
            script {
              type 'xld.UpdateCIProperty'
              server 'XL Deploy'
              ciID 'Applications/Calculator/front/${next-version}'
              ciProperty 'satisfiesOkTestManager'
              propertyValue 'true'
            }
          }
          custom('Deploy to PRE') {
            facets {
              facet {
                type 'udm.DeploymentTaskFacet'
                applicationId 'Configuration/environmentManagement/Application3ed3511896fb41ef9b15472d332def6f'
                environmentId 'Configuration/environmentManagement/Environment24456c6be9d9453eb4405e5885f8976c'
                version '${next-version}'
              }
            }
            script {
              type 'xldeploy.Deploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deploymentApplication 'Applications/Calculator/front'
              deploymentVersion '${next-version}'
              deploymentPackage 'Applications/Calculator/front/${next-version}'
              deploymentEnvironment 'Environments/Calculator/calculator-pre'
            }
          }
          gate('Release to PRO') {
            owner 'admin'
            plannedDuration 90000
          }
        }
      }
      phase('PRO') {
        color '#FD8D10'
        tasks {
          gate('Check requirements') {
            owner 'admin'
            plannedDuration 90000
          }
          gate('Ok Release Manager') {
            owner 'admin'
            plannedDuration 90000
          }
          custom('Update Ok Release Manager') {
            script {
              type 'xld.UpdateCIProperty'
              server 'XL Deploy'
              ciID 'Applications/Calculator/front/${next-version}'
              ciProperty 'satisfiesOkReleaseManager'
              propertyValue 'true'
            }
          }
          custom('Deploy to PRO') {
            facets {
              facet {
                type 'udm.DeploymentTaskFacet'
                applicationId 'Configuration/environmentManagement/Application3ed3511896fb41ef9b15472d332def6f'
                environmentId 'Configuration/environmentManagement/Environment7750876838504a07981f9b587f60e438'
                version '${next-version}'
              }
            }
            script {
              type 'xldeploy.Deploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deploymentApplication 'Applications/Calculator/front'
              deploymentVersion '${next-version}'
              deploymentPackage 'Applications/Calculator/front/${next-version}'
              deploymentEnvironment 'Environments/Calculator/calculator-pro'
            }
          }
        }
      }
    }
    extensions {
      dashboard('Dashboard') {
        parentId 'Applications/Folder1a787388922e47c08684a591a930e33e/Releasead9d37c72cce402a8593d9992428edab'
        owner 'admin'
        tiles {
          releaseProgressTile('Release progress') {
            col 0
          }
          timelineTile('Release timeline') {
            
          }
          deploymentsSuccessRateTile('Deployment success rate') {
            row 0
            col 1
          }
          releaseHealthTile('Release health') {
            
          }
        }
      }
    }
    
  }
}