const userAgent = navigator.userAgent;
const useJsIR = /iphone|kindle|ipad|Safari|AppleWebKit/i.test(userAgent);
const dataSrc = useJsIR ? 'js/index.html' : 'wasmJs/index.html';

document.write(`
    <div class="container">
        <object class="internal" type="text/html" data="${dataSrc}"></object>
    </div>
`);