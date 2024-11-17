const port = window.location.port;
const protocol = window.location.protocol;
const domain = window.location.hostname;
window.location.href=(protocol+"//"+domain+":"+port+"/endpointService")
