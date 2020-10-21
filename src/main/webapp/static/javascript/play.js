firebase.auth().onAuthStateChanged((user) => {
    if (!user) {
        let game = document.querySelector(".game")
        game.innerHTML = "Please <a href='/login'>Login</a> or <a href='/signup'>Signup</a> to play."
    }
})