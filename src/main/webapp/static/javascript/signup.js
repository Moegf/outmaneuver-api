let uInput = document.getElementById("username")
let uTimer
let uNames
let ready = true

// firebase.firestore.setLogLevel('debug')

firebase.firestore().doc("/usernames/taken").get().then((doc) => {
    if (doc.exists) {
        uNames = doc.data().usernames
    } else {
        alert("Error: could not get document")
    }
}).catch(error => {
    alert("Something went wrong: " + error.message)
})

uInput.addEventListener('keyup', () => {
    clearTimeout(uTimer)
    uTimer = setTimeout(checkUname, 1000)
})

uInput.addEventListener('keydown', () => {
    clearTimeout(uTimer)
})

addEventListener('keydown',event => {
    if(event.key === "Enter")
        signup()
})

firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        if (ready) {
            window.location = "/"
        } else {
            setInterval(() => {
                if (ready)
                    window.location = "/"
            }, 100)
        }
    }
})

function checkUname() {
    let unique = document.getElementById("unique")
    let taken = document.getElementById("taken")

    if (uInput.value.length >= 3) {
        if (!(uNames.includes(uInput.value))) {
            unique.classList.remove("invisible")
            taken.classList.add("invisible")
            document.getElementById("signupButton").disabled = false
        } else {
            unique.classList.add("invisible")
            taken.classList.remove("invisible")
            document.getElementById("signupButton").disabled = true
        }
    } else {
        unique.classList.add("invisible")
        taken.classList.remove("invisible")
        document.getElementById("signupButton").disabled = true
    }
}

function signup() {
    ready = false
    let email = document.getElementById("email").value
    let password = document.getElementById("password").value
    let passwordConfirm = document.getElementById("passwordConfirm").value

    if (password !== passwordConfirm) {
        alert("Passwords do not match")
        // location.reload()
    }

    firebase.firestore().doc("/usernames/taken").get().then(doc => {
        if (doc.exists) {
            uNames = doc.data().usernames
            if (uInput.value.length >= 3 && !(uNames.includes(uInput.value))) {
                firebase.auth().createUserWithEmailAndPassword(email, password).then(result => {
                    firebase.firestore().doc("/users/" + result.user.uid).set({
                        username: uInput.value
                    }).then(() => {
                        ready = true
                    }).catch(error => {
                        alert("Something went wrong: " + error.message)
                    })
                }).catch(error => {
                    alert(`Something went wrong: ${error.message}`)
                    // location.reload()
                })

            } else {
                alert("Error: invalid username")
            }
        } else {
            alert("Error: could not get document")
        }
    }).catch(error => {
        alert(`Something went wrong: ${error.message}`)
        // location.reload()
    })
}
