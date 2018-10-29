/**
 *	UPS Power Monitor
 *
 *	Author:  Brian Egge, based on work by Tyler Britten
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
            capability "Battery"
            capability "Power Meter"
            capability "Power Source"
            capability "Voltage Measurement"
			capability "Refresh"
            
        attribute "input_voltage", "string"
		attribute "input_frequency", "string"
        attribute "input_state", "string"
		attribute "output_voltage", "string"
        attribute "output_power", "string"
		attribute "output_percent", "string"
        attribute "output_source", "string"
        
        attribute "battery_percent", "string"
		attribute "battery_timeremaining", "string"
		attribute "battery_voltage", "string"
        
        attribute "system_alarm", "string"
        attribute "system_status", "string"
		}

tiles {
        standardTile("status", "device.system_status", canChangeBackground: true, canChangeIcon: true) {
      	state "normal", label:'NORMAL', icon: "st.Appliances.appliances17", backgroundColor: "#79b821"
        state "onbattery", label:"ON BATTERY", icon: "st.Appliances.appliances17", backgroundColor: "#f5b220"
        state "onbypass", label:"ON BYPASS", icon: "st.Appliances.appliances17", backgroundColor: "#f5b220"
        state "manualoff", label:"MANUAL OFF", icon: "st.Appliances.appliances17", backgroundColor: "#0000FF"
        state "fail", label:"FAILURE", icon: "st.Appliances.appliances17", backgroundColor: "#FF0000"
        }
             
        valueTile("inputvoltage", "device.input_voltage", width: 1, height: 1) {
        state("input_voltage", label:'In ${currentValue} VAC',
            backgroundColors:[
                [value: 114, color: "#ff0000"],
                [value: 115, color: "#ff3b0b"],
                [value: 116, color: "#fa7616"],
                [value: 117, color: "#f5b220"],
                [value: 118, color: "#f1d801"],
                [value: 119, color: "#b5c811"],
                [value: 120, color: "#79b821"],
                [value: 122, color: "#79b821"],
                [value: 123, color: "#79b821"],
                [value: 124, color: "#79b821"],
                [value: 125, color: "#79b821"],
                [value: 126, color: "#b5c811"],
                [value: 127, color: "#f1d801"],
				[value: 128, color: "#f5b220"],
                [value: 129, color: "#ff0000"]
            ]
        )}
        
        standardTile("inputstate", "device.input_state", canChangeBackground: false, canChangeIcon: false) {
      	state "normal",   label:'INPUT OK', icon: "st.switches.switch.on",   backgroundColor: "#79b821"
      	state "fail", label:'INPUT BAD', icon: "st.switches.switch.off", backgroundColor: "#ff0000"
        }
        
        valueTile("inputfreq", "device.input_frequency", width: 1, height: 1) {
        state("input_frequency", label:'${currentValue} Hz',
            backgroundColors:[
                [value: 57, color: "#ff0000"],
                [value: 58, color: "#ff3b0b"],
                [value: 59, color: "#f1d801"],
                [value: 60, color: "#79b821"],
                [value: 61, color: "#f1d801"],
            ]
        )}
        
        valueTile("outputvoltage", "device.output_voltage", width: 1, height: 1) {
        state("output_voltage", label:'Out ${currentValue} VAC',
            backgroundColors:[
                [value: 114, color: "#ff0000"],
                [value: 115, color: "#ff3b0b"],
                [value: 116, color: "#fa7616"],
                [value: 117, color: "#f5b220"],
                [value: 118, color: "#f1d801"],
                [value: 119, color: "#b5c811"],
                [value: 120, color: "#79b821"],
                [value: 122, color: "#79b821"],
                [value: 123, color: "#79b821"],
                [value: 124, color: "#79b821"],
                [value: 125, color: "#79b821"],
                [value: 126, color: "#b5c811"],
                [value: 127, color: "#f1d801"],
				[value: 128, color: "#f5b220"],
                [value: 129, color: "#ff0000"]
            ]
        )}
        standardTile("outputsource", "device.output_source", canChangeBackground: false, canChangeIcon: false) {
      	state "normal", label:'OUTPUT OK', action: "off", icon: "st.switches.switch.on",   backgroundColor: "#79b821"
      	state "none", label:'OUTPUT OFF', action: "on", icon: "st.switches.switch.off", backgroundColor: "ff0000"
        state "bypass", label:'IN BYPASS', action: "on", icon: "st.switches.switch.off", backgroundColor: "#f5b220"
        state "battery", label:'ON BATTERY', icon: "st.switches.switch.off", backgroundColor: "#F6EF1F"
        }
        
        valueTile("loadpercent", "device.output_percent", width: 1, height: 1) {
        state("output_percent", label:'Load ${currentValue}%',
            backgroundColors:[
				[value: 25, color: "#79b821"],
                [value: 35, color: "#b5c811"],
                [value: 45, color: "#f1d801"],
                [value: 55, color: "#f5b220"],
                [value: 65, color: "#fa7616"],
                [value: 75, color: "#ff3b0b"],
                [value: 85, color: "#ff0000"]
            ]
        )}
               
        valueTile("battery", "device.battery_percent", width: 1, height: 1) {
        state("battery_percent", label:'Battery ${currentValue}%',
            backgroundColors:[
                [value: 25, color: "#ff0000"],
                [value: 45, color: "#ff3b0b"],
                [value: 60, color: "#fa7616"],
                [value: 75, color: "#f5b220"],
                [value: 85, color: "#f1d801"],
                [value: 90, color: "#b5c811"],
                [value: 95, color: "#79b821"]
            ]
        )}
        valueTile("power", "device.output_power", width: 1, height: 1) {
        state("output_power", label:'${currentValue} W',
            backgroundColor: "#444444"
        )}
        
        //standardTile("alarm", "device.system_alarm", canChangeBackground: false, canChangeIcon: false, width: 3, height: 1) {
      	//state "alarm",   label:'ALARM', action: "silenceAlarm", icon: "st.alarm.beep.beep",   backgroundColor: "#ff0000"
      	//state "normal", label:'SYSTEM OK', icon: "st.alarm.beep.beep", backgroundColor: "#79b821"
        //}
        standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) 
        {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }  
        
        // This tile will be the tile that is displayed on the Hub page.
    	main "status"

    	// These tiles will be displayed when clicked on the device, in the order listed here.
    	details(["inputstate", "inputvoltage", "inputfreq", "power", "battery", "outputvoltage", "loadpercent", "outputsource", "refresh"])
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

    log.debug("Header: ${evtHeader}")
    log.debug("Body: ${evtBody}")

    if (evtHeader?.contains("SID: uuid:")) {
		def sid = (evtHeader =~ /SID: uuid:.*/) ? ( evtHeader =~ /SID: uuid:.*/)[0] : "0"
		sid -= "SID: uuid:".trim()
        log.debug "Subscription updated!  New SID: ${sid}"
    	updateDataValue("subscriptionId", sid)
        
    }

    if (evtBody) {
        def body = new XmlSlurper().parseText(evtBody)
        if (body == 0) {
            log.debug ("Command succeeded!")
            return [getAttributes()]
        } else {
            def result = []
            result << createEvent(name: "input_voltage", value: body.input_voltage.text())
            result << createEvent(name: "input_frequency", value: body.input_frequency.text())
            if (body.ups_status.text() == "OL") { 
              result << createEvent(name: "system_status", value: "normal")
              result << createEvent(name: "input_state", value: "normal")
              result << createEvent(name: "output_source", value: "normal")
            } else if (body.ups_status.text() == "OB" || body.ups_status.text() == "LB") { 
              result << createEvent(name: "system_status", value: "onbattery")
              result << createEvent(name: "input_state", value: "fail")
              result << createEvent(name: "output_source", value: "battery")
            }
            result << createEvent(name: "output_voltage", value: body.output_voltage.text())
            result << createEvent(name: "output_power", value: body.ups_power.text())
            result << createEvent(name: "output_percent", value: Double.parseDouble(body.ups_power.text()) / Double.parseDouble(body.ups_power_nominal.text()) * 100.0 )
            result << createEvent(name: "battery_voltage", value: body.battery_voltage.text())
            result << createEvent(name: "battery_timeremaining", value: body.battery_runtime.text())
            result << createEvent(name: "battery_percent", value: body.battery_charge.text())

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

def updated() {
   log.debug "updated()"
   unschedule(poll)
   runEvery1Minute(poll)
}

def poll() {
	getAttributes()
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

