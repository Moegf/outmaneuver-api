#OutManeuver
OutManeuver is a multiplayer online game in development by the Commonwealth School game design club.

##Running
The Maven manages the dependencies, use the package and clean commands accordingly.
To run, create use `launcher.Main` as the main class. In order for Firebase to work, admin credentials must be entered as an environment variable.

##API
The backend uses a websocket located at `/roomsocket` to handle player commands sent over JSON.
The following features are supported:

###From Client to Server

####Joining A Room
```json5
{
  type: "join",
  uid: "[uid]",
  roomID: "[roomID]"
}
```

####Selecting A Role
```json5
{
  type: "role",
  role: "[role]"
}
```

###From Server to Client

####Updating gamedata
```json5
{
  players: {}, //map of player to role
  roles: {}, //map of role to availability
  status: "[status]"
}
```