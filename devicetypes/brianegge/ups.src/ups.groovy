/**
 *	Wemo Coffeemaker Switch (Connect)
 *
 *	Author:  Tyler Britten  with special thanks to and code from Kevin Tierney and Brian Keifer
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

metadata {
		definition (name: "UPS", namespace: "brianegge", author: "Brian Egge") {
            capability "Actuator"
            capability "Polling"
			capability "Refresh"
            capability "Momentary"
            capability "Switch"

            attribute "brewMode", "string"
            
            command "brew"
		}

    tiles(scale: 2) {
      standardTile("refresh", "refresh",label:"Refresh",width:2,height:2,decoration: "flat") {
        state "default", action:"refresh", icon:"st.secondary.refresh"
      }
     valueTile("Status","device.brewMode",width:6,height:4,decoration: "flat"){
        state "PlaceCarafe", label:'Place Carafe',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "Refill", label:'Refill',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "RefillWater", label:'Refill Water',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "Ready", label:'Ready to Brew',backgroundColor:"#ffffff",icon:"st.Appliances.appliances14"
        state "Brewing", label:'Brewing',backgroundColor:"#00a0dc",icon:"st.Appliances.appliances14"
        state "Brewed", label:'Coffee is Brewed',backgroundColor:"#44b621",icon:"st.Appliances.appliances14"
        state "CleaningBrewing", label:'Cleaning - Brewing',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "CleaningSoaking", label:'Cleaning - Soaking',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "BrewFailCarafeRemoved", label:'Brew Fail - Carafe Removed',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
      }			
      standardTile("Action", "device.brewMode",width:2,height:2,decoration: "flat") {
        state "PlaceCarafe", label:'Not Ready',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "Refill", label:'Not Ready',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "RefillWater", label:'Not Ready',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "Ready", label:'BREW',backgroundColor:"#ffffff",icon:"st.Appliances.appliances14",action: "momentary.push"
        state "Brewing", label:'Brewing',backgroundColor:"#00a0dc",icon:"st.Appliances.appliances14"
        state "Brewed", label:'Brewed',backgroundColor:"#44b621",icon:"st.Appliances.appliances14"
        state "CleaningBrewing", label:'Cleaning',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "CleaningSoaking", label:'Cleaning',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
        state "BrewFailCarafeRemoved", label:'Brew Fail',backgroundColor:"#e86d13",icon:"st.Appliances.appliances14"
      }
      main "Action"
      details(["Status","Action","refresh"])
      }
}

def parse(String description) {

    def evtMessage = parseLanMessage(description)
    def evtHeader = evtMessage.header
    def evtBody = evtMessage.body

	if (evtBody) {
		evtBody = evtBody.replaceAll(~/&amp;/, "&")
    	evtBody = evtBody.replaceAll(~/&lt;/, "<")
    	evtBody = evtBody.replaceAll(~/&gt;/, ">")
	}

    // log.debug("Header: ${evtHeader}")
    // log.debug("Body: ${evtBody}")

    if (evtHeader?.contains("SID: uuid:")) {
		def sid = (evtHeader =~ /SID: uuid:.*/) ? ( evtHeader =~ /SID: uuid:.*/)[0] : "0"
		sid -= "SID: uuid:".trim()
        log.debug "Subscription updated!  New SID: ${sid}"
    	updateDataValue("subscriptionId", sid)
        
    }

    if (evtBody) {
        //log.debug("evtBody: ${evtBody}")
        def body = new XmlSlurper().parseText(evtBody)
        if (body == 0) {
            log.debug ("Command succeeded!")
            return [getAttributes()]
        } else {
            def result = []
            //log.debug("ELSE!: ${body.Body}")
            def mode = body.Body.GetAttributesResponse.attributeList.attribute.find {it.name == "Mode"}.value.text()
            def notifymode = body.property.attributeList.attribute.find {it.name == "Mode"}.value.text()
            def notifymodetime = body.property.attributeList.attribute.find {it.name == "ModeTime"}.value.text()
            log.debug "Current Level: " + level 
            if(mode){
                def currentMode = getModeName(mode)
                result << createEvent(name: "brewMode", value: currentMode) 
                log.debug "Received getCMState mode:  " + currentMode 
            } else if (notifymode) {
                def currentMode = getModeName(notifymode)
                result << createEvent(name: "brewMode", value: currentMode) 
                log.debug "Received getCMState NOTIFY mode:  " + currentMode 
            } else if (notifymodetime){
                log.debug "Received getCMState NOTIFY modetime:  " + notifymodetime
            } else{
                log.debug "Other Response: \nHeader: " + evtHeader + "\nBody: " + evtBody
 				}
   		    return result
   	 		}
     }
} // end parse



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

    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    return ip + ":" + port
}

