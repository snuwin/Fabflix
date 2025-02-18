function handleSearchResults(resultData) {
    let movieTableBodyElement = jQuery("#movie_table_body");

    for (let i = 0; i < resultData.length; i++) {
        let movie = resultData[i];

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-movie.html?id=' + movie["id"] + '">' +
            movie["title"] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + movie["year"] + "</th>";
        rowHTML += "<th>" + movie["director"] + "</th>";
        rowHTML += "<th>" + (movie["genres"] || "N/A") + "</th>";

        const stars = movie.stars && movie.starIds
            ? movie.stars.split(", ").map(
                (star, index) => {
                    // Split starIds into an array for mapping
                    const starIdArray = movie.starIds.split(", ");
                    const starId = starIdArray[index] || "#"; // Fallback to "#" if no starId
                    return `<a href='single-star.html?id=${starId}'>${star}</a>`;
                }
            ).join(", ")
            : "N/A";

        rowHTML += "<th>" + stars + "</th>";
        rowHTML += "<th>" + (movie["rating"] || "N/A") + "</th>";
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }
    console.log("Result Data:", resultData);
}

$(document).ready(function () {
    console.log("search-results.js loaded successfully");

    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get("title") || "";
    if (!searchQuery) {
        console.log("error: param title");
    } else {
        console.log("search query:", searchQuery);
    }

    if (searchQuery.trim() === "") {
        $("#movie_table_body").html("<tr><td colspan='6'>No search query provided. Please go back and try again.</td></tr>");
        return;
    }

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        data: { title: searchQuery },
        success: (resultData) => {
            console.log("AJAX Success:", resultData);
            handleSearchResults(resultData);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Error fetching search results:", textStatus, errorThrown);
            $("#movie_table_body").html("<tr><td colspan='6'>An error occurred while fetching results. Please try again later.</td></tr>");
        },
    });
});
