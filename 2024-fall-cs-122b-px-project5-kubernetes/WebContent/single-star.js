
/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    // Populate star info element
    let starInfoElement = jQuery("#star_info");

    // Populate the list of movies
    let starTableBodyElement = jQuery("#star_table_body");

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        let movie = resultData[i];
        let starName = resultData[i]["name"];
        let starDob = resultData[i]["birthYear"];

        starDob = starDob ? starDob : "N/A";

        // Append starName and starDob
        starInfoElement.append("<h1>" + starName + "</h1>");
        starInfoElement.append("<p>Date of Birth: " + starDob + "</p>");

        rowHTML += "<tr>";
        let movieLinksHTML = "";
        for (let j = 0; j < movie["movies"].length; j++) {
            let movieId = movie["movieIds"][j];
            let movieTitle = movie["movies"][j];

            movieLinksHTML += '<a href="single-movie.html?id=' + movieId + '">' + movieTitle + '</a>';
            // Add comma and space after each hyperlink except for the last one
            if (j < movie["movieIds"].length - 1) {
                movieLinksHTML += ", ";
            }
        }
        rowHTML += "<td>" + movieLinksHTML + "</td>";
        rowHTML += "</tr>";
        console.log(resultData);
        starTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
let starId = getParameterByName("id");

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});