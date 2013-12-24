# iCasa REST API

iCasa provides a set of functional and simulation services used by pervasive application developers. A set of REST web services is provided to access those services. In this document the iCasa REST API will be presented. All web services use JSON as data representation.

- <a href="#Zone">Zone Service</a>
- <a href="#Device">Device Service</a>
- <a href="#Clock">Clock Service</a>
- <a href="#Person">Person Service</a>

<a name="Zone"></a>
## Zone Service

Base URL: _http://host:port/icasa/zone_

### Gets the zone list (GET)

Gets the list of zones in the iCasa execution platform.

Path: __http://host:port/icasa/zones/zones__

Path parameter -> None

Data parameter -> None

#### Example:

    > curl -X GET http://localhost:9000/icasa/zones/zones

Result:

    [
      {
        "id": "livingroom",
        "isRoom": true,
        "rightX": 655,
        "leftX": 410,
        "name": "livingroom",
        "topY": 28,
        "bottomY": 378,
        "variables": {
           "Volume": 10,
           "Illuminance": 5,
           "Temperature": 295.15
       }
      },
      {
       "id": "kitchen",
       "isRoom": true,
       "rightX": 655,
       "leftX": 410,
       "name": "kitchen",
       "topY": 370,
       "bottomY": 580,
       "variables": {
         "Illuminance": 0,
         "Temperature": 296.15
       }
      },
      {
       "id": "bathroom",
       "isRoom": true,
       "rightX": 315,
       "leftX": 55,
       "name": "bathroom",
       "topY": 20,
       "bottomY": 370,
       "variables": {
         "Volume": 10,
         "Illuminance": 5,
         "Temperature": 295.15
       }
      }
    ]

### Gets a specific zone (GET)

Gets the information of a particular zone

Path -> __http://host:port/icasa/zones/zone/${zoneId}__

Path parameter -> ${zoneId} the id of the zone to be consulted

Data parameter -> _None_


#### Example:  

    > curl -X GET http://localhost:9000/icasa/zones/zone/bathroom

Result:

    {
       "id": "bathroom",
       "isRoom": true,
       "rightX": 315,
       "leftX": 55,
       "name": "bathroom",
       "topY": 20,
       "bottomY": 370,
       "variables": {
          "Volume": 10,
          "Illuminance": 5,
          "Temperature": 295.15
        }
    }
    
### Craete a zone (POST)

Creates a new zone in the iCasa execution platform.

Path -> __http://host:port/icasa/zones/zone__

Path parameter -> None

Data parameter -> the JSON data associated with the new zone


#### Example:  

    > curl -X POST -d "{"zoneId":"hall","name":"hall","isRoom":false,"leftX":1,"topY":1,"rightX":50,"bottomY":50}" http://localhost:9000/icasa/zones/zone

Result:

    {
     "id":"hall",
     "isRoom":true,
     "rightX":50,
     "leftX":1,
     "name":"hall",
     "topY":1,
     "bottomY":50,
     "variables":{}
   }

   
### Delete an zone (DELETE)

Deletes a zone in the iCasa execution platform.

Path -> __http://host:port/icasa/zones/zone/${zoneId}__

Path parameter -> ${zoneId} the id of the zone to be deleted

Data parameter -> _None_


#### Example:  

    > curl -X DELETE http://localhost:9000/icasa/zones/zone/hall

   
### Updates a zone (PUT)

Updates a zone in the iCasa execution platform.

Path -> __http://host:port/icasa/zones/zone/${zoneId}__

Path parameter -> ${zoneId} the id of the zone to be created

Data parameter -> the JSON data associated with new zone


#### Example:  

    > curl -X POST -d "{"id":"hall","isRoom":true,"rightX":316,"leftX":98,"name":"hall","topY":72,"bottomY":277,"variables":{}}" _http://localhost:9000/icasa/zones/zone/hall_

Result:

    {
     "id":"hall",
     "isRoom":true,
     "rightX":316,
     "leftX":98,
     "name":"hall",
     "topY":72,
     "bottomY":277,
     "variables":{}
   }


<a name="Device"></a>
## Device Service

Base URL: _http://host:port/icasa/devices_

### Gets the device list (GET)

Gets the list of devices in the iCasa execution platform.

Path: __http://host:port/icasa/devices/devices__

Path parameter -> None

Data parameter -> None

#### Example:

    > curl -X GET http://localhost:9000/icasa/devices/devices

