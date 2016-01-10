/**
 *	Wemo Crockpot Switch (Connect)
 *
 *	Copyright 2014 Kevin Tierney
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
 
preferences {
  //Default Cooking Time
  input "lowCookTime", "number", title: "Defualt Time - Low",description: "Low Minutes", defaultValue: '360',	displayDuringSetup: true
  input "highCookTime", "number", title: "Defualt Time - High",description: "High Minutes", defaultValue: '240',	displayDuringSetup: true
  input "warmCookTime", "number", title: "Defualt Time - Warm",description: "Warm Minutes", defaultValue: '120',	displayDuringSetup: true	
  //Default Cooking Mode
  input "defaultCookMode", "enum", title: "Default Mode",description:"Default mode for On command", options: ["Low","High","Warm"], defaultValue:"Low",displayDuringSetup: true
}
metadata {
		definition (name: "Wemo Crockpot", namespace: "tierneykev", author: "Kevin Tierney") {
      capability "Actuator"
			capability "Switch"
			capability "Polling"
			capability "Refresh"

			attribute "timer" ,string
			attribute "cookMode", string
			attribute "cookedTime", string
            
				
			command "low"
			command "high"
			command "warm"
			command "setCookTime"
			command "sendOn"
            command "postRequest"
			command "configure"				
		}

    tiles {
      standardTile("refresh", "refresh",label:"Refresh") {
        state "default", action:"refresh", icon:"st.secondary.refresh"
      }
      standardTile("configure", "device.configure",label:"Refresh") {
        state "default", action:"device.configure", icon:"st.secondary.configure"
      }
      standardTile("low", "device.cookMode",label:"Low") {
        state "default", label: 'LOW', action: "low", icon:"st.Appliances.applicances16",backgroundColor:"#ffffff"
        state "low", label: 'LOW', action: "low", icon:"st.Appliances.applicances16",backgroundColor:"#66ff33"
      }
      standardTile("high", "device.cookMode") {
        state "default", label: 'HIGH', action: "high", icon:"st.Appliances.applicances16",backgroundColor:"#ffffff"
        state "high", label: 'HIGH', action: "high", icon:"st.Appliances.applicances16",backgroundColor:"#66ff33"
      }	
      standardTile("warm", "device.cookMode") {
        state "default", label: 'WARM', action: "warm", icon:"st.Appliances.applicances16",backgroundColor:"#ffffff"
        state "warm", label: 'WARM', action: "warm", icon:"st.Appliances.applicances16",backgroundColor:"#66ff33"
      }
      standardTile("off", "device.cookMode") {
        state "default", label: 'OFF', action: "off", icon:"st.Appliances.applicances16",backgroundColor:"#ffffff"
        state "off", label: 'OFF', action: "off", icon:"st.Appliances.applicances16",backgroundColor:"#66ff33"
      }
      valueTile("currMode","device.cookMode",width:2,height:2){
        state "default", label:'${currentValue}'
      }			
      valueTile("cookedTime", "device.cookedTime",decoration:"flat") {
        state "default", label:'Cooked Time: ${currentValue}'
      }
      controlTile("timeControl","device.timer","slider",height:1,width:2,range:"(0..720)"){
        state "default",action:"setCookTime"
      }
      valueTile("time", "device.timer",decoration:"flat") {
        state "default", label:'Time Left: ${currentValue}'
      }
      main "currMode"
      details(["currMode","cookedTime","off","timeControl","time","low","high","warm","refresh"])
      }
}

def installed(){
	log.debug "Installed with settings: ${settings}" 
}

def updated(){
	log.debug "Updated with settings: ${settings}"
    subscribe()
}
def low(){
	log.trace "low called"
    def bodyMap = [mode:"51",time:lowCookTime]
	//sendEvent(name: "cookMode", value:"low" )
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}
def high(){
	log.trace "high called"
    def bodyMap = [mode:"52",time:highCookTime]
	//sendEvent(name: "cookMode", value:"high" )
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}
def warm(){
	log.trace "warm called"
    def bodyMap = [mode:"50",time:warmCookTime]
	//sendEvent(name: "cookMode", value:"warm" )
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}

def off() {
	//log.trace "off called"
    //sendEvent(name: "cookMode", value:"off" )
    //sendEvent(name: "cookedTime", value:"0" )
    def bodyMap = [mode:'0',time:'0']
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}
def on(){
	//Sets up on command based on defaults
	log.trace "On Called"
    def sendMode = getModeNum(defaultCookMode)
    def sendTime = getCookTime(sendMode)
    def bodyMap = [mode:sendMode,time:sendTime]
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}

def setCookTime(minutes){
  	//Called When slider moved to update time
  	//Need to look up current mode since it needs to be sent
	//log.trace "Set Cook Time ${minutes}"
    //sendEvent(name: "timer", value:minutes )
    def sendMode = getModeNum(device.currentState("cookMode")?.value)
    def bodyMap = [mode:sendMode,time:minutes]
    sendCommand('/upnp/control/basicevent1','urn:Belkin:service:basicevent:1','SetCrockpotState',bodyMap)
}

def sendCommand(path,urn,action,body){
	log.debug "Send command called with path: ${path} , urn: ${urn}, action: ${action} , body: ${body}"
	 def result = new physicalgraph.device.HubSoapAction(
        path:    path,
        urn:     urn,
        action:  action,
        body:    body,
        headers: [Host:getHostAddress(), CONNECTION: "close"]
    )
    return result
}

def refresh() {
	log.trace "Refresh Called"
   	sendCommand("/upnp/control/basicevent1","urn:Belkin:service:basicevent:1",'GetCrockpotState','')
}

// parse events into attributes
def parse(String description){
	//log.debug "Parse: ${description}"
    def result = []  
    def msg = parseLanMessage(description)
	def headerString = msg.header
    def bodyString = msg.body
    //log.debug "headerString: ${headerString}"
    //log.debug "bodyString: ${bodyString}"
    
    if (headerString?.contains("SID: uuid:")) {
		def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
		sid -= "SID: uuid:".trim()
        log.debug "updating subscription sid: ${sid}"

		updateDataValue("subscriptionId", sid)
	}
    
    if(bodyString){
    	//log.debug "bodyString: ${bodyString}"
        def body = new XmlSlurper().parseText(bodyString)               
        
        //GetCrockpotStateResponse
        if(body.Body.SetCrockpotStateResponse.time.text()){
            //crockpot responds with time left only (currentTime)
            //log.debug "setCrockpotStateResp: \nHeader: " + headerString + "\nBody: " + bodyString
            def currentTime = body.Body.SetCrockpotStateResponse.time.text()
            result << createEvent(name: "timer", value:currentTime)
        } else if(body.Body.GetCrockpotStateResponse.text())	{
            def currentMode = body.Body.GetCrockpotStateResponse.mode.text()
            result << createEvent(name: "cookMode", value: getModeName(currentMode)) 

            def currentTime = body.Body.GetCrockpotStateResponse.time.text()
            result << createEvent(name: "timer", value:currentTime)

            def currentCooked = body.Body.GetCrockpotStateResponse.cookedTime.text()
            result << createEvent(name: "cookedTime", value:currentCooked)

            log.debug "Received getCPState mode:  " + getModeName(currentMode) + " | time: " +  currentTime + " | cookedTime: " + currentCooked
		} else if(body.property.mode.text()){
        	//Subscription Notifications
        	//log.debug "Notify response: ${bodyString}"
            if(body.property.mode.text()) {
            	def currentMode = body.property.mode.text()
                def currentModeName = getModeName(currentMode)
            	result << createEvent(name: "cookMode", value: currentModeName)
            }
            if(body.property.time.text()){
            	def currentTime = body.property.time.text()
            	result << createEvent(name: "timer", value:currentTime)
            }
            if(body.property.cookedTime.text()){
            	def currentCooked = body.property.cookedTime.text()
            	result << createEvent(name: "cookedTime", value:currentCooked)
            }                     
        } else if(body.property.BinaryState.text()){
        	//notification sends two responses - second is with binaryState
            //Binary State  1 = off / 5 = on
        } else {
            log.debug "Other Response: \nHeader: " + headerString + "\nBody: " + bodyString
        }
        result
    }
} // end parse

private getCookTime(modeNum){
	//looks up default times from settings
    def cookTimeMap = ['50':warmCookTime , '51':lowCookTime , '52':highCookTime ]
    return cookTimeMap.get(modeNum)
}

private getModeName(modeNum){
	//log.debug "getModeName: " + modeNum
    def numToMode = ['0':'off','50': 'warm','51': 'low', '52':'high']
	return numToMode.get(modeNum)
}

private getModeNum(modeName){
	//log.debug "getModeNum: " + modeName
    def modeToNum = ['off':'0','warm':'50','low':'51','high':'52']
	return modeToNum.get(modeName)
}
private getCallBackAddress() {
	//Returns hub's local ip:port
		device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
		Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
		[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {

	//Get Device IP Address
    //log.trace "getHostAdddress called"
		def ip = getDataValue("ip")
		def port = getDataValue("port")

		if (!ip || !port) {
				def parts = device.deviceNetworkId.split(":")
				if (parts.length == 2) {
						ip = parts[0]
						port = parts[1]
				} else {
						//log.warn "Can't figure out ip and port for device: ${device.id}"
				}
		}
		
		//convert IP/port
		ip = convertHexToIP(ip)
		port = convertHexToInt(port)
		//log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
		return ip + ":" + port
}

private subscribe() {
	   
    def address = getCallBackAddress()
    def ip = getHostAddress()
    def path = '/upnp/event/basicevent1'
 
	log.trace "Subscribe called. address: ${address} , ip: ${ip}, path: ${path}"

    def result = new physicalgraph.device.HubAction(
        method: "SUBSCRIBE",
        path: path,
        headers: [
            HOST: ip,
            CALLBACK: "<http://${address}/notify>",
            NT: "upnp:event",
            TIMEOUT: "Second-28800"
        ]
    )

    log.trace "SUBSCRIBE $path"

    return result
}

