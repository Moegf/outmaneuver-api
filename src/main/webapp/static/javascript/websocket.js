const socket = {
}

socket.init = (url) => {
    socket.webSocket = new WebSocket(url)
}

socket.send = (data) => {
    socket.webSocket = (() => {
        socket.webSocket.send(data)
    })
}

socket.setListener = (callback) => {
    socket.webSocket.addEventListener("message", callback)
}