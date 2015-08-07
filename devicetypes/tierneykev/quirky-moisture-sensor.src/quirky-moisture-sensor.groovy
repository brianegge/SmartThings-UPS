/*
Quirky Moisture Sensor

 */
metadata {
	definition (name: "Quirky Moisture Sensor",namespace: "tierneykev", author: "Kevin Tierney") {
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Water Sensor"
        
        command "enrollResponse"
 
 
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500,0020,0B05", outClusters: "0003,0019"	}
 
	simulator {
 
	}

	preferences {
	}
 
	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
        
		
 
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery'
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
 
		main "water"
		details(["water", "battery", "refresh"])
	}
}
 
def parse(String description) {
	log.debug "description: $description"

	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('zone status')) {
	    map = parseIasMessage(description)
    }
 
	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : null
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}
 
private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) {
        switch(cluster.clusterId) {
            case 0x0001:
            	resultMap = getBatteryResult(cluster.data.last())
                break

        }
    }

    return resultMap
}

private boolean shouldProcessMessage(cluster) {
    // 0x0B is default response indicating message got through
    // 0x07 is bind message
    boolean ignoredMessage = cluster.profileId != 0x0104 || 
        cluster.command == 0x0B ||
        cluster.command == 0x07 ||
        (cluster.data.size() > 0 && cluster.data.first() == 0x3e)
    return !ignoredMessage
}
 
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		resultMap = getBatteryResult(Integer.parseInt(descMap.value, 16))
	}
 
	return resultMap
}
 


private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]
    
    Map resultMap = [:]
    switch(msgCode) {
        case '0x0020': // Closed/No Motion/Dry
        	resultMap = getMoistureResult('dry')
            break

        case '0x0021': // Open/Motion/Wet
        	resultMap = getMoistureResult('wet')
            break

        case '0x0022': // Tamper Alarm
            break

        case '0x0023': // Battery Alarm
            break

        case '0x0024': // Supervision Report
        	 log.debug 'dry with tamper alarm'
        	resultMap = getMoistureResult('dry')
            break

        case '0x0025': // Restore Report
        	log.debug 'water with tamper alarm'
        	resultMap = getMoistureResult('wet')
            break

        case '0x0026': // Trouble/Failure
            break

        case '0x0028': // Test Mode
            break
    }
    return resultMap
}


private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)
    
    def result = [
    	name: 'battery'
    ]
    
	def volts = rawValue / 10
	def descriptionText
	if (volts > 3.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 2.1
    	def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
		result.descriptionText = "${linkText} battery was ${result.value}%"
	}

	return result
}


private Map getMoistureResult(value) {
	log.debug 'water'
	String descriptionText = "${device.displayName} is ${value}"
	return [
		name: 'water',
		value: value,
		descriptionText: descriptionText
	]
}

def refresh()
{
	log.debug "Refreshing Battery"
	[

		"st rattr 0x${device.deviceNetworkId} 1 1 0x20"

	]
}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting, IAS CIE, and Bindings."
	def configCmds = [	
		"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        "zcl global send-me-a-report 1 0x20 0x20 300 0600 {01}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
       
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x001 {${device.zigbeeId}} {}", "delay 1000",
        
        "raw 0x500 {01 23 00 00 00}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",
	]
    return configCmds + refresh() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
    [	
    	
	"raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} 1 1"
        
    ]
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}
