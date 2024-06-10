function isSafari() {
    var ua = window.navigator.userAgent;
    var iOS = !!ua.match(/iP(ad|od|hone)/i);
    var hasSafariInUa = !!ua.match(/Safari/i);
    var noOtherBrowsersInUa = !ua.match(/Chrome|CriOS|OPiOS|mercury|FxiOS|Firefox/i)
    var result = false;
    if(iOS) { //detecting Safari in IOS mobile browsers
        var webkit = !!ua.match(/WebKit/i);
        result = webkit && hasSafariInUa && noOtherBrowsersInUa
    } else if(window.safari !== undefined){ //detecting Safari in Desktop Browsers
        result = true;
    } else { // detecting Safari in other platforms
        result = hasSafariInUa && noOtherBrowsersInUa
    }
    return result;
}
document.addEventListener('DOMContentLoaded', () => {
    var canva = document.createElement('canvas');
    var ctx = canva.getContext("2d");
    var img = ctx.getImageData(0, 0, 1, 1);
    var pix = img.data;     // byte array, rgba
    var unknownCanvas = (pix[3] != 0);   // alpha in Safari is not zero
    const dataSrc = (unknownCanvas || isSafari()) ? 'js/index.html' : 'wasmJs/index.html';

    // Clear the existing document content
    document.body.innerHTML = '';

    // Create a new iframe element
    const iframe = document.createElement('iframe');

    // Set the src attribute to the dataSrc
    iframe.src = dataSrc;

    // Set iframe styles to occupy the entire viewport
    iframe.style.width = '100%';
    iframe.style.height = '100%';
    iframe.style.border = 'none';
    iframe.style.position = 'fixed';
    iframe.style.top = '0';
    iframe.style.left = '0';
    iframe.style.margin = '0';
    iframe.style.padding = '0';
    iframe.style.overflow = 'hidden';
    iframe.style.zIndex = '9999';

    // Append the iframe to the document body
    document.body.appendChild(iframe);
});