metadata {
	definition (name: "Zigbee Test", namespace: "tierneykev", author: "Kevin Tierney") {
    command getClusters
	}

	tiles {
		standardTile("thing", "device.thing", width: 2, height: 2) {
			state(name:"default", icon: "st.unknown.thing.thing-circle", label: "Please Wait")
		}
         standardTile("refresh", "device.image", inactiveLabel: false, decoration: "flat") {
          state "refresh", action:"getClusters", icon:"st.secondary.refresh"
        }


		main "thing"
		details(["thing","refresh"])
	}
}


def getClusters() { 
     "zdo active 0x${device.deviceNetworkId}" 
       log.debug "Get Clusters Called";
}

def parse(String description) {
    log.debug "parse description: $description"
    }