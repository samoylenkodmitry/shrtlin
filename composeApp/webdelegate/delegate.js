const userAgent = navigator.userAgent;
const useJsIR = /iphone|kindle|ipad|Safari|AppleWebKit/i.test(userAgent);
const dataSrc = useJsIR ? 'js/index.html' : 'wasmJs/index.html';

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