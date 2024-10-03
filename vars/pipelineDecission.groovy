#!groovy

def decidepipeline(Map configMap){
    application = configMap.get("application")

    //here we are getting nodeJSVM

    switch(application) {
        case 'nodeJSVM':
            echo "Application is nodeJS and VM based"
            nodeJSVMCI(configMap)
            break
        case 'JavaVM':
            javaVMCI(configMap)
            break
        default: 
            error "un recognised application"
            break
    }
    echo "I need to take the decission for your map"
}