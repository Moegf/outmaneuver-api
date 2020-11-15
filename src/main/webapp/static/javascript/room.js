let gameContainer = document.getElementById("gameContainer")
let webSocketURL = window.location.hostname === "localhost" ? "ws://localhost/roomsocket": "ws://outmaneuver.herokuapp.com/roomsocket"
let roleMenu = document.getElementById("roleMenu")
let room
let user
let webSocket
let gamedata

firebase.auth().onAuthStateChanged(data => {
    user = data
    if(user){
        loginAlert.remove()
        gameContainer.classList.remove("invisible")
        firebase.firestore().collection("rooms").doc(getParam("id")).get().then(doc => {
            room = doc
        }).catch(error => {
            alert(`There was an error loading the room: ${error.message}`)
            location.reload()
        }).then(() => {
            loadRoom(room)
            webSocket = new WebSocket(webSocketURL)
            webSocket.onopen = () => joinRoom(user, room)
            webSocket.onmessage = event => handleMessage(event)
        })
    } else {
        //TODO: add logic here
    }
})

function getParam(name) {
    if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1])
}

function loadRoom(room) {
    document.title = `${room.data().name}`
    document.getElementById("gameTitle").innerHTML += `<h1>${room.data().name}</h1>`
}

function joinRoom (user, room) {
    webSocket.send(JSON.stringify({
        type: "join",
        uid: user.uid,
        roomID: room.id,
    }))
}

function handleMessage(event) {
    let eventData = JSON.parse(event.data)
    switch(eventData.type){
        case "gamedata":
            gamedata = eventData
            loadGamedata()
            break;
        default:
            throw `Type of message not recognized, given: ${event.data.type}`
    }
}

function loadGamedata(){
    if(gamedata.status === "pending"){
        //set the role selection menu
        let roleDiv = document.getElementById("roleSelection")
        roleDiv.innerHTML = ""
        roleMenu = document.createElement("select")
        for(let role in gamedata.roles) {
            if (gamedata.roles[role] !== 0) {
                roleMenu.appendChild(new Option(role, role))
            }
        }
        roleMenu.onclick = setRole
        roleDiv.append(roleMenu)

        //show all the players
        let playerDiv = document.getElementById("players")
        playerDiv.innerHTML = `<h1>Players</h1>`
        for(player in gamedata.players) {
            let playerParagraph = document.createElement("h2")
            playerParagraph.innerText = `${player} - ${gamedata.players[player]}`
            playerDiv.appendChild(playerParagraph)
        }
    }
}

function setRole() {

    let role = roleMenu.value

    //check if role is current role
    if(role === gamedata.players[user.uid])
        return

    webSocket.send(JSON.stringify({
        type: "role",
        role: role
    }))
}