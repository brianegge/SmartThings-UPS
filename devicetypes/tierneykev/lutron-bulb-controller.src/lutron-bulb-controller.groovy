metadata {
	definition (name: "Lutron Bulb Controller", namespace: "tierneykev", author: "Kevin Tierney") {
    command getClusters
    fingerprint profileId: "C05E", deviceId: "0820", inClusters: "0000,1000,FF00,FC44", outClusters: "1000,0003,0006,0008,0004,0005,0000,FF00"
	}


		tiles {
		standardTile("thing", "device.thing", width: 2, height: 2) {
			state(name:"default", icon: "st.unknown.thing.thing-circle", label: "Please Wait")
		}
         standardTile("refresh", "device.image", inactiveLabel: false, decoration: "flat") {
          state "refresh", action:"getClusters", icon:"st.secondary.refresh"
        }
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "thing"
		details(["thing","refresh","configure"])
	}
}
def getClusters() { 
     "zdo active 0x${device.deviceNetworkId}" 
       log.debug "Get Clusters Called";
}

def parse(String description) {
    log.debug "parse description: $description"

    
    if (description?.startsWith('enroll request')) {
        List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        def result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
    }

def configure() {
	log.debug "Config Called"
def configCmds=[	
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 6 {${device.zigbeeId}} {}","delay 500",
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 8 {${device.zigbeeId}} {}"
    ]
    return configCmds
}