Result:

    [
       {
          "id": "Dimi-X3258Q-P",
          "location": "livingroom",
          "positionX": 577,
          "name": "Dimi-X3258Q-P",
          "positionY": 162,
          "state": "activated",
          "properties": {
             "power_level": 0,
             "state": "activated",
             "Location": "livingroom",
             "max_illuminance": 0,
             "fault": "no"
          },
          "type": "iCasa.DimmerLight",
          "services":["fr.liglab.adele.icasa.device.light.DimmerLight",
                    "fr.liglab.adele.icasa.simulator.SimulatedDevice",
                    "fr.liglab.adele.icasa.device.GenericDevice"],
          "fault": "no"
       },
       {
          "id": "Ther-A3654Q-S",
          "location": "kitchen",
          "positionX": 634,
          "name": "Ther-A3654Q-S",
          "positionY": 405,
          "state": "activated",
          "properties": {
             "state": "activated",
             "Location": "kitchen",
             "current_temperature": 296.15,
             "fault": "no"
          },
          "type": "iCasa.Thermometer",
          "services":["fr.liglab.adele.icasa.device.light.Photometer",
                    "fr.liglab.adele.icasa.simulator.SimulatedDevice",
                    "fr.liglab.adele.icasa.device.GenericDevice"],
          "fault": "no"
       }
    ]
   

### Gets a specific device (GET)

Gets the information of a particular device

Path -> __http://host:port/icasa/devices/device/${deviceId}__

Path parameter -> ${deviceId} the id of the device to be consulted

Data parameter -> _None_


#### Example:  

    > curl -X GET _http://localhost:9000/icasa/devices/device/Ther-A3654Q-S_

Result:

       {
          "id": "Ther-A3654Q-S",
          "location": "kitchen",
          "positionX": 634,
          "name": "Ther-A3654Q-S",
          "positionY": 405,
          "state": "activated",
          "properties": {
             "state": "activated",
             "Location": "kitchen",
             "current_temperature": 296.15,
             "fault": "no"
          },
          "type": "iCasa.Thermometer",
          "services":["fr.liglab.adele.icasa.device.light.Photometer",
                    "fr.liglab.adele.icasa.simulator.SimulatedDevice",
                    "fr.liglab.adele.icasa.device.GenericDevice"],
          "fault": "no"
       }

      
### Creates a device (POST)

Creates a new device in the iCasa execution platform.

Path -> __http://host:port/icasa/devices/device__

Path parameter -> None

Data parameter -> the JSON data associated with the new device


#### Example:  

    > curl -X POST -d "{"deviceId":"Heater-970c350695","name":"","type":"iCasa.Heater","positionX":1,"positionY":1,"properties":{}}" http://localhost:9000/icasa/devices/device

Result:

    {
      "id":"Heater-970c350695",
      "positionX":-1,
      "name":"Heater-970c350695",
      "positionY":-1,
      "state":"activated",
      "properties":{
         "heater.updaterThread.period":5000,
        "state":"activated",
        "fault":"no"
      },
      "type":"iCasa.Heater",
        "services":["fr.liglab.adele.icasa.device.temperature.Heater",
                    "fr.liglab.adele.icasa.simulator.SimulatedDevice",
                    "fr.liglab.adele.icasa.device.GenericDevice"],
      "fault":"no"}
   }
   

### Updates a device (PUT)

Updates a device in the iCasa execution platform.

Path -> __http://host:port/icasa/devices/device/${deviceId}__

Path parameter -> ${deviceId} the id of the device to be consulted

Data parameter -> the JSON data associated with the device


#### Example:  

    > curl -X POST -d "{"deviceId":"Heater-970c350695","name":"Heater-970c350695","type":"iCasa.Heater","positionX":182.8000030517578,"positionY":441.8000030517578,"properties":{"heater.updaterThread.period":5000,"state":"activated","fault":"no"},"id":"Heater-970c350695","width":32,"height":32,"state":"activated","fault":"no"}" http://localhost:9000/icasa/zones/zone/hall

Result:

    {
        "id":"Heater-970c350695",
        "location":"bedroom",
       "positionX":182,
      "name":"Heater-970c350695",
      "positionY":441,
      "state":"activated",
      "properties":{
         "heater.updaterThread.period":5000,
         "state":"activated",
         "Location":"bedroom",
         "fault":"no"},
      "type":"iCasa.Heater",
        "services":["fr.liglab.adele.icasa.device.temperature.Heater",
                "fr.liglab.adele.icasa.simulator.SimulatedDevice",
                "fr.liglab.adele.icasa.device.GenericDevice"],
      "fault":"no"
   }
   
   
