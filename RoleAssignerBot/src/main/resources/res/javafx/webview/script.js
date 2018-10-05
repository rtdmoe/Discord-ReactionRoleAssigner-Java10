var nextMessage = 0;
const historySize = 512;
function append(msg) {
    var msgElement = getLogMessage(nextMessage);
    msgElement.innerHTML = msg;
    document.getElementById('console').appendChild(msgElement);
    window.scrollTo({top: document.body.scrollHeight, behaviour: "instant"});
    nextMessage += 1;
    if(nextMessage == historySize) nextMessage = 0;
}
function getLogMessage(msg) {
    var msgElement = document.getElementById('msg' + msg);
    if(msgElement === null || msgElement === undefined) {
        msgElement = document.createElement('div');
        msgElement.setAttribute('id', 'msg' + msg);
    } else {
        msgElement.parentNode.removeChild(msgElement);
    }
    return msgElement;
}
