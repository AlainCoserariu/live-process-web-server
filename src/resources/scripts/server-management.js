var exampleSocket = new WebSocket(
	"http://127.0.0.1:8001"
);

function buttonFunction() {
	let button = document.getElementById("send-text");
	let text = document.getElementById("message");
	exampleSocket.send(text.value);
}

exampleSocket.onmessage = function (event) {
	let consString = document.getElementById("console");
	let msg = event.data;
	consString.innerHTML = msg.toString();
};
