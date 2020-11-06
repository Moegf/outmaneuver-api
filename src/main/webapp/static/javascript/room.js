let gameContainer = document.getElementById("gameContainer")
let room
let user
let webSocket

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
            webSocket = new WebSocket("ws://localhost/roomsocket")
            webSocket.onopen = () => {joinRoom(user, room)}
        })
    } else {
        //TODO: add logic here
    }
})

function getParam(name){
    if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1])
}

function loadRoom(room){
    document.title = `${room.data().name}`
    gameContainer.innerHTML += `<h1>${room.data().name}</h1>`
}

function joinRoom (user, room) {
    webSocket.send(JSON.stringify({
        type: "join",
        uid: user.uid,
        roomID: room.id,
    }))
}