### Deleta a device (DELETE)

Deletes a device in the iCasa execution platform.

Path -> __http://host:port/icasa/devices/device/${deviceId}__

Path parameter -> ${deviceId} the id of the device to be deleted

Data parameter -> _None_

<a name="Clock"></a>
## Clock Service

Base URL: _http://host:port/icasa/clocks/clock/default_

### Gets the clock (GET)

Gets the information associated to the default clock service

Path: __http://host:port/icasa/clocks/clock/default__

Path parameter -> None

Data parameter -> None

#### Example:

    curl -X GET http://localhost:9000/icasa/clocks/clock/default

Result:

    {
       "pause": false,
       "currentTime": 1327354107840,
       "startDate": 1319666400000,
       "startDateStr": "27/10/2011-00:00:00",
       "currentDateStr": "23/01/2012-22:28:27",
       "factor": 1440
    }

### Updates the clock (PUT)

Updates the state of the clock service in the execution platform

Path -> __http://host:port/icasa/clocks/clock/default__

Path parameter -> None

Data parameter -> None

    curl -X POST -d "{"pause": true, "startDate": 1319666400000, "factor": 1440}" http://localhost:9000/icasa/clocks/clock/default

Result:

    {
       "pause": false,
       "currentTime": 1327542202080,
       "startDate": 1319666400000,
       "startDateStr": "27/10/2011-00:00:00",
       "currentDateStr": "26/01/2012-02:43:22",
       "factor": 1440
    }


<a name="Person"></a>   
## Person Service

Base URL: _http://host:port/icasa/persons_

### Gets the person type list (GET)

Gets the list of person types in the iCasa execution platform.

Path: __http://host:port/icasa/persons/personTypes__

Path parameter -> None

Data parameter -> None

#### Example:

    > curl -X GET http://localhost:9000/icasa/persons/personTypes

Result:

    [
       {
           "id": "Grandfather",
           "name": "Grandfather"
       },
       {
           "id": "Grandmother",
           "name": "Grandmother"
       },
       {
           "id": "Father",
           "name": "Father"
       },
       {
           "id": "Mother",
           "name": "Mother"
       }
    ]

### Gets the person list (GET)

Gets the list of person in the iCasa execution platform.

Path: __http://host:port/icasa/persons/persons__

Path parameter -> None

Data parameter -> None

#### Example:

    > curl -X GET http://localhost:9000/icasa/persons/persons

Result:

    [
       {
           "id": "Paul",
           "location": "kitchen",
           "positionX": 506,
           "name": "Paul",
           "positionY": 439,
           "type": "Grandfather"
       },
       {
           "id": "Aurelie",
           "location": "livingroom",
           "positionX": 474,
           "name": "Aurelie",
           "positionY": 158,
           "type": "Grandmother"
       }
    ]

### Updates a person (PUT)

Updates the state of a persion service in the execution platform

Path -> __http://host:port/icasa/persons/person/${personId}__

Path parameter -> ${personId} the id of the person to be updated

Data parameter -> _None_


#### Example:  

    > curl -X PUT -d "{"personId":"Paul","name":"Paul","type":"Grandfather","positionX":542,"positionY":294,"id":"Paul","width":50,"height":50,"location":"kitchen"}" http://localhost:9000/icasa/persons/person/Paul

Result:

    {  
      "id":"Paul",
      "location":"livingroom",
      "positionX":542,
      "name":"Paul",
      "positionY":294,
      "type":"Grandfather"
   }

### Creates a person (POST)

Creates a new person in the iCasa execution platform.

Path -> __http://host:port/icasa/persons/person__

Path parameter -> None

Data parameter -> the JSON data associated with the new person


#### Example:  

    > curl -X POST -d "{"personId":"Paul","name":"Paul","type":"Grandfather","positionX":1,"positionY":1}" http://localhost:9000/icasa/persons/person

Result:

    {  
      "id":"Paul",
      "location":"unknown",
      "positionX":1,
      "name":"Paul",
      "positionY":1,
      "type":"Grandfather"
   }
   
### Deletes a person (DELETE)

Deletes a person in the iCasa execution platform.

Path -> __http://host:port/icasa/persons/person/${personId}__

Path parameter -> ${personId} the id of the person to be deleted

Data parameter -> _None_
