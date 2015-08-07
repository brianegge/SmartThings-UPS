/*
 *  
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
 *  Linear Z-Wave Siren
 *
 *  Author: Kevin Tierney
 *  Date: 2015-07-30
 */

metadata {
	definition (name: "Linear Z-Wave Siren", namespace: "tierneykev", author: "Kevin Tierney") {
		capability "Actuator"
        capability "Alarm"
        capability "Battery"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
		capability "Switch"
        
        command "test"
        command "strobe"
        command "siren"
        command "both"

        //Supported Command Classes
        //0x20-Basic ,0x25-Binary Switch ,0x70-Configuration , 0x72-Manufacturer Specific ,0x86-Version
        fingerprint inClusters: "0x20,0x25,0x70,0x72,0x86,"
	}
   
	simulator {
		// reply messages
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
		reply "200121,2002": "command: 2003, payload: 21"
		reply "200142,2002": "command: 2003, payload: 42"
		reply "2001FF,delay 3000,200100,2002": "command: 2003, payload: 00"
	}

	tiles {
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "both", label:'alarm!', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
            state "siren", label:'siren!', action:'alarm.strobe', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
            state "strobe", label:'strobe!', action:'alarm.both', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"						
		}
 
        standardTile("test", "device.alarm", inactiveLabel: false) {
                state "off", label:'', action:"test", icon:"st.secondary.test"
                state "test", label:'', action:"alarm.off", icon:"st.secondary.test",backgroundColor:"#00CC99"
            }
        standardTile("off", "device.alarm", inactiveLabel: false) {
            state "default", label:'Disarm', action:"alarm.off", icon:"st.alarm.alarm.alarm"
        } 
         valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

       
		main "off"
		//details(["alarm","off","test","battery","refresh"])
        details(["alarm","off","test","battery","refresh"])
	}

preferences {
    input "autoStopTime", "enum", title: "Disarm Time",required:true,displayDuringSetup:true, options: ["30","60","120","Infinite"],default:'30'
} 

	}

def updated() {
		def autoStopTimeParameter = 0
        if (autoStopTime == '30') {
        	autoStopTimeParameter = 0
        } else if( autoStopTime == '60'){
        	autoStopTimeParameter = 1
        } else if (autoStopTime == '120') {
        	autoStopTimeParameter = 2
        } else if (autoStopTime == 'Infinite') {
        	autoStopTimeParameter = 3
        }
        log.debug "AutoStopTime - ${autoStopTimeParameter}"
	    zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [autoStopTimeParameter])
}

def strobe() {
	log.debug "Setting alarm to strobe."
    sendEvent(name: "alarm", value: "strobe")
    zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [2])
}

def siren() {
	log.debug "Setting alarm to siren."
   sendEvent(name: "alarm", value: "siren")
   zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [1])
}

def both() {
	log.debug "Setting alarm to both."
    sendEvent(name: "alarm", value: "both")
	zwave.configurationV1.configurationSet(parameterNumber: 0, size: 1, configurationValue: [0])

    
}
def off() {
	log.debug "Turning Alarm Off"
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	]
}
def test() {
	log.debug "testing siren"
[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		"delay 3000",
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet()
	]
    
}

def parse(String description) {
	log.debug "parse($description)"
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = createEvents(cmd)
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def createEvents(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def switchValue = cmd.value ? "on" : "off"
	def alarmValue
	if (cmd.value == 0) {
		alarmValue = "off"
	}
	else if (cmd.value <= 33) {
		alarmValue = "strobe"
	}
	else if (cmd.value <= 66) {
		alarmValue = "siren"
	}
	else {
		alarmValue = "both"
	}
	[
		createEvent([name: "switch", value: switchValue, type: "digital", displayed: false]),
		createEvent([name: "alarm", value: alarmValue, type: "digital"])
	]
}


def createEvents(physicalgraph.zwave.Command cmd) {
	log.warn "UNEXPECTED COMMAND: $cmd"
}


def createEvents(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def poll() {
	if (secondsPast(state.lastbatt, 36*60*60)) {
		return zwave.batteryV1.batteryGet().format
	} else {
		return null
	}
}

private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (new Date().time - timestamp) > (seconds * 1000)
}

def refresh() {
	log.debug "sending battery refresh command"
	zwave.batteryV1.batteryGet().format()
}