/**
 *  Log HubStatus to IFTTT
 *
 *  Copyright 2017 Kevin Tierney
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Log HubStatus to IFTTT",
    namespace: "tierneykev",
    author: "Kevin Tierney",
    description: "Logs HubStatus messages to IFTTT for tracking internet status",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



def installed() {

    log.debug "Installed"
	subscribeToEvents()
}

def updated() {
    log.debug "Updated"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	unsubscribe()
    //get first hub object in your location.
    def hub = location.hubs[0] 
    //subscribe function "eventHandler" to Hub "status" events
    subscribe(hub, "hubStatus", eventHandler)
  
}

def eventHandler(evt) {	
    log.debug "Event Name: $evt.name | Event Value: $evt.value | Event Description: $evt.descriptionText"
    def apiURL = "https://maker.ifttt.com/trigger/hub_status/with/key/brmOeqPFbtdHJL2UgSR4kb"
    if(evt.value == 'disconnected' || evt.value == 'active'){
        try {
            httpPost( uri: apiURL, body: [value1: evt.name, value2: evt.value, value3:evt.descriptionText])  {
            response -> log.debug (response.data)
            }
        } catch (e) {
            log.debug "something went wrong: $e"
        }
    }
    
    
 
}