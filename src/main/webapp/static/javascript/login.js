firebase.auth().onAuthStateChanged((user) => {
    if (user)
        window.location = "/"
})

addEventListener('keydown',event => {
    if(event.key === "Enter")
        login()
})

function login() {
    let email = document.getElementById("email").value
    let password = document.getElementById("password").value

    firebase.auth().signInWithEmailAndPassword(email, password).catch(error => {
        alert(`Unable to login ${error.message}`)
    })
}