private postRequest(path, SOAPaction, body) {
    // Send  a post request
    def result = new physicalgraph.device.HubAction([
        'method': 'POST',
        'path': path,
        'body': body,
        'headers': [
        'HOST': getHostAddress(),
        'Content-type': 'text/xml; charset=utf-8',
        'SOAPAction': "\"${SOAPaction}\""
        ]
        ], device.deviceNetworkId)
    return result
}

def poll() {
	refresh()
}
def on() {
        push()
}
def push(){
	//Sets up on command based on defaults
	log.trace "Brew Called"
    setAttribute("Mode",4)
}
private getModeName(modeNum){
	log.debug "getModeName: " + modeNum
    def numToMode = ['0':'Refill',
                     '1': 'PlaceCarafe',
                     '2': 'RefillWater',
                     '3':'Ready',
                     '4':'Brewing',
                     '5': 'Brewed',
                     '6': 'CleaningBrewing',
                     '7': 'CleaningSoaking',
                     '8':'BrewFailCarafeRemoved']
	return numToMode.get(modeNum)
}

def setAttribute(name, value) {
	def body = """
	<?xml version="1.0" encoding="utf-8"?>
	<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	<s:Body>
	<u:SetAttributes xmlns:u="urn:ups.example.com:service:UPS:1">
	<attributeList>&lt;attribute&gt;&lt;name&gt;${name}&lt;/name&gt;&lt;value&gt;${value}&lt;/value&gt;&lt;/attribute&gt;</attributeList>
	</u:SetAttributes>
	</s:Body>
	</s:Envelope>
	"""
	postRequest('/upnp/control/deviceevent1', 'urn:ups.example.com:service:deviceevent:1#SetAttributes', body)
}


def getAttributes() {
    log.debug("getAttributes()")
    def body = """
    <?xml version="1.0" encoding="utf-8"?>
    <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
    <u:GetAttributes xmlns:u="urn:ups.example.com:service:deviceevent:1">
    </u:GetAttributes>
    </s:Body>
    </s:Envelope>
    """
    postRequest('/upnp/control/deviceevent1', 'urn:ups.example.com:service:deviceevent:1#GetAttributes', body)
}

def refresh() {
    //log.debug "Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'"
    log.debug("Refresh requested!")
	subscribe()
    getAttributes()
    //poll()
}

private subscribeAction(path, callbackPath="") {
    log.trace "subscribe($path, $callbackPath)"
    def address = getCallBackAddress()
    def ip = getHostAddress()

    def result = new physicalgraph.device.HubAction(
        method: "SUBSCRIBE",
        path: path,
        headers: [
            HOST: ip,
            CALLBACK: "<http://${address}/notify$callbackPath>",
            NT: "upnp:event",
            TIMEOUT: "Second-28800"
        ]
    )

    log.trace "SUBSCRIBE $path"
    sendHubCommand(result) 
    return result
}
def subscribe() {
    log.debug "Subscribing to ${getHostAddress()}"
    subscribeAction("/upnp/event/basicevent1")
}

def resubscribe() {
    //log.debug "Executing 'resubscribe()'"
    def sid = getDeviceDataByName("subscriptionId")

    sendHubCommand(new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
    HOST: ${getHostAddress()}
    SID: uuid:${sid}
    TIMEOUT: Second-5400
    """, physicalgraph.device.Protocol.LAN))

}

def unsubscribe() {
    def sid = getDeviceDataByName("subscriptionId")
    sendHubCommand(new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
    HOST: ${getHostAddress()}
    SID: uuid:${sid}
    """, physicalgraph.device.Protocol.LAN))
}

