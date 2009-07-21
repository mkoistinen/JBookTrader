function getXmlHttpObj() {
	try {
		return new XMLHttpRequest();
	}
	catch(e) {}
	
	try { 
		return new ActiveXObject("Msxml2.XMLHTTP.6.0");
	}
	catch(e) {}
	
	try { 
		return new ActiveXObject("Msxml2.XMLHTTP.3.0");
	}
	catch(e) {}
	
	try { 
		return new ActiveXObject("Msxml2.XMLHTTP");
	}
	catch(e) {}
	
	try { 
		return new ActiveXObject("Microsoft.XMLHTTP");
	}
	catch(e) {}

	return false;
}

function handleHttpResponse() {
	if (httpObj.readyState == 4) {
		if (httpObj.responseText.indexOf('invalid') == -1) {
			try {
				lines = httpObj.responseText.split("\n");
			}
			catch (e) {
				window.alert(e);
			}

			for (ln=0; ln<lines.length; ln++) {
				results = lines[ln].split(",");
				
				if (results[0].indexOf("[STRATEGY]") > -1) {
					strategy = results[1];
					try {
						document.getElementById(strategy + "_position").innerHTML = results[4];
						document.getElementById(strategy + "_trades").innerHTML = results[5];
						document.getElementById(strategy + "_maxdd").innerHTML = results[6];
						document.getElementById(strategy + "_pnl").innerHTML = results[7];
					}
					catch (e) {
						// JBT is probably now running additional strategy(ies)...
						refreshWholePage();
					}
					try {
						document.getElementById(strategy + "_symbol").innerHTML = results[2];
						document.getElementById(strategy + "_price").innerHTML = results[3];
					}
					catch (e) {
						// Ignore, some layouts just don't use these.
					}
				}
				else if (results[0].indexOf("[SYMBOL]") > -1) {
					try {
						symbol = results[1];
						document.getElementById(symbol + "_quote").innerHTML = results[2];
						document.getElementById(symbol + "_position").innerHTML = results[3];
						document.getElementById(symbol + "_pnl").innerHTML = results[4];
					}
					catch (e) {
						// Ignore, some layouts just don't use these.
					}
				}
				else if (results[0].indexOf("[SUMMARY]") > -1) {
					try {
						document.getElementById(symbol + "_trades").innerHTML = results[1];
						document.getElementById(symbol + "_pnl").innerHTML = results[2];
					}
					catch (e) {
						// How did this happen?
					}
				}
			}
		}
		isWorking = false;
	}
}

function update() {
	if (!isWorking && httpObj) {
		// Hack to bypass any caching
		now = new Date();
		httpObj.open("GET", url + "?" + now.getMilliseconds(), true);
		httpObj.onreadystatechange = handleHttpResponse;
		isWorking = true;
		httpObj.send(null);
	}
}

function refreshWholePage() {
	window.location.reload();
}

var httpObj = getXmlHttpObj();
var url = "update.html";
var isWorking = false;
var strategies = null;
var timerId = setInterval(function() { update(); false; }, 5000);