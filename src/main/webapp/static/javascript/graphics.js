const {enable3d, Scene3D, Canvas, Cameras, THREE, ExtendedObject3D} = ENABLE3D

firebase.auth().onAuthStateChanged((user) => {
    if (!user) {
        let game = document.querySelector("#game")
        game.innerHTML = "Please <a href='/login'>Login</a> or <a href='/signup'>Signup</a> to play."
    }
})

class Pilot extends Scene3D {
    constructor() {
        super({
            key: "Pilot"
        });
    }

    preload() {
        this.load.image("rock", "/static/assets/space-rock.png")
        this.third.load.preload("background", "/static/assets/background.png")
    }

    init() {
        this.accessThirdDimension()
    }

    create() {
        const zoom = 70
        const w = this.cameras.main.width / zoom
        const h = this.cameras.main.height / zoom

        this.cams = {
            ortho: this.third.cameras.orthographicCamera(this, {left: w / -2, right: w / 2, top: h / 2, bottom: h / -2}),
            perspective: this.third.camera,
        }

        this.third.warpSpeed("-ground", "-sky")

        // this.third.physics.debug.enable()

        this.third.renderer.gammaFactor = 1.2

        this.third.load.texture("background").then(bg => (this.third.scene.background = bg))

        this.cams.offset = new THREE.Vector3()

        this.third.camera.position.set(0, 5, 20)
        this.third.camera.lookAt(0, 0, 0)
        this.third.camera = this.cams.perspective

        // this.third.physics.add.box()

        let graphics = this.add.graphics({
            lineStyle: {
                color: 0xffffff
            },
            fillStyle: {
                color: 0x000000
            }
        })
        graphics.strokeRect(345, 270, 270, 270)

        this.third.add.sphere({ x: 0, y: 2, z: 0, radius: 1 })

        /* let rock1 = this.add.image(480, 405, "rock").setScale(0.5, 0.5)

        let rockTeleport = setInterval(() => {
            rock1.x = Math.random() * 270 + 345
            rock1.y = Math.random() * 270 + 270
        }, 100) */
    }

    update (time, delta) {
        this.third.camera.position.set(0, 5, 20)
        this.third.camera.lookAt(0, 0, 0)
    }
}

class Start extends Scene3D {
    constructor() {
        super({
            key: "Start"
        });
    }

    preload() {

    }

    init() {
        // this.accessThirdDimension()
    }

    create() {
        // this.third.warpSpeed()

        // this.third.physics.add.box()

        this.add.text(480, 150, "Outmaneuver", {
            fontFamily: "monaco",
            fontSize: "80px",
            fill: "white"
        }).setOrigin(0.5, 0.5)

        let playButton = this.add.text(480, 400, "Play", {
            fontFamily: "monaco",
            fontSize: "24px",
            fill: "white"
        }).setOrigin(0.5, 0.5)

        playButton.setInteractive({useHandCursor: true})
            .on("pointerover", () => playButton.setFill("#0f0"))
            .on("pointerout", () => playButton.setFill("white"))
            .on("pointerdown", () => this.scene.start("Pilot"))
    }
}

let config = {
    type: Phaser.WEBGL,
    transparent: true,
    parent: "game",
    width: 960,
    height: 540,
    scale: {
        mode: Phaser.Scale.FIT,
        autoCenter: Phaser.Scale.CENTER_BOTH
    },
    scene: [Start, Pilot],
    ...Canvas()
}

window.addEventListener('load', () => {
    enable3d(() => new Phaser.Game(config)).withPhysics('/static/assets')
})