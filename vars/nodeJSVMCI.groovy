def call(Map configMap){

    def component = configMap.get("component")
    echo "component is: $component"




    pipeline {
        agent { node { label 'Agent-1' } }
        options {
            timeout(time: 1, unit: 'HOURS')
        }

        environment{
            packageVersion = ''
        }

        parameters {
            string(name: 'component', defaultValue: '', description: 'Which component?')


        }

        stages {
            stage('Get version'){
                steps{
                    script{
                        def packageJson = readJSON(file: 'package.json')
                        packageVersion = packageJson.version
                        echo "version: ${packageVersion}"
                    }
                }
            }
            stage('Install depdencies') {
                steps {
                    sh """
                        sudo yum update -y
                        sudo yum install nodejs npm -y

                    """
                }
            }
            stage('Unit test') {
                steps {
                    echo "unit testing is done here"
                }
            }
            //sonar-scanner command expect sonar-project.properties should be available
            stage('Sonar Scan') {
                steps {
                    echo "Sonar scan done"
                }
            }
            stage('Build') {
                steps {
                    sh 'ls -ltr'
                    sh "zip -r ${component}.zip ./* --exclude=.git --exclude=.zip"
                }
            }
            stage('SAST') {
                steps {
                    echo "SAST Done"
                    echo "package version: $packageVersion"
                }
            }
            //install pipeline utility steps plugin, if not installed
            stage('Publish Artifact') {
                steps {
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: '172.31.89.51:8081/',
                        groupId: 'com.roboshop',
                        version: "$packageVersion",
                        repository: "${component}",
                        credentialsId: 'nexus-auth',
                        artifacts: [
                            [artifactId: "${component}",
                            classifier: '',
                            file: "${component}.zip",
                            type: 'zip']
                        ]
                    )
                }
            }

            //here below we need to configure the downstream job, Above we have the CI (upstream job)
            //this job will wait untill the downstream job is over

            stage('Deploy') {
            steps {

                    script{ 
                        echo "Deployment..."
                        def params = [
                            string(name: 'version', value: "$packageVersion")
                    ]
                    build job: "../${component}-deploy/", wait: true, parameters: params
                    }
                }
            }


        }
        

        post{
            always{
                echo 'cleaning up workspace'
                deleteDir()
            }
        }
    }
}

    
    
