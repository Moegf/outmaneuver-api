let gameContainer = document.getElementById("gameContainer")

firebase.auth().onAuthStateChanged(user => {
    if(user){
        loginAlert.remove()
        gameContainer.classList.remove("invisible")
        firebase.firestore().collection("rooms").doc(getParam("id")).get().then(room => {
            loadRoom(room)
        }).catch(error => {
            alert(`There was an error loading the room: ${error.message}`)
            location.reload()
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