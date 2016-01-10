/**
 *  BBQ Guru CyberQ
 *
 *  Copyright 2016 Kevin Tierney
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
metadata {
	definition (name: "BBQ Guru CyberQ", namespace: "tierneykev", author: "Kevin Tierney") {
		capability "Actuator"
		capability "Sensor"
		capability "Temperature Measurement"
        capability "Refresh"
        
        //Cook
        attribute "cooktemp", number
        attribute "cookset", number
        attribute "cookstatus", string      
        //Food1
        attribute "food1name", string
		attribute "food1temp", number
        attribute "food1set", number
        attribute "food1status", string        
        //Food2
        attribute "food2name", string
        attribute "food2temp", number
        attribute "food2set", number
        attribute "food2status", string
        //Food3
        attribute "food3name", string
		attribute "food3temp", number           
        attribute "food3set", number                
        attribute "food3status", string        
        //Timer & Output (No Parent Element)
        attribute "output_percent", string
        attribute "timercurr", string
        attribute "timerstatus", string

        
        command "setCooksetTemp"
        command "setFood1Temp"
        command "setFood2Temp"
        command "setFood3Temp"


	}
    preferences{
    	input("deviceIP", "string", title:"IP Address", description: "IP Address", required: true, displayDuringSetup: true)
        input("devicePort", "string", title:"Port", description: "Port", defaultValue: 80 , required: true, displayDuringSetup: true)
        input("loginRequired", "bool", title:"Is login required?", description: "Choose if user/pass has been enabled", defaultValue: true, displayDuringSetup: true)
        input("username", "string", title:"User", description: "Please enter your username", required: false, displayDuringSetup: true)
    	input("password", "password", title:"Password", description: "Please enter your password", required: false, displayDuringSetup: true)
        input("cookName","string", title:"Cook Name", description: "Name cooker sensor",required:false,displayDuringSetup:true,defaultValue:"CookName")
        input("food1Name","string", title:"Food 1 Name", description: "Name Food 1 Sensor",required:false,displayDuringSetup:true,defaultValue:"Food1")
        input("food2Name","string", title:"Food 2 Name", description: "Name Food 2 Sensor",required:false,displayDuringSetup:true,defaultValue:"Food2")
        input("food3Name","string", title:"Food 3 Name", description: "Name Food 3 Sensor",required:false,displayDuringSetup:true,defaultValue:"Food3")
    
    }

	tiles {
        //Cook
    	valueTile("cooktemp", "device.cooktemp",width:2,height:2) {
            state "default", label:'${currentValue}'
        }
        
        valueTile("cookstatus", "device.cookstatus") {
            state "default", label:'${currentValue}'
        }
        controlTile("cooksetcontrol","device.cookset","slider",height:1,width:2,range:"(32..475)"){
        	state "cookset",action:"setCooksetTemp"
        }
        valueTile("cookset", "device.cookset") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        
	    //Food1
     	valueTile("food1name", "device.food1name") {
            state "default", label:'${currentValue}'
        }  
		valueTile("food1temp", "device.food1temp") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        valueTile("food1set", "device.food1set") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        valueTile("food1status", "device.food1status") {
            state "default", label:'${currentValue}'
        }
        controlTile("food1control","device.food1set","slider",height:1,width:2,range:"(32..475)"){
        	state "food1set",action:"setFood1Temp"
        }
	    //Food2
        valueTile("food2name", "device.food2name") {
            state "default", label:'${currentValue}'
        }
    	valueTile("food2temp", "device.food2temp") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        valueTile("food2set", "device.food2set") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        valueTile("food2status", "device.food2status") {
            state "default", label:'${currentValue}'
        }
		controlTile("food2control","device.food2set","slider",height:1,width:2,range:"(32..475)"){
        	state "food2set",action:"setFood2Temp"
        }

	    //Food3
        valueTile("food3name", "device.food3name") {
            state "default", label:'${currentValue}'
        }
     	valueTile("food3temp", "device.food3temp") {
            state "default", label:'${currentValue}',unit:'dF'
        }                
        valueTile("food3set", "device.food3set") {
            state "default", label:'${currentValue}',unit:'dF'
        }
        controlTile("food3control","device.food3set","slider",height:1,width:2,range:"(32..475)"){
        	state "food3set",action:"setFood3Temp"
        }
        valueTile("food3status", "device.food3status") {
            state "default", label:'${currentValue}'
        }
	    //Timer & Output (No Parent Element)
    	valueTile("output", "device.output_percent") {
            state "default", label:'Output %\n ${currentValue}'
        }
        valueTile("timer", "device.timercurr") {
            state "default", label:'Timer\n ${currentValue}'
        }
	    
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
    	}

    
        main(["cooktemp"])
        details([
        "cooktemp","cookstatus","output",
        "cooksetcontrol","cookset",
        "food1name","food1status","food1temp",
        "food1control","food1set",
        "food2name","food2status","food2temp",
        "food2control","food2set",
        "food3name","food3status","food3temp",
        "food3control", "food3set",
        "timer",
        "refresh"
        ])
	}
}
def installed(){
	log.debug "Installed with settings: ${settings}"
    updateSettings()
}
def updated(){
    log.debug "Updated with settings: ${settings}"
    //Need to set the devices network ID - ip:port in hex
	def hosthex = convertIPtoHex(deviceIP).toUpperCase() 
    def porthex = convertPortToHex(devicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex" 
    updateSettings() 
}
def refresh(){
	log.trace 'Refresh Called'
    getStatus()
}

def setCooksetTemp(degrees){
	//log.debug("setCooksetTemp Called: ${degrees}")
    setParameter('COOK_SET',degrees)
}
def setFood1Temp(degrees){
	//log.debug("setFood1Temp Called: ${degrees}")
    setParameter('FOOD1_SET',degrees)
}
def setFood2Temp(degrees){
	//log.debug("setFood2Temp Called: ${degrees}")
    setParameter('FOOD2_SET',degrees)
}
def setFood3Temp(degrees){
	//log.debug("setFood3Temp Called: ${degrees}")
    setParameter('FOOD3_SET',degrees)
}
def parse(description) {
	//log.debug "Parsing '${description}'"
	def result = []
	def msg = parseLanMessage(description)
    //log.debug "msg: ${msg}"
    //log.debug "body: ${msg.body}"
    //log.debug "xml: ${msg.xml}"
    
    
    def xml = msg.xml
    if(xml){
        //Cook
        result << createEvent(name: "cooktemp", value:convertTemp(xml.COOK.COOK_TEMP.text()) )
        result << createEvent(name: "cookset", value:convertTemp(xml.COOK.COOK_SET.text()) )
        result << createEvent(name: "cookstatus", value: convertStatus(xml.COOK.COOK_STATUS.text()))
        //Food1
        result << createEvent(name: "food1name", value: xml.FOOD1.FOOD1_NAME.text()) 
        result << createEvent(name: "food1temp", value:convertTemp(xml.FOOD1.FOOD1_TEMP.text()) )
        result << createEvent(name: "food1set", value:convertTemp(xml.FOOD1.FOOD1_SET.text()) )
        result << createEvent(name: "food1status", value: convertStatus(xml.FOOD1.FOOD1_STATUS.text()))
        //Food2
        result << createEvent(name: "food2name", value: xml.FOOD2.FOOD2_NAME.text()) 
        result << createEvent(name: "food2temp", value:convertTemp(xml.FOOD2.FOOD2_TEMP.text()) )
        result << createEvent(name: "food2set", value:convertTemp(xml.FOOD2.FOOD2_SET.text()) )
        result << createEvent(name: "food2status", value: convertStatus(xml.FOOD2.FOOD2_STATUS.text()))
        //Food3
        result << createEvent(name: "food3name", value: xml.FOOD3.FOOD3_NAME.text()) 
        result << createEvent(name: "food3temp", value:convertTemp(xml.FOOD3.FOOD3_TEMP.text()) )
        result << createEvent(name: "food3set", value:convertTemp(xml.FOOD3.FOOD3_SET.text()) )
        result << createEvent(name: "food3status", value: convertStatus(xml.FOOD3.FOOD3_STATUS.text()))
        //Timer & Output (No Parent Element)
        result << createEvent(name: "output_percent", value: xml.OUTPUT_PERCENT.text()) 
        result << createEvent(name: "timercurr", value: xml.TIMER_CURR.text())
     
        result
        
    } else {
    	//When sending command, device responds with HTML page
        
    	//log.debug "Non XML response received"
        runIn(15,getStatus) //query XML and update values
    }
}



def updateSettings(){
	//log.trace "update settings called"
	def userpassascii = "${username}:${password}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	def headers = [:] 
    headers.put("HOST", "$deviceIP:$devicePort")
    if (loginRequired) {
        headers.put("Authorization", userpass)
    }
    
    def cookName = settings['cookName']
    def food1Name = settings['food1Name']
    def food2Name = settings['food2Name']
    def food3Name = settings['food3Name']
    def body = "COOK_NAME=${cookName}&FOOD1_NAME=${food1Name}&FOOD2_NAME=${food2Name}&FOOD3_NAME=${food3Name}"
    
    def result = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/",
        headers: headers,
       	body: body
       )
}	

def getStatus() {
	log.trace "Get Status Called"
	def userpassascii = "${username}:${password}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    
    def headers = [:] 
    headers.put("HOST", "$deviceIP:$devicePort")
    if (loginRequired) {
        headers.put("Authorization", userpass)
    }
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/config.xml",
        headers: headers
       )
}
private setParameter(parameter,value){
	log.trace "setParameter called with ${parameter}:${value}"
	def userpassascii = "${username}:${password}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
   
    def headers = [:] 
    headers.put("HOST", "$deviceIP:$devicePort")
    if (loginRequired) {
        headers.put("Authorization", userpass)
    }
	
    def result = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/",
        headers: headers,
        body: """${parameter}=${value}"""
       )
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug "Port entered is $port and the converted hex code is $hexport"
    return hexport
}

private convertTemp(String temp){
	def formattedTemp
	if (temp == 'OPEN'){
    formattedTemp = 0.0
    } else {
    formattedTemp = temp.toFloat()/10
    }
    return formattedTemp
}
private convertStatus(statusNum){
	def status
    switch(statusNum){
    // 0 "OK", 1 "HIGH", 2 "LOW", 3 "DONE", 4 "ERROR", 5 "HOLD", 6 "ALARM", 7 "SHUTDOWN"
	case '0':
		status = 'OK'
     	break
    case '1':
		status = 'High'
     	break
    case '2':
      status = 'Low'
      break
    case '3':
      status = 'Done'
      break
    case '4':
      status = 'Error'
      break
    case '5':
      status = 'Hold'
      break
    case '6':
      status = 'Alarm'
      break
    case '7':
      status = 'Shutdown'
      break
    default:
     	status = 'Unknown'
     	break
    }   
    return status
}