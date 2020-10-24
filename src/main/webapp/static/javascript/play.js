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

async function loadRooms() {
    firebase.firestore().collection("rooms").get().then(snapshot => {
        snapshot.forEach(room => {
            drawRoom(room)
        })
    })
}

function drawRoom(room){
    let roomHTML = "";
    roomHTML += `<div class="room">`
    roomHTML += `<h1>${room.data().name}</h1>`
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
        status: pending,
        host: firebase.auth().currentUser.uid
    }).then(room => {
        //TODO: send user to room
        location.reload()
    }).catch(error => {
        alert(`An error occured while creating a new room: ${error.message}`)
        location.reload()
    })
}