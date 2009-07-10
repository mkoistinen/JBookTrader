addEventListener("load", function() { setTimeout(updateLayout, 0); });

var currentWidth = 0;

function updateLayout()
{
	if (window.innerWidth != currentWidth)
	{
		currentWidth = window.innerWidth;
		var orient = currentWidth == 320 ? "profile" : "landscape";
		document.body.setAttribute("orient", orient);
		setTimeout(function() {
			window.scrollTo(0, 1);
		}, 100);
	}
}

setInterval(updateLayout, 400);