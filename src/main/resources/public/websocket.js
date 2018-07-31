window.STATIC_URL="http://localhost:8080/";
var ws = null;
var url = window.STATIC_URL+"webSocketServer/sockjs";
var transports = [];

function connect(data) {
    if (!url) {
        alert('Select whether to use W3C WebSocket or SockJS');
        return;
    }

    ws = new SockJS(window.STATIC_URL+"webSocketServer/sockjs");

    ws.onopen = function () {
        echo(data);
        echo_msg('Info: connection opened.');
    };

    ws.onmessage = function (event) {
        echo_msg(event.data);
    };

    ws.onclose = function (event) {
        echo_msg('Info: connection closed.');
        echo_msg(event);
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        console.log("close....!!");
        ws = null;
    }
}

function echo(data) {
    if (ws != null) {
        ws.send(data);
    } else {
        alert('connection not established, please connect.');
    }
}

function updateUrl(urlPath) {
    if (urlPath.indexOf('sockjs') != -1) {
        url = urlPath;
        document.getElementById('sockJsTransportSelect').style.visibility = 'visible';
    }
    else {
        if (window.location.protocol == 'http:') {
            url = 'ws://' + window.location.host + urlPath;
        } else {
            url = 'wss://' + window.location.host + urlPath;
        }
        document.getElementById('sockJsTransportSelect').style.visibility = 'hidden';
    }
}

function updateTransport(transport) {
    transports = (transport == 'all') ?  [] : [transport];
}

function echo_msg(message) {
    console.log(message);
    try
    {
        var terminal = document.getElementById('console');
        var p = document.createElement('p');
        p.style.wordWrap = 'break-word';
        p.appendChild(document.createTextNode(message));
        terminal.appendChild(p);
        while (terminal.childNodes.length > 25) {
            terminal.removeChild(terminal.firstChild);
        }
        terminal.scrollTop = terminal.scrollHeight;
    }
    catch( e )
    {
        console.log( e );
        disconnect();
    }
}
