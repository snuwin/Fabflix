function getParameterByName(target) {
    let url = window.location.href;

    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");
    console.log(resultData);

    let movieTitleElement = jQuery("#movie_title_body");
    let movieTitle = resultData[0]["title"];
    movieTitleElement.append("<h1>" + movieTitle + "</h1>");

    let movieInfoElement = jQuery("#single_movie_table_body");
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        let movie = resultData[i];
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        let genres = movie["genres"].split(", ");
        let genreLinks = genres.map(function(genre) {
            return `<a href="index.html?genre=${encodeURIComponent(genre)}">${genre}</a>`;
        }).join(", ");
        rowHTML += "<th>" + genreLinks + "</th>";

        let starLinksHTML = "";
        for (let j = 0; j < movie["starIds"].length; j++) {
            let starId = movie["starIds"][j];
            let starName = movie["stars"][j];
            starLinksHTML += '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
            if (j < movie["starIds"].length - 1) {
                starLinksHTML += ", ";
            }
        }
        rowHTML += "<th>" + starLinksHTML + "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";

        movieInfoElement.append(rowHTML);
    }
}

function addToCart() {
    let movieId = getParameterByName('id');  // Get the movie ID from the URL
    let cartItems = sessionStorage.getItem("cartItems");

    if (!cartItems) {
        cartItems = [];
    } else {
        cartItems = JSON.parse(cartItems);
    }

    cartItems.push(movieId);
    sessionStorage.setItem("cartItems", JSON.stringify(cartItems));

    alert("Movie added to cart successfully!");
}

jQuery(document).ready(function() {
    let movieId = getParameterByName('id');
    console.log("Movie ID:", movieId);

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movieInfo?id=" + movieId,
        success: (resultData) => handleResult(resultData)
    });

    jQuery("#add-to-cart-button").click(() => addToCart());
});
