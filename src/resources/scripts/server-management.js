var exampleSocket = new WebSocket(
	"http://127.0.0.1:8001"
);

// exampleSocket.onopen = function () {
// 	exampleSocket.send("First message !");
// 	exampleSocket.send("Second message !");
// }


function buttonFunction() {
	console.log("test");
	let button = document.getElementById("send-text");
	let text = document.getElementById("message");
	exampleSocket.send(text.value);
}

exampleSocket.onmessage = function (event) {
	document.getElementById("console").innerHTML("<p>dynamique content</p>" + event.data);
	console.log("<p>dynamique content</p>" + event.data);
};
