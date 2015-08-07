/**
 *  
 *
* UBI Speach Synthesis only device type
* 
 */

preferences {
    input("speechToken", "text", title: "Speech Token", description: "Token for the speech lesson on this UBI")
    }

metadata {
    definition (name: "Ubi_Speech", namespace: "tierneykev", author: "Kevin Tierney") {        
         capability "Speech Synthesis"       
    }

}    
// SpeechSynthesis.speak
def speak(textToSpeak) {
    log.debug("speak(${textToSpeak})")
    def params = [
            uri: "https://portal.theubi.com",
            path: "/webapi/behaviour",
            query: [
                access_token : speechToken,
                text : textToSpeak
            ]
    ]

    httpGet(params) { response ->
        log.debug "Request return code, $response.status"
    }
}