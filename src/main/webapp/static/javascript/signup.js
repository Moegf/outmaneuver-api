addEventListener('keydown',event => {
    if(event.key === "Enter")
        signup()
})

firebase.auth().onAuthStateChanged((user) => {
    if (user)
        window.location = "/"
})

function signup(){
    let email = document.getElementById("email").value
    let password = document.getElementById("password").value
    let passwordConfirm = document.getElementById("passwordConfirm").value

    if (password !== passwordConfirm) {
        alert("Passwords do not match")
        // location.reload()
    }

    firebase.auth().createUserWithEmailAndPassword(email, password).catch(error => {
        alert(`Something went wrong: ${error.message}`)
        // location.reload()
    })
}
