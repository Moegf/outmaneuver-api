let uInput = document.getElementById("username")
let uTimer
let ready = true

// firebase.firestore.setLogLevel('debug')

// check entered username if nothing is typed for 1 second

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

// redirect if user is successfully logged in and/or signup process is finished

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

    // check if username is 3 or more characters

    if (uInput.value.length >= 3) {
        // show green check mark
        unique.classList.remove("invisible")
        taken.classList.add("invisible")
        document.getElementById("signupButton").disabled = false
    } else {
        // show red x
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

    // check username length

    if (uInput.value.length >= 3) {
        firebase.auth().createUserWithEmailAndPassword(email, password).then(result => {
            // update / create user document with username
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
}
