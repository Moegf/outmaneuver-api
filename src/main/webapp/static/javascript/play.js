let loginAlert = document.getElementById("loginAlert")
let gameContainer = document.getElementById("gameContainer")
let roomContainer = document.getElementById("roomContainer")
let rooms = [];

firebase.auth().onAuthStateChanged(user => {
    if(user){
        loginAlert.remove()
        gameContainer.classList.remove("invisible")

        rooms = loadRooms()
    } else {
        //TODO: add logic here
    }
})

addEventListener('keydown',event => {
    if(event.key === "Enter")
        newRoom()
})

async function loadRooms() {
    firebase.firestore().collection("rooms").get().then(snapshot => {
        snapshot.forEach(room => {
            drawRoom(room)
        })
    })
}

async function drawRoom(room){
    //get host user object
    let host = await firebase.firestore().collection("users").doc(room.data().host).get()
    let roomHTML = "";
    roomHTML += `<div class="room">`
    roomHTML += `<h1>${room.data().name}</h1>`
        roomHTML += `<h3>Hosted By: ${host.data().username}</h3>`

    if(room.data().host == firebase.auth().currentUser.uid) {
        roomHTML += `<h3 class="delete" onclick="deleteRoom('${room.id}')">Delete Room</h3>`
    }

    roomHTML += `<a href="/room?id=${room.id}">join</a>`
    roomHTML += `</div>`

    roomContainer.innerHTML += roomHTML
}

function newRoom(){
    let name = document.getElementById("name").value

    if(!name){
        alert("Name cannot be empty")
        location.reload()
    }

    firebase.firestore().collection("rooms").add({
        name: name,
        status: "pending",
        host: firebase.auth().currentUser.uid,
    }).then(room => {
        room.collection("players").doc(firebase.auth().currentUser.uid).set({
            role: "waiting"
        })
        window.location = `/room?id=${room.id}`
    }).catch(error => {
        alert(`An error occured while creating a new room: ${error.message}`)
        location.reload()
    })
}

function deleteRoom(id){
    firebase.firestore().collection("rooms").doc(id).delete().then(() => {
        location.reload()
    }).catch(error => {
        alert(`Unable to delete room: ${error.message}`)
        location.reload()
    })
}