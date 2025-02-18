const contextPath = window.location.pathname.split('/')[1];
const dashboardUrl = `/${contextPath}/_dashboard`;

document.getElementById("addStarForm").onsubmit = async function(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const name = formData.get("name");
    const birthYear = formData.get("birthYear");

    const response = await fetch(`${dashboardUrl}`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `operation=add_star&name=${encodeURIComponent(name)}&birthYear=${encodeURIComponent(birthYear)}`
    });

    const resultText = await response.text();
    document.getElementById("addStarResult").innerHTML = resultText;
};

document.getElementById("getMetadataButton").onclick = async function() {
    const response = await fetch(`${dashboardUrl}`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "operation=get_metadata"
    });

    const resultText = await response.text();
    document.getElementById("metadataResult").innerHTML = resultText;
};

document.getElementById("addMovieForm").onsubmit = async function(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const title = formData.get("title");
    const year = formData.get("year");
    const director = formData.get("director");
    const starName = formData.get("starName");
    const genreName = formData.get("genreName");

    const response = await fetch(`${dashboardUrl}`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `operation=add_movie&title=${encodeURIComponent(title)}&year=${encodeURIComponent(year)}&director=${encodeURIComponent(director)}&starName=${encodeURIComponent(starName)}&genreName=${encodeURIComponent(genreName)}`
    });

    const resultText = await response.text();
    document.getElementById("addMovieResult").innerHTML = resultText;
};
