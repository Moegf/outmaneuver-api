firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        // User logged in already or has just logged in.
        let header = document.getElementById('header')
        let login = document.getElementById('login')
        let signup = document.getElementById('signup')
        let logout = document.getElementById('logout')

        login.classList.add('invisible')
        signup.classList.add('invisible')
        logout.classList.remove('invisible')

        header.innerHTML += `<p id="userGreeting">${user.email}</p>`

    } else {
        let logout = document.getElementById('logout')
        let login = document.getElementById('login')
        let signup = document.getElementById('signup')
        let greeting = document.getElementById('userGreeting')

        logout.classList.add('invisible')
        login.classList.remove('invisible')
        signup.classList.remove('invisible')
        if(greeting) greeting.remove()

    }
})

function logout(){
    firebase.auth().signOut().catch(error =>{
        alert(`Something went wrong on logging out: ${error.message}`)
    })